import numpy as np
import tensorflow as tf
from fastapi import FastAPI, UploadFile, File, Request
from pydantic import BaseModel
from PIL import Image
import io
import os
import traceback
import time

app = FastAPI()

# Middleware to log EVERY request attempt reaching the server
@app.middleware("http")
async def log_requests(request: Request, call_next):
    start_time = time.time()
    print(f"\n>>> INCOMING: {request.method} {request.url.path} from {request.client.host}")
    response = await call_next(request)
    process_time = time.time() - start_time
    print(f"<<< COMPLETED: status {response.status_code} in {process_time:.4f}s\n")
    return response

CLASSES = [
    'apple', 'banana', 'cabbage', 'carrot', 'chicken', 'corn', 
    'cucumber', 'ginger', 'goat', 'grapes', 'mango', 'onion', 
    'orange', 'potato', 'tomato', 'watermelon'
]

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

TFLITE_MODEL_PATH = "app/src/main/ml/nutriscan_model.tflite"
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
        print(f"SUCCESS: TFLite model loaded!")
    else:
        print(f"ERROR: TFLite model not found at {TFLITE_MODEL_PATH}")
except Exception as e:
    print(f"ERROR LOADING TFLITE MODEL: {e}")

class FoodPredictionResponse(BaseModel):
    food_name: str
    serving_size: str
    calories: int
    protein: str
    carbs: str
    fat: str
    ph_level: float
    is_alkaline: bool

@app.get("/")
async def health_check():
    return {"status": "alive"}

@app.post("/predict", response_model=FoodPredictionResponse)
async def predict_food(image: UploadFile = File(...)):
    print(f"Processing image: {image.filename}")
    contents = await image.read()
    img = Image.open(io.BytesIO(contents)).convert("RGB").resize(IMG_SIZE)
    input_data = (np.array(img).astype(np.float32) / 127.5) - 1.0
    input_data = np.expand_dims(input_data, axis=0)
    
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    
    output_data = interpreter.get_tensor(output_details[0]['index'])
    class_idx = np.argmax(output_data[0])
    predicted_class = CLASSES[class_idx]
    
    print(f"RESULT: {predicted_class}")

    info = FOOD_DATA.get(predicted_class, {"calories": 0, "protein": "0g", "carbs": "0g", "fat": "0g", "ph_level": 7.0, "is_alkaline": True})

    return FoodPredictionResponse(
        food_name=predicted_class.title(),
        serving_size="100g",
        calories=info["calories"],
        protein=info["protein"],
        carbs=info["carbs"],
        fat=info["fat"],
        ph_level=info["ph_level"],
        is_alkaline=info["is_alkaline"]
    )

if __name__ == "__main__":
    import uvicorn
    # Important: 0.0.0.0 makes the server accessible via 10.0.2.2 from emulator
    uvicorn.run(app, host="0.0.0.0", port=8000)
