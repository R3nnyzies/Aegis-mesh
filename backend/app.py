from fastapi import FastAPI
from backend.database import db  # Initializes SQLite DB on startup

# Import our modular routes
from backend.routes import auth, emergency, hospitals, triage

app = FastAPI(
    title="Aegis Mesh Backend",
    description="Dynamic Resource Routing & AI Triage API",
    version="2.0.0" # Upgraded to modular architecture!
)

# Register the routers and define their URL prefixes
app.include_router(auth.router, prefix="/api/v1/auth", tags=["Authentication"])
app.include_router(emergency.router, prefix="/api/v1/emergency", tags=["Emergency Dispatch"])
app.include_router(hospitals.router, prefix="/api/v1/hospitals", tags=["Medical Facilities"])
app.include_router(triage.router, prefix="/api/v1/triage", tags=["AI Triage"])

@app.get("/")
def health_check():
    return {"status": "Aegis Mesh API is running with modular routes!"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("app:app", host="0.0.0.0", port=8000, reload=True)