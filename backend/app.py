import sys
import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn

from backend.database import db

# Ensure Python can find our 'ai' folder which is one level up from 'backend'
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from backend.scraper.hospital_scraper import filter_specialized_clinics
from ai.triage_engine import generate_first_aid_instructions

app = FastAPI(
    title="Aegis Mesh Backend",
    description="Dynamic Resource Routing & AI Triage API",
    version="1.0.0"
)

# -----------------------------------------
# Pydantic Models (Data Schemas)
# -----------------------------------------
class VictimProfile(BaseModel):
    age: str = "Unknown"
    allergies: str = "None known"
    chronic_conditions: str = "None known"

class EmergencyRequest(BaseModel):
    victim_name: str
    condition: str
    latitude: float
    longitude: float
    profile: VictimProfile  # Nested profile for the AI context

# -----------------------------------------
# API Routes
# -----------------------------------------
@app.get("/")
def health_check():
    return {"status": "Aegis Mesh Backend is online."}

@app.post("/api/v1/emergency/dispatch")
def handle_emergency_dispatch(emergency: EmergencyRequest):
    """
    MASTER ENDPOINT:
    When the Android app connects to the internet, it hits this endpoint.
    1. Scrapes local clinics for the right medical inventory.
    2. Generates live AI first-aid steps based on the victim's mesh profile.
    """
    try:
        # Task 1: Find the right hospital via the Scraper Engine
        best_facility = filter_specialized_clinics(
            condition=emergency.condition, 
            lat=emergency.latitude, 
            lon=emergency.longitude
        )
        
        # Task 2: Generate First-Aid via the AI Triage Engine
        # We convert the nested Pydantic profile model into a standard dictionary
        first_aid_steps = generate_first_aid_instructions(
            victim_profile=emergency.profile.model_dump(), 
            current_emergency=emergency.condition
        )
        
        # Combine both into a single response for the mobile app
        return {
            "status": "success",
            "dispatch_data": {
                "victim_name": emergency.victim_name,
                "emergency_type": emergency.condition,
                "recommended_facility": best_facility,
                "ai_first_aid_instructions": first_aid_steps
            }
        }
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)