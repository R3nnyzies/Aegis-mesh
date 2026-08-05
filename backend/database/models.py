from pydantic import BaseModel
from typing import Optional

class HospitalDBModel(BaseModel):
    id: Optional[int] = None
    facility_name: str
    latitude: float
    longitude: float
    known_inventory: str
    is_specialized: bool

class EmergencyLogModel(BaseModel):
    victim_name: str
    emergency_type: str
    latitude: float
    longitude: float
    hospital_routed_to: str
    ai_instructions_given: str
