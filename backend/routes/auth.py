from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import List, Optional

router = APIRouter()

class LoginRequest(BaseModel):
    username: str
    password: str

class MedicalProfileSchema(BaseModel):
    blood_group: Optional[str] = ""
    allergies: List[str] = []
    chronic_illnesses: List[str] = []
    current_medications: List[str] = []

class VerificationLevelSchema(BaseModel):
    phone_verified: bool = True
    national_id_verified: bool = False
    face_match_verified: bool = False

class ProfileUpdate(BaseModel):
    full_name: str
    age: Optional[str] = ""
    medical_profile: MedicalProfileSchema
    verification_level: Optional[VerificationLevelSchema] = None

# Mock database for demonstration
mock_user_db = {
    "full_name": "Cloud User (Jane Doe)",
    "age": "28",
    "medical_profile": {
        "blood_group": "O+",
        "allergies": ["Peanuts"],
        "chronic_illnesses": ["Type 1 Diabetes"],
        "current_medications": ["Insulin"]
    },
    "verification_level": {
        "phone_verified": True,
        "national_id_verified": True,
        "face_match_verified": False
    }
}

@router.post("/login")
def login(request: LoginRequest):
    # Simulated authentication
    if request.username and request.password:
        return {"status": "success", "message": "Authenticated", "token": "aegis_secure_token_123"}
    raise HTTPException(status_code=401, detail="Invalid credentials")

@router.get("/profile")
def get_user_profile():
    """Fetches the logged-in user's profile."""
    return {
        "status": "success",
        "profile": mock_user_db
    }

@router.post("/profile")
def update_user_profile(profile: ProfileUpdate):
    """Updates the logged-in user's profile."""
    global mock_user_db
    mock_user_db["full_name"] = profile.full_name
    mock_user_db["age"] = profile.age
    mock_user_db["medical_profile"] = profile.medical_profile.dict()
    if profile.verification_level:
        mock_user_db["verification_level"] = profile.verification_level.dict()

    return {
        "status": "success",
        "message": "Profile updated successfully",
        "profile": mock_user_db
    }
