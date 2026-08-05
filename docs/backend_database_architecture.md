# Aegis Mesh: Database & Dynamic Routing Architecture
### Phase 2: Persistent Storage & GPS Calculation Documentation

## Overview
To transition from a mock prototype to a production-ready system, the backend was upgraded to include a relational SQLite database. This layer is responsible for tracking historical emergency events (for analytics) and caching specialized medical facility locations.

---

## 1. The Schema & Auto-Seeding (`db.py` & `schema.sql`)
**Core Mechanics:**
* **`emergencies` Table:** Acts as an immutable log. Every time `RoutingService` is triggered, it writes the Victim's Name, Coordinates, the AI instructions generated, and the hospital routed to.
* **CSV Auto-Seeder:** When the FastAPI server starts, it checks if `aegismesh.db` exists. If this is a fresh deployment, it automatically parses `database/hospitals.csv` and inserts the data into the SQL tables. This allows non-technical health administrators to update hospital inventory using Excel, and the backend absorbs it automatically.

---

## 2. Dynamic Routing Engine (`hospital_scraper.py`)
**Core Mechanics:**
* **Haversine Formula:** Replaced the mock distance strings. The engine now uses advanced trigonometry (`math.atan2`, `math.sin`) to calculate the exact curvature-of-the-earth distance between the Android device's GPS coordinates and every clinic in the database.
* **Intelligent Sorting:** After calculating distances, the array of clinics is sorted closest-to-furthest. The engine then iterates through the sorted array looking for specific substrings in the `known_inventory` column (e.g., `anti-venom`). The first match found is mathematically guaranteed to be the closest specialized clinic available.

## 3. Data Validation (`models.py`)
**Core Mechanics:**
* Utilizes Pydantic schemas (`EmergencyLogModel`) to ensure strict typing before any data touches the SQL queries, preventing SQL Injection vulnerabilities and data corruption.
