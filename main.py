import json
import numpy as np
import tensorflow as tf
from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel
from PIL import Image
import io
import os

app = FastAPI()

# Load the database
DB_PATH = "app/src/main/assets/food_nutrition_ph_database.json"
with open(DB_PATH, "r") as f:
    food_db = json.load(f)

# IMPORTANT: Sorting classes alphabetically.
# This is standard for most datasets like Food-101.
CLASSES = sorted(list(food_db.keys()))
MODEL_PATH = "app/src/main/EFFICIENTNET_80_PERCENT_GOAL.weights.h5"
IMG_SIZE = (224, 224)

def build_model(num_classes):
    base_model = tf.keras.applications.EfficientNetB0(
        weights=None,
        include_top=False,
        input_shape=(IMG_SIZE[0], IMG_SIZE[1], 3)
    )
    x = tf.keras.layers.GlobalAveragePooling2D()(base_model.output)
    x = tf.keras.layers.Dense(256, activation='relu')(x)
    x = tf.keras.layers.Dropout(0.2)(x)
    output = tf.keras.layers.Dense(num_classes, activation='softmax')(x)
    return tf.keras.Model(inputs=base_model.input, outputs=output)

try:
    model = build_model(len(CLASSES))
    model.load_weights(MODEL_PATH)
    print(f"Weights loaded successfully! Handling {len(CLASSES)} classes.")
except Exception as e:
    print(f"Error loading weights: {e}")
    model = None

class FoodPredictionResponse(BaseModel):
    food_name: str
    calories: int
    protein: str
    carbs: str
    fat: str
    ph_level: float
    is_alkaline: bool

def preprocess_image(image_bytes):
    img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
    img = img.resize(IMG_SIZE)
    img_array = np.array(img)
    # Using official EfficientNet preprocessing
    img_array = tf.keras.applications.efficientnet.preprocess_input(img_array)
    img_array = np.expand_dims(img_array, axis=0)
    return img_array

@app.post("/predict", response_model=FoodPredictionResponse)
async def predict_food(image: UploadFile = File(...)):
    if model is None:
        return {"error": "Model not loaded."}

    contents = await image.read()
    input_data = preprocess_image(contents)

    predictions = model.predict(input_data)
    class_idx = np.argmax(predictions[0])
    confidence = float(np.max(predictions[0]))

    predicted_class = CLASSES[class_idx]
    print(f"Prediction: {predicted_class} (Confidence: {confidence:.2f})")

    food_data = food_db.get(predicted_class, food_db[CLASSES[0]])

    return FoodPredictionResponse(
        food_name=food_data["food_name"],
        calories=food_data["calories"],
        protein=f"{food_data['macronutrients']['protein_g']}g",
        carbs=f"{food_data['macronutrients']['carbs_g']}g",
        fat=f"{food_data['macronutrients']['fat_g']}g",
        ph_level=7.0 if food_data["ph_classification"] == "neutral" else (8.5 if "alkaline" in food_data["ph_classification"] else 6.0),
        is_alkaline="alkaline" in food_data["ph_classification"]
    )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
