
---

## 📂 Codebase Details

The following modules have been implemented within the `frontend/java/com/aegismesh` directories:

### 1. Central Coordinator: `SOSService.java`
* **Foreground Service**: Starts as a sticky Android foreground service displaying active notifications to prevent OS termination.
* **Notification management**: Updates notification messages in real-time (`Sending alert...`, `Internet unavailable. Using mesh network.`, `Delivered successfully.`).
* **Service Binding**: Binds asynchronously to `LocationService` and `MeshService` using thread-safe synchronization locks and `CountDownLatch` barriers.
* **Input Validation**: Sanitizes and validates user IDs, triggers, emergency types, and notes to protect against buffer overflows or injection attempts.

### 2. Network Interface: `ApiClient.java`
* **Configurable Base URL**: Exposes standard endpoints derived dynamically from `BuildConfig.BASE_URL` to easily switch environments (e.g., local emulator `http://10.0.2.2:8000/`, physical dev networks, or production `https://api.aegismesh.com/`).
* **HttpURLConnection Implementation**: Avoids external HTTP library dependencies, using native Java connection handlers with 10-second connect/read timeouts.

### 3. Local Cache: `EmergencyDbHelper.java`
* **SQLite Persistence**: Manages the local SQLite database (`aegis_mesh.db`) to log unsent alerts.
* **Status tracking**: Tracks emergency alert states using constant tags: `PENDING`, `SENT`, `DELIVERED`, and `FAILED`.

### 4. Dynamic Recovery: `ConnectivityManager.NetworkCallback`
* Registered dynamically within `SOSService` to monitor the device's internet connection state in real-time.
* Triggered immediately upon connection recovery to query the local SQLite database and resend pending/failed emergencies without power-intensive background polling.

### 5. Eventual Recovery: `EmergencyResendWorker.java` (WorkManager)
* Implements a background worker task scheduled to run every 15 minutes when constraints (`NetworkType.CONNECTED`) are met.
* Resolves edge cases where the application is terminated, or the device is rebooted, ensuring that unsent reports are eventually synchronized.

### 6. Supporting Bound Services: `LocationService.java` & `MeshService.java`
* **`LocationService`**: Safe asynchronous LocationManager updates that fetch location coordinates, handling permission checks and disabled GPS.
* **`MeshService`**: Simulates BLE / Wi-Fi Direct peer searches and relays packets between device chains.

---

## 🔄 Transmission Failover & Recovery Flow

```mermaid
graph TD
    A[SOS Triggered] --> B{Internet Available?}
    B -- Yes --> C[ApiClient HTTP POST /emergency]
    C -- Success --> D[Mark DELIVERED & Update UI]
    C -- Fail / Timeout --> E[Save to SQLite DB - STATUS_FAILED]
    B -- No --> E
    E --> F[Initiate MeshService P2P Relay]
    
    E --> G[Recovery Tasks]
    G --> H[ConnectivityManager.NetworkCallback]
    G --> I[Periodic WorkManager Worker]
    
    H -- Connection Restored --> J[Retrieve Unsent from DB]
    I -- Connection Restored --> J
    J --> K[Resend via ApiClient]
    K -- Success --> L[Update Database Status to DELIVERED]
