import sys
import os
import logging
import time
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware

# Standardized Logging Configuration
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)-8s | %(name)s | %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S"
)
logger = logging.getLogger("AegisMesh")

# This tells Python to look at the root folder of your project
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
from backend.database import db  # Initializes SQLite DB on startup

# Import our modular routes
from backend.routes import auth, emergency, hospitals, triage

app = FastAPI(
    title="Aegis Mesh Backend",
    description="Dynamic Resource Routing & AI Triage API",
    version="2.0.0" # Upgraded to modular architecture!
)

# Request-Response Middleware for Verbose Logging
@app.middleware("http")
async def log_requests(request: Request, call_next):
    start_time = time.time()

    # Read request body
    body = await request.body()
    body_str = body.decode("utf-8") if body else ""

    # We need to replace the request object with one that has the body already read
    # otherwise the route handler won't be able to read it.
    async def receive():
        return {"type": "http.request", "body": body}

    request._receive = receive

    response = await call_next(request)

    process_time = (time.time() - start_time) * 1000
    formatted_process_time = "{0:.2f}ms".format(process_time)

    # Log details about the request and response
    client_host = request.client.host if request.client else "unknown"

    log_msg = (
        f"REQ: {request.method} {request.url.path} | "
        f"RES: {response.status_code} | "
        f"TIME: {formatted_process_time} | "
        f"IP: {client_host}"
    )

    if body_str:
        log_msg += f" | BODY: {body_str[:200]}{'...' if len(body_str) > 200 else ''}"

    logger.info(log_msg)

    return response

# Startup Event: Banner and Route Info
@app.on_event("startup")
async def startup_event():
    print("\n" + "="*50)
    print("      🛡️  AEGIS MESH BACKEND IS STARTING UP  🛡️")
    print("="*50)
    logger.info(f"System: {app.title} v{app.version}")
    logger.info("Initializing modular routes and database connections...")

    # List all registered routes
    print("\nRegistered API Endpoints:")
    for route in app.routes:
        if hasattr(route, "methods"):
            methods = ", ".join(route.methods)
            print(f"  → [{methods}] {route.path}")
    print("="*50 + "\n")

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