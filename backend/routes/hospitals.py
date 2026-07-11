from fastapi import APIRouter
from backend.services.hospital_service import HospitalService

router = APIRouter()

@router.get("/")
def get_all_hospitals():
    return {"hospitals": HospitalService.get_all_hospitals_from_db()}