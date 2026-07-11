import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))

from ai.triage_engine import generate_first_aid_instructions

class AIService:
    @staticmethod
    def get_first_aid(profile: dict, condition: str):
        return generate_first_aid_instructions(profile, condition)