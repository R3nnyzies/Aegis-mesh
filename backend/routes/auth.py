from fastapi import APIRouter, HTTPException
from pydantic import BaseModel

router = APIRouter()

class LoginRequest(BaseModel):
    username: str
    password: str

@router.post("/login")
def login(request: LoginRequest):
    # Simulated authentication
    if request.username and request.password:
        return {"status": "success", "message": "Authenticated", "token": "aegis_secure_token_123"}
    raise HTTPException(status_code=401, detail="Invalid credentials")