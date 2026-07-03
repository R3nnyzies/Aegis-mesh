***

### The Documentation

Here is the comprehensive documentation for all three components we just built (The API Server, The Data Scraper, and The AI Engine).

***

# Aegis Mesh: Backend Logic & AI Triage
### Phases 2 & 3: Dynamic Routing and AI Assistance Documentation

## Overview
While the C++ and Java frontend handle immediate offline peer-to-peer communication, the **Python Backend** activates the moment a responder establishes an internet connection. 

This infrastructure is responsible for two major critical features outlined in the project blueprints: **Dynamic Resource Routing** (finding the exact clinic equipped to handle the specific emergency) and **AI-Assisted Triage** (guiding the responder through immediate first-aid).

---

## 1. The API Server (`backend/app.py`)
**Purpose:** To serve as the central, high-speed routing hub for the Aegis Mesh application once connectivity is achieved.

### Core Mechanics
* **FastAPI Framework:** Chosen for its extremely high performance and native asynchronous support. During a mass emergency or network spike, FastAPI can handle thousands of concurrent requests without dropping data.
* **Pydantic Data Validation:** By using `BaseModel`, the server strictly enforces the shape of the incoming payload (Victim Name, Condition, Latitude, Longitude). If a responder's app sends malformed data due to packet loss, the server handles it gracefully.
* **Single Point of Entry:** The `/api/v1/routing/find_facility` endpoint encapsulates the complexity of the backend. The mobile app makes one single `POST` request, and the server coordinates the scraping and mapping logic behind the scenes.

---

## 2. Dynamic Resource Scraper (`backend/scraper/hospital_scraper.py`)
**Purpose:** To solve the problem of traditional emergency routing, which blindly sends victims to the nearest general hospital—often resulting in fatalities if the hospital lacks specific resources (e.g., anti-venom or incubators).

### Core Mechanics
* **Web Scraping (BeautifulSoup):** Rather than relying on static, quickly-outdated databases, the scraper simulates reading live directory pages (like a local Ministry of Health portal). It parses HTML to extract real-time data, such as a clinic's current inventory.
* **Condition-to-Resource Mapping:** The `filter_specialized_clinics` algorithm applies logic gates to the victim's emergency. If the condition states "Spider Bite," the algorithm defines "anti-venom" as a strictly required keyword.
* **Intelligent Elimination:** The engine scans all nearby clinics. Even if a general hospital is 2.0 km away, if it lacks the required keyword, the algorithm drops it and routes the user to a specialized dispensary 2.5 km away that *does* have the required inventory, saving critical time.

---

## 3. AI Triage Engine (`ai/triage_engine.py`)
**Purpose:** To act as a digital 911 dispatcher, providing customized, step-by-step first-aid instructions tailored to both the emergency and the victim's personal medical history.

### Core Mechanics
* **LLM Integration (Google Gemini):** Uses a generative AI model to process complex medical scenarios on the fly. 
* **Prompt Engineering for Emergencies:** AI models naturally tend to be verbose. To make the output usable for a panicked responder looking at a mobile screen, the prompt uses strict constraints:
  1. Mandatory bullet points.
  2. Maximum of 4 actionable steps.
  3. Absolute restriction against long-winded medical jargon.
* **Profile Contextualization:** The AI injects the victim’s pre-shared mesh profile (Age, Allergies, Chronic Conditions) into the prompt. This prevents the AI from suggesting treatments that might trigger a secondary crisis (e.g., suggesting a medication the victim is highly allergic to).
* **Failsafe Offline Mechanism:** Because internet connections are unreliable during emergencies, the engine features a robust `get_fallback_instructions()` function. If the API fails, the server instantly returns hardcoded, medically-standard offline instructions based on keyword matching, ensuring the responder is never left without guidance.

***

### Project Status: What's Next?
We have successfully built the core mechanics for **Phase 1 (C++ Mesh/Hardware)**, **Phase 2 (Python Scraping & API)**, and **Phase 3 (AI Triage)**! 

The foundational architecture for the entire Aegis Mesh blueprint is now written. 

Would you like to:
1. Integrate the `triage_engine.py` into the `app.py` server so the Android app gets both the Hospital Route *and* the AI instructions in one single network request?
2. Write the SQLite Database schema (`database/schema.sql` / `db.py`) to permanently store the mapped hospitals and user profiles?