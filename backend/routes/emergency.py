from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from backend.services.routing_service import RoutingService
from backend.services.alert_service import AlertService

router = APIRouter()

class VictimProfile(BaseModel):
    age: str = "Unknown"
    allergies: str = "None known"
    chronic_conditions: str = "None known"

class EmergencyRequest(BaseModel):
    victim_name: str
    condition: str
    latitude: float
    longitude: float
    profile: VictimProfile

@router.post("/dispatch")
def handle_emergency_dispatch(emergency: EmergencyRequest):
    try:
        # Trigger external alerts (SMS/Push)
        AlertService.broadcast_to_authorities(emergency)
        
        # Get the AI + Hospital routing plan
        dispatch_plan = RoutingService.generate_dispatch_plan(emergency)
        
        return {
            "status": "success",
            "dispatch_data": dispatch_plan
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))