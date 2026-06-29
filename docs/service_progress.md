# Services Overview (Aegis Mesh)

This document describes the background services operating within the Aegis Mesh application (`com.aegismesh.services`), detailing their individual responsibilities and how they interact to handle emergency situations.

There are three primary services implemented in the application:

## 1. LocationService
**Purpose:** Responsible for requesting and retrieving the current physical location coordinates (latitude, longitude, and accuracy) of the user.

**Key Operations:**
- Uses the Android `LocationManager` to access both GPS and Network providers.
- When a location request is made, it first attempts to retrieve the last known location. If the last known location is fresh (less than 30 seconds old), it returns immediately to ensure fast tracking in low-signal areas.
- If no fresh location is available, it requests a single asynchronous location update from the active provider.
- Handles permissions and gracefully returns errors if providers are disabled or unavailable.

## 2. MeshService
**Purpose:** Handles local peer-to-peer mesh networking (e.g., BLE Mesh / Wi-Fi Direct) when traditional cellular or internet connectivity is unavailable.

**Key Operations:**
- Operates on a dedicated background thread (`ExecutorService`) to prevent blocking the main UI thread during network operations.
- Exposes the `sendEmergencyOverMesh` method, which takes an `Emergency` object and simulates discovering nearby peer devices, establishing a relay connection, and relaying the emergency packet.
- Returns success or failure callbacks to monitor transmission outcomes over the mesh network.

## 3. SOSService
**Purpose:** The central coordinator responsible for managing the entire emergency event lifecycle.

**Key Operations:**
- **Coordination:** It dynamically binds to both the `LocationService` and `MeshService` to orchestrate an SOS event.
- **Trigger Handling:** Accepts SOS triggers via intent actions (Manual, Gesture, Fall, Voice), sanitizes the input, and initiates the workflow in a foreground service (showing a persistent notification to the user).
- **Location Acquisition:** Communicates with `LocationService` to get the latest coordinates, allowing up to 10 seconds for the operation before falling back to default coordinates.
- **Network Failover & Transmission:**
  - **Internet First:** Checks if an active internet connection is available. If so, it attempts to send the emergency via `ApiClient` with an exponential retry mechanism (up to 3 attempts).
  - **Mesh Fallback:** If there is no internet, or if all API retries fail, it saves the event locally using `EmergencyDbHelper` and delegates the transmission to `MeshService`.
- **Dynamic Recovery:** Registers a `ConnectivityManager.NetworkCallback` to listen for internet restoration in real-time. If the internet comes back online, it immediately queries the local database for unsent emergencies and resends them.
- **Background Backup:** Schedules a periodic WorkManager task (`EmergencyResendWorker`) every 15 minutes to ensure any stuck packets are eventually recovered and delivered when network constraints are met.
