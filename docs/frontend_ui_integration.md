***
# Aegis Mesh: Frontend UI & Network Integration
### Phase 1 & 2: App Navigation, Data Models, and API Connectivity

## Overview
This document outlines the final pieces of the Android frontend architecture. It covers the user-facing dashboard, the secure collection of medical data, the data models used to parse backend responses, and the modern Java networking layer that bridges the Android app to the FastAPI/AI backend.

---

## 1. Authentication & Dashboard Navigation
**Files:** `LoginActivity.java`, `HomeActivity.java`

### Core Mechanics
* **Zero-Friction Emergency Access:** In a crisis, a user cannot be expected to remember passwords. `LoginActivity` utilizes Android `SharedPreferences` to persist the user's session. Once logged in, subsequent app launches bypass the login screen entirely, dropping the user directly into the active dashboard.
* **Proactive Permission Management:** Android 12+ enforces strict runtime permissions for Bluetooth (BLE) and Location. `HomeActivity` executes a `checkPermissions()` routine the moment the dashboard loads. By requesting `BLUETOOTH_ADVERTISE`, `BLUETOOTH_SCAN`, and `ACCESS_FINE_LOCATION` *before* an emergency occurs, we prevent the OS from crashing the app or blocking the mesh broadcast during an actual SOS event.
* **Profile Readiness Checks:** The dashboard dynamically updates its UI (`onResume`) to warn the user if their medical profile is empty, ensuring the AI triage engine has data to work with before a crisis happens.

---

## 2. Medical Data Collection
**File:** `ProfileActivity.java`

### Core Mechanics
* **Persistent Local Storage:** Medical data (Allergies, Chronic Conditions, Age) is highly sensitive and critical for offline mesh networking. This Activity saves the data directly to the device's hardware storage using `SharedPreferences`.
* **AI Contextualization:** If the user leaves fields blank, the system defaults to strict strings like `"None known"` or `"Unknown"`. This prevents the Gemini LLM from hallucinating medical conditions when generating first-aid steps.
* **Global Access:** It provides a static utility method (`getSavedUser()`) allowing the background networking service and the UI to instantly pull the victim's profile into memory the exact second the C++ gesture engine triggers an SOS.

---

## 3. Data Models & API Payload Structuring
**Files:** `models/User.java`, `models/Hospital.java`, `models/Emergency.java`

### Core Mechanics
* **Nested JSON Construction:** To satisfy the FastAPI backend requirement, the `Emergency` model was updated. Instead of sending a flat JSON string, it now accepts the `User` object and nests it under a `"profile"` key. This packages the GPS coordinates, the emergency type, and the medical history into one single payload.
* **Hospital Parsing:** The new `Hospital` model acts as a deserializer. When the Python web scraper returns the recommended specialized clinic, this model extracts the coordinates, name, and distance from the JSON response so the Android UI can plot it or display it to the responder.

---

## 4. Modern Network Client
**File:** `network/ApiClient.java`

### Core Mechanics
* **Strict URI Parsing (Java 20+ Compliance):** The `java.net.URL(String)` constructor is deprecated in modern Java due to security and parsing vulnerabilities. The client was updated to construct a `java.net.URI` first, ensuring the string strictly conforms to RFC 2396 standards before opening the `HttpURLConnection`.
* **Background Threading Integration:** All HTTP `POST` requests block the thread they run on. The client is designed to be wrapped in a background `Thread` or `ExecutorService` when called from `EmergencyActivity`, guaranteeing the UI (like the "SOS Triggered" animation) never freezes while waiting for the AI to generate a response.
* **Dual-Response Handling:** Upon receiving a `200 OK` from the Master Dispatch endpoint, the client extracts *both* the AI First-Aid instructions and the Dynamic Hospital Routing data simultaneously, completing the loop from the distress signal to actionable medical advice.

***
