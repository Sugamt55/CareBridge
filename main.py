import json
import random
from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel
from typing import Optional

app = FastAPI()

# Load the database
with open("app/src/main/assets/food_nutrition_ph_database.json", "r") as f:
    food_db = json.load(f)

class FoodPredictionResponse(BaseModel):
    food_name: str
    calories: int
    protein: str
    carbs: str
    fat: str
    ph_level: float
    is_alkaline: bool

@app.post("/predict", response_model=FoodPredictionResponse)
async def predict_food(image: UploadFile = File(...)):
    # In a real scenario, you would use a machine learning model to classify the image.
    # For now, we'll simulate a prediction by picking a random item from the database.

    # Simulate processing time
    # await asyncio.sleep(1)

    food_id = random.choice(list(food_db.keys()))
    food_data = food_db[food_id]

    # Map database structure to the app's FoodPredictionResponse
    return FoodPredictionResponse(
        food_name=food_data["food_name"],
        calories=food_data["calories"],
        protein=f"{food_data['macronutrients']['protein_g']}g",
        carbs=f"{food_data['macronutrients']['carbs_g']}g",
        fat=f"{food_data['macronutrients']['fat_g']}g",
        # Default/Mock values for ph_level and is_alkaline as they differ in your JSON
        ph_level=7.0 if food_data["ph_classification"] == "neutral" else (8.5 if "alkaline" in food_data["ph_classification"] else 6.0),
        is_alkaline="alkaline" in food_data["ph_classification"]
    )

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
