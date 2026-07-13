### 1. API Documentation


# Aegis Mesh: API Documentation
**Version:** 1.0.0  
**Base URL:** `http://<SERVER_IP>:8000`

## Overview
The Aegis Mesh backend is primarily accessed when a localized mesh network establishes a bridge to the broader internet. The core functionality is centralized in the Master Dispatch endpoint, which handles both dynamic medical routing and AI-assisted first-aid generation.

---

## 1. Master Emergency Dispatch
**Endpoint:** `POST /api/v1/emergency/dispatch`  
**Description:** Receives distress data from the Android application, scrapes local databases to find the nearest *specialized* medical facility, and generates customized step-by-step first-aid instructions using the Gemini LLM.

### Request Body (JSON)
The Android app must send the payload in the following format (`Content-Type: application/json`):

```json
{
  "victim_name": "Jane Doe",
  "condition": "Severe venomous spider bite",
  "latitude": -1.0945,
  "longitude": 37.0142,
  "profile": {
    "age": "24",
    "allergies": "Penicillin",
    "chronic_conditions": "Asthma"
  }
}
```

### Success Response (200 OK)
Returns a unified response containing both the routing coordinates and the AI instructions.

```json
{
  "status": "success",
  "dispatch_data": {
    "victim_name": "Jane Doe",
    "emergency_type": "Severe venomous spider bite",
    "recommended_facility": {
      "name": "JKUAT Specialized Dispensary",
      "inventory": "anti-venom, epinephrine, asthma inhalers",
      "distance": "2.5 km away",
      "coordinates": {
        "lat": -1.1023,
        "lon": 37.0199
      }
    },
    "ai_first_aid_instructions": "• Keep Jane Doe completely still to slow the spread of venom.\n• Keep the bitten area below heart level.\n• Do NOT apply a tourniquet.\n• Monitor her breathing closely due to her Asthma condition."
  }
}
```

### Error Responses
*   **`404 Not Found`:** If the web scraper cannot locate any facilities within a safe radius.
*   **`500 Internal Server Error`:** If the LLM generation fails and the server is unable to fall back to the offline dictionary, or if there is a database/scraping critical failure.


***

### 2. Synchronization: Files We Have Completed So Far

Here is the exact state of what we have built together, mapped to your original file tree. You can use this to see exactly where my context is at before you paste your team's code.

**C++ / NDK Core (100% Complete for Phase 1)**
*   `frontend/cpp/gesture_processing.cpp` *(G-force math & triggers)*
*   `frontend/cpp/ble_mesh.cpp` *(BLE packet crafting & multi-hop logic)*
*   `frontend/cpp/wifi_direct.cpp` *(POSIX TCP sockets for Profile transfer)*
*   `frontend/cpp/native-lib.cpp` *(JNI Bridge)*
*   `frontend/cpp/CMakeLists.txt` *(Build instructions)*

**Java / Android Hardware Services (Partial)**
*   `frontend/java/com/aegismesh/sensors/GestureDetector.java` *(Accelerometer binding)*
*   `frontend/java/com/aegismesh/services/MeshService.java` *(BLE & Wi-Fi Direct binding)*
*   `frontend/java/com/aegismesh/activities/EmergencyActivity.java` *(Touch UI)*
*   `frontend/AndroidManifest.xml` *(Security permissions & wakelocks)*

**Python / FastAPI Backend (Completed Core Logic)**
*   `backend/requirements.txt`
*   `backend/app.py` *(Master Endpoint routing)*
*   `backend/scraper/hospital_scraper.py` *(HTML parsing & condition mapping)*
*   `ai/triage_engine.py` *(Gemini LLM integration & offline fallback)*
*   `backend/database/db.py` *(SQLite initialization engine)*
*   `database/schema.sql` *(Table definitions)*

**Documentation (Generated)**
*   `docs/cpp_architecture_summary.md`
*   `docs/java_services_architecture.md`
*   `docs/android_ui_manifest_architecture.md`
*   `docs/backend_ai_architecture.md`
*   `docs/api_documentation.md`

***

