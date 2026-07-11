from fastapi import APIRouter
from pydantic import BaseModel
from backend.services.ai_service import AIService

router = APIRouter()

class TriageRequest(BaseModel):
    condition: str
    profile: dict

@router.post("/")
def standalone_triage(request: TriageRequest):
    instructions = AIService.get_first_aid(request.profile, request.condition)
    return {"instructions": instructions}