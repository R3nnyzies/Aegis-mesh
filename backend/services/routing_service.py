from backend.services.hospital_service import HospitalService
from backend.services.ai_service import AIService
from backend.database.db import log_emergency
from backend.database.models import EmergencyLogModel

class RoutingService:
    @staticmethod
    def generate_dispatch_plan(emergency_data):
        # 1. Get facility
        facility = HospitalService.find_best_facility(
            condition=emergency_data.condition,
            lat=emergency_data.latitude,
            lon=emergency_data.longitude
        )
        
        # 2. Get AI Instructions
        first_aid = AIService.get_first_aid(
            profile=emergency_data.profile.model_dump(),
            condition=emergency_data.condition
        )

        # 3. LOG THE EMERGENCY TO THE DATABASE
        # This creates a permanent record of the SOS for analytics/authorities
        log_entry = EmergencyLogModel(
            victim_name=emergency_data.victim_name,
            emergency_type=emergency_data.condition,
            latitude=emergency_data.latitude,
            longitude=emergency_data.longitude,
            hospital_routed_to=facility["name"] if facility else "Unknown",
            ai_instructions_given=first_aid
        )
        log_emergency(log_entry)
        
        # 4. Return unified plan to the Android App
        return {
            "victim_name": emergency_data.victim_name,
            "emergency_type": emergency_data.condition,
            "recommended_facility": facility,
            "ai_first_aid_instructions": first_aid
        }
