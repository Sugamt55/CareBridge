import json
import numpy as np
import tensorflow as tf
from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel
from PIL import Image
import io
import os
import traceback

app = FastAPI()

# Load the database
DB_PATH = "app/src/main/assets/food_nutrition_ph_database.json"
try:
    with open(DB_PATH, "r") as f:
        food_db = json.load(f)
    print(f"Database loaded with {len(food_db)} items.")
except Exception as e:
    print(f"FAILED TO LOAD DATABASE: {e}")
    food_db = {}

# Standard Food-101 base list
base_classes = [
    'apple_pie', 'baby_back_ribs', 'baklava', 'beef_carpaccio', 'beef_tartare', 'beet_salad', 'beignets', 'bibimbap', 'bread_pudding', 'breakfast_burrito', 'bruschetta', 'caesar_salad', 'cannoli', 'caprese_salad', 'carrot_cake', 'ceviche', 'cheesecake', 'cheese_plate', 'chicken_curry', 'chicken_quesadilla', 'chicken_wings', 'chocolate_cake', 'chocolate_mousse', 'churros', 'clam_chowder', 'club_sandwich', 'crab_cakes', 'creme_brulee', 'croque_madame', 'cup_cakes', 'deviled_eggs', 'donuts', 'dumplings', 'edamame', 'eggs_benedict', 'escargots', 'falafel', 'filet_mignon', 'fish_and_chips', 'foie_gras', 'french_fries', 'french_onion_soup', 'french_toast', 'fried_calamari', 'fried_rice', 'frozen_yogurt', 'garlic_bread', 'gnocchi', 'greek_salad', 'grilled_cheese_sandwich', 'grilled_salmon', 'guacamole', 'gyoza', 'hamburger', 'hot_and_sour_soup', 'hot_dog', 'huevos_rancheros', 'hummus', 'ice_cream', 'lasagna', 'lobster_bisque', 'lobster_roll_sandwich', 'macaroni_and_cheese', 'macarons', 'miso_soup', 'mussels', 'nachos', 'omelette', 'onion_rings', 'oysters', 'pad_thai', 'paella', 'pancakes', 'panna_cotta', 'peking_duck', 'pho', 'pizza', 'pork_chop', 'poutine', 'prime_rib', 'pulled_pork_sandwich', 'ramen', 'ravioli', 'red_velvet_cake', 'risotto', 'samosa', 'sashimi', 'scallops', 'seaweed_salad', 'shrimp_and_grits', 'spaghetti_bolognese', 'spaghetti_carbonara', 'spring_rolls', 'steak', 'strawberry_shortcake', 'sushi', 'tacos', 'takoyaki', 'tiramisu', 'tuna_tartare', 'waffles'
]

# Padding the list to exactly 512 entries as required by the model
# Replace 'base_classes' with your 396 real names if you have them.
CLASSES = base_classes + [f"placeholder_{i}" for i in range(len(base_classes), 512)]

TFLITE_MODEL_PATH = "app/src/main/fixed_model_512.tflite"
interpreter = None
input_details = None
output_details = None
IMG_SIZE = (512, 512)

try:
    interpreter = tf.lite.Interpreter(model_path=TFLITE_MODEL_PATH)
    interpreter.allocate_tensors()
    input_details = interpreter.get_input_details()
    output_details = interpreter.get_output_details()
    
    # Auto-detect input size from the TFLite model
    input_shape = input_details[0]['shape']
    IMG_SIZE = (input_shape[1], input_shape[2])
    
    print(f"SUCCESS: TFLite model loaded! Expected Input: {IMG_SIZE}, Output: {output_details[0]['shape'][1]} classes")
except Exception as e:
    print(f"ERROR LOADING TFLITE MODEL: {e}")
    traceback.print_exc()

class FoodPredictionResponse(BaseModel):
    food_name: str
    calories: int
    protein: str
    carbs: str
    fat: str
    ph_level: float
    is_alkaline: bool

def preprocess_image(image_bytes, input_type):
    img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    img = img.resize(IMG_SIZE)
    img_array = np.array(img)
    
    # Cast to float32 if the model requires it
    if input_type == np.float32:
        img_array = (img_array.astype(np.float32) / 127.5) - 1.0
    
    img_array = np.expand_dims(img_array, axis=0)
    return img_array

@app.post("/predict", response_model=FoodPredictionResponse)
async def predict_food(image: UploadFile = File(...)):
    if interpreter is None:
        return FoodPredictionResponse(food_name="Model Not Loaded", calories=0, protein="0g", carbs="0g", fat="0g", ph_level=0.0, is_alkaline=False)

    try:
        contents = await image.read()
        input_data = preprocess_image(contents, input_details[0]['dtype'])
        
        # Run inference
        interpreter.set_tensor(input_details[0]['index'], input_data)
        interpreter.invoke()
        
        # Process output
        output_data = interpreter.get_tensor(output_details[0]['index'])
        class_idx = np.argmax(output_data[0])
        confidence = float(np.max(output_data[0]))
        
        predicted_class = CLASSES[class_idx]
        print(f"PREDICTED: {predicted_class} ({confidence:.2%})")

        # Try finding in database, or fallback to name only
        food_data = food_db.get(predicted_class)
        
        if not food_data:
            return FoodPredictionResponse(
                food_name=predicted_class.replace("_", " ").replace("placeholder", "Unknown Item").title(),
                calories=0, protein="0g", carbs="0g", fat="0g",
                ph_level=7.0, is_alkaline=True
            )
        
        macros = food_data.get("macronutrients", {})
        ph_class = str(food_data.get("ph_classification", "")).lower()
        
        return FoodPredictionResponse(
            food_name=food_data.get("food_name", predicted_class.title()),
            calories=int(food_data.get("calories", 0)),
            protein=f"{macros.get('protein_g', 0)}g",
            carbs=f"{macros.get('carbs_g', 0)}g",
            fat=f"{macros.get('fat_g', 0)}g",
            ph_level=7.0 if ph_class == "neutral" else (8.5 if "alkaline" in ph_class else 6.0),
            is_alkaline="alkaline" in ph_class
        )
    except Exception as e:
        print("--- RUNTIME ERROR ---")
        traceback.print_exc()
        return FoodPredictionResponse(
            food_name=f"Inference Error: {str(e)[:30]}",
            calories=0, protein="0g", carbs="0g", fat="0g",
            ph_level=0.0, is_alkaline=False
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
