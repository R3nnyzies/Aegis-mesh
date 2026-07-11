from backend.services.hospital_service import HospitalService
from backend.services.ai_service import AIService

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
        
        # 3. Return unified plan
        return {
            "victim_name": emergency_data.victim_name,
            "emergency_type": emergency_data.condition,
            "recommended_facility": facility,
            "ai_first_aid_instructions": first_aid
        }