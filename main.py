import numpy as np
import tensorflow as tf
from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel
from PIL import Image
import io
import os
import traceback

app = FastAPI()

# Updated to exactly match your nutriscan_model.tflite (16 labels)
CLASSES = [
    'apple', 'banana', 'cabbage', 'carrot', 'chicken', 'corn', 
    'cucumber', 'ginger', 'goat', 'grapes', 'mango', 'onion', 
    'orange', 'potato', 'tomato', 'watermelon'
]

# Nutritional Data Mapping (Approximate values per 100g)
FOOD_DATA = {
    'apple': {"calories": 52, "protein": "0.3g", "carbs": "14g", "fat": "0.2g", "ph_level": 3.5, "is_alkaline": False},
    'banana': {"calories": 89, "protein": "1.1g", "carbs": "23g", "fat": "0.3g", "ph_level": 4.5, "is_alkaline": False},
    'cabbage': {"calories": 25, "protein": "1.3g", "carbs": "6g", "fat": "0.1g", "ph_level": 5.5, "is_alkaline": True},
    'carrot': {"calories": 41, "protein": "0.9g", "carbs": "10g", "fat": "0.2g", "ph_level": 6.0, "is_alkaline": True},
    'chicken': {"calories": 239, "protein": "27g", "carbs": "0g", "fat": "14g", "ph_level": 6.0, "is_alkaline": False},
    'corn': {"calories": 86, "protein": "3.2g", "carbs": "19g", "fat": "1.2g", "ph_level": 6.5, "is_alkaline": False},
    'cucumber': {"calories": 15, "protein": "0.7g", "carbs": "3.6g", "fat": "0.1g", "ph_level": 5.5, "is_alkaline": True},
    'ginger': {"calories": 80, "protein": "1.8g", "carbs": "18g", "fat": "0.8g", "ph_level": 5.5, "is_alkaline": True},
    'goat': {"calories": 143, "protein": "27g", "carbs": "0g", "fat": "3g", "ph_level": 6.0, "is_alkaline": False},
    'grapes': {"calories": 69, "protein": "0.7g", "carbs": "18g", "fat": "0.2g", "ph_level": 3.5, "is_alkaline": True},
    'mango': {"calories": 60, "protein": "0.8g", "carbs": "15g", "fat": "0.4g", "ph_level": 4.0, "is_alkaline": True},
    'onion': {"calories": 40, "protein": "1.1g", "carbs": "9g", "fat": "0.1g", "ph_level": 5.5, "is_alkaline": True},
    'orange': {"calories": 47, "protein": "0.9g", "carbs": "12g", "fat": "0.1g", "ph_level": 3.5, "is_alkaline": True},
    'potato': {"calories": 77, "protein": "2g", "carbs": "17g", "fat": "0.1g", "ph_level": 5.5, "is_alkaline": True},
    'tomato': {"calories": 18, "protein": "0.9g", "carbs": "3.9g", "fat": "0.2g", "ph_level": 4.5, "is_alkaline": True},
    'watermelon': {"calories": 30, "protein": "0.6g", "carbs": "8g", "fat": "0.2g", "ph_level": 5.5, "is_alkaline": True}
}

# Updated path to match the actual file in assets
TFLITE_MODEL_PATH = "app/src/main/assets/nutriscan_model.tflite"
interpreter = None
input_details = None
output_details = None
IMG_SIZE = (224, 224) 

try:
    if os.path.exists(TFLITE_MODEL_PATH):
        interpreter = tf.lite.Interpreter(model_path=TFLITE_MODEL_PATH)
        interpreter.allocate_tensors()
        input_details = interpreter.get_input_details()
        output_details = interpreter.get_output_details()
        
        # Auto-detect input size from the TFLite model
        input_shape = input_details[0]['shape']
        IMG_SIZE = (input_shape[1], input_shape[2])
        
        print(f"SUCCESS: TFLite model loaded! Expected Input: {IMG_SIZE}, Output: {len(CLASSES)} classes")
    else:
        print(f"ERROR: TFLite model not found at {TFLITE_MODEL_PATH}")
except Exception as e:
    print(f"ERROR LOADING TFLITE MODEL: {e}")
    traceback.print_exc()

class FoodPredictionResponse(BaseModel):
    food_name: str
    serving_size: str
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
        return FoodPredictionResponse(food_name="Model Not Loaded", serving_size="N/A", calories=0, protein="0g", carbs="0g", fat="0g", ph_level=0.0, is_alkaline=False)

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
        
        predicted_class = "Unknown"
        if class_idx < len(CLASSES):
            predicted_class = CLASSES[class_idx]
            
        print(f"PREDICTED: {predicted_class} ({confidence:.2%})")

        # Get nutritional data from our mapping
        info = FOOD_DATA.get(predicted_class, {
            "calories": 0, 
            "protein": "0g", 
            "carbs": "0g", 
            "fat": "0g", 
            "ph_level": 7.0, 
            "is_alkaline": True
        })

        return FoodPredictionResponse(
            food_name=predicted_class.replace("_", " ").title(),
            serving_size="100g",
            calories=info["calories"],
            protein=info["protein"],
            carbs=info["carbs"],
            fat=info["fat"],
            ph_level=info["ph_level"],
            is_alkaline=info["is_alkaline"]
        )
    except Exception as e:
        print("--- RUNTIME ERROR ---")
        traceback.print_exc()
        return FoodPredictionResponse(
            food_name=f"Inference Error: {str(e)[:30]}",
            serving_size="N/A",
            calories=0, protein="0g", carbs="0g", fat="0g",
            ph_level=0.0, is_alkaline=False
        )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
