***

# Aegis Mesh: User Interface & OS Configuration
### Phase 1: App Entry Point & Security Permissions Documentation

## Overview
The final layer of the frontend application ties the underlying native C++ algorithms and Java background services to the physical user interacting with the screen. It also handles the strict security sandbox enforced by the Android Operating System. 

This document outlines the architecture of `EmergencyActivity.java` (the UI logic) and `AndroidManifest.xml` (the OS rulebook).

---

## 1. The User Interface (`EmergencyActivity.java`)
**Purpose:** To provide a visual interface for the user, manage the hardware lifecycle to preserve battery, and act as the bridge between human interaction and the C++ processing engine.

### Core Mechanics
* **Two-Factor Hardware Trigger (Touch + Shake):** 
  To eliminate accidental SOS triggers, the UI enforces a two-step verification process using an `OnTouchListener`. 
  * `ACTION_DOWN`: When the user places their finger on the "Hold to Arm" button, Java immediately signals the C++ engine (`is_screen_held = true`), arming the accelerometer polling.
  * `ACTION_UP`: If the user lets go of the screen, the C++ engine is instantly disarmed, and the shake counter resets.
* **Lifecycle & Battery Management:** 
  Continuous 50Hz sensor polling drains battery quickly. The Activity uses Android's lifecycle callbacks to optimize this:
  * `onResume()`: Turns on the `SensorManager` when the app is actively on the screen.
  * `onPause()`: Unregisters the sensor when the app is minimized, preventing background battery drain when the app is not armed.
* **Asynchronous Callbacks (`runOnUiThread`):** 
  When the C++ engine detects a valid 3-shake pattern, it triggers the Java `onSosTriggered()` callback. Because this C++ callback originates on a background hardware thread, the Activity must use `runOnUiThread()` to safely update the text on the screen and fire UI alerts (like Toasts or haptic vibrations).
* **Service Decoupling (Intents):** 
  Once the SOS is triggered, the Activity packages the Victim's Name and Condition into an Android `Intent` and commands the OS to start the `MeshService`. By doing this, the actual Bluetooth networking is handed off to a background service—meaning even if the user closes the app UI, the mesh network continues to broadcast.

---

## 2. OS-Level Configuration (`AndroidManifest.xml`)
**Purpose:** To declare the strict permissions and hardware requirements needed to legally and technically bypass Android's security sandboxing for localized networking.

### Core Mechanics
* **Bluetooth & Wi-Fi Direct Permissions:** 
  Android strictly monitors applications that attempt to act as network routers. We explicitly declare permissions for Legacy Android (11 and below) and Granular permissions for Modern Android (12+), such as `BLUETOOTH_ADVERTISE`, `BLUETOOTH_SCAN`, and `NEARBY_WIFI_DEVICES`.
* **The Location Sandbox Quirk:** 
  We declare `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION`. While Aegis Mesh does not strictly need GPS satellites to form a local mesh, the Android OS mandates Location permissions for *any* app that scans for BLE or Wi-Fi networks. This is because hardware MAC addresses can theoretically be used to triangulate a user's physical location.
* **Background Execution & Wakelocks:** 
  To ensure the SOS mesh network doesn't die when the user's phone screen turns off, we declare `FOREGROUND_SERVICE` (giving the app high priority in the OS memory manager) and `WAKE_LOCK` (preventing the CPU from going to sleep while broadcasting).
* **Hardware Feature Requirements (`uses-feature`):** 
  We add tags declaring that `bluetooth_le`, `wifi.direct`, and `sensor.accelerometer` are mandatory. This ensures the Google Play Store prevents users with incompatible/old hardware from downloading an app that could fail them in an emergency.

---

### Phase 1 Completion Summary
With the completion of the UI and the Manifest, **Phase 1 (Hardware Interfacing & Device-to-Device Mesh)** is fully architected. 
1. The user touches the screen and shakes the phone.
2. The UI feeds this to the C++ engine.
3. C++ confirms the gesture.
4. Java takes the resulting payload and commands the physical antennas to broadcast and relay multi-hop packets via BLE and Wi-Fi Direct.

***
