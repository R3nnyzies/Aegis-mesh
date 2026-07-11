# Aegis Mesh: Enterprise Backend Architecture
### Phase 2: Services and Routes Refactoring Documentation

## Overview
As the Aegis Mesh backend expanded to handle Database connections, Web Scraping, AI Generation, and Notification alerts, a monolithic architecture (`app.py` containing all logic) became unscalable. 

The backend has been completely refactored into a modern, enterprise-grade modular architecture using FastAPI `APIRouter`. This creates a strict separation of concerns between API routing (receiving network requests) and Services (business logic).

---

## 1. The Services Layer (`backend/services/`)
**Purpose:** To encapsulate all core business logic, database queries, and external API calls. This layer does the actual "work" of the application.

*   `hospital_service.py`: Abstracts the web scraper and database. Routes don't need to know *how* to scrape HTML, they just ask this service to `find_best_facility()`.
*   `ai_service.py`: A wrapper class that interfaces with the external `ai/triage_engine.py` script and the Gemini LLM.
*   `routing_service.py`: The "Orchestrator." It imports both the Hospital and AI services, pulling data from both simultaneously to generate a unified dispatch plan.
*   `alert_service.py`: Responsible for external communications, simulating the broadcast of SOS data to external authorities or Twilio/Firebase notification systems.

---

## 2. The Routes Layer (`backend/routes/`)
**Purpose:** To define the API endpoints (URLs), enforce Pydantic data validation schemas, and handle HTTP status codes. These files are kept deliberately "thin."

*   `emergency.py` (`/api/v1/emergency/dispatch`): The master endpoint. It accepts the massive JSON payload from the Android `ApiClient`, passes the data to `RoutingService`, and returns the final JSON to the mobile app.
*   `auth.py` (`/api/v1/auth/login`): Handles user authentication. Returns mock JWT/secure tokens to allow the Android `LoginActivity` to transition the user to the Home Dashboard.
*   `hospitals.py` (`/api/v1/hospitals/`): A pure data endpoint that fetches the SQLite database list of known medical facilities.
*   `triage.py` (`/api/v1/triage/`): Allows external systems or decoupled app features to request AI first-aid without triggering a full hospital routing dispatch.

---

## 3. The FastAPI Core (`backend/app.py`)
**Purpose:** The application entry point. 
Because of the refactor, `app.py` is now strictly responsible for initializing the server (Uvicorn), initializing the SQLite database, and calling `app.include_router()` to plug the modular routes into the main application tree. This makes maintaining and scaling the API significantly easier for the development team.