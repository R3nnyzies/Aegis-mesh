Here is a comprehensive documentation markdown summarizing the architectural decisions, logic, and code we have built so far in the C++ layer.

***

# Aegis Mesh: Native C++ Core Engine
### Phase 1: Hardware Interfacing & Low-Level Networking Documentation

## Overview
As part of **Phase 1** of the Aegis Mesh project, the core processing engines for hardware sensors and localized mesh networking have been implemented in C++ using the Android NDK. 

By pushing this logic down to the native C++ layer rather than keeping it in Java, we achieve three critical goals for an emergency application:
1. **Zero Garbage Collection Pauses:** Continuous high-frequency sensor polling won't cause memory bloat or UI stuttering.
2. **Battery Efficiency:** C++ math processing is highly optimized for background execution.
3. **Hardware-Level Socket Control:** POSIX networking allows for highly customized, low-overhead packet manipulation.

Here is an elaborate breakdown of the components developed so far:

---

## 1. The Gesture Engine (`gesture_processing.cpp`)
**Purpose:** To provide a reliable, false-positive-free method of triggering an SOS without requiring fine motor skills (navigating a touchscreen) during high-stress situations.

### Core Mechanics
* **State Verification (Two-Factor Activation):** To prevent accidental triggers if the user drops the phone or is running, the engine requires a combined action: *Holding a specific screen zone* while *shaking the phone*.
* **G-Force Math:** The engine calculates the raw magnitude of force using the Pythagorean theorem across the X, Y, and Z accelerometer axes: `sqrt(X² + Y² + Z²)`.
* **Debouncing & Time Windows:** The algorithm requires 3 distinct spikes of force (over 2.7 Gs). It includes a 500ms "debounce" timer between shakes to prevent a single continuous shake from registering as multiple shakes. The user must complete the 3 shakes within a strict 3-second window, otherwise, the counter resets.

---

## 2. The BLE Mesh Routing Protocol (`ble_mesh.cpp`)
**Purpose:** To establish a fast, decentralized "flare" system using Bluetooth Low Energy (BLE). Because Android restricts direct radio access via C++, this acts as the "Brain" that processes payloads given to it by Java.

### Core Mechanics
* **Strict Memory Packing:** BLE advertising packets are limited to roughly 31 bytes. The engine utilizes C-style struct packing (`#pragma pack(push, 1)`) to squeeze maximum data into this limit:
  * `4 bytes`: Unique Message ID
  * `1 byte`: Hop Count (Time-to-Live)
  * `10 bytes`: Victim Name (e.g., "John")
  * `16 bytes`: Primary Medical Condition (e.g., "Seizures")
* **Multi-Hop Routing (Mesh Logic):** When an incoming packet is passed from Java to C++, the engine checks the `Hop Count`. If it is under the maximum limit (e.g., 5 hops), it increments the counter and passes it back to Java to be rebroadcast, extending the physical range of the SOS.
* **Loop Prevention:** To prevent a "broadcast storm" where two nearby phones infinitely bounce the same message back and forth, the engine maintains a `std::vector` routing table of seen Message IDs. If a packet arrives that the phone has already processed, it is immediately dropped.

---

## 3. Wi-Fi Direct High-Bandwidth Sockets (`wifi_direct.cpp`)
**Purpose:** While BLE serves as the initial emergency flare, it cannot hold a full medical profile. Once devices discover each other via BLE, Java establishes a Wi-Fi Direct connection, and this C++ engine handles the heavy data transfer using POSIX TCP Sockets.

### Core Mechanics
* **TCP Client/Server Architecture:** 
  * **Server Mode:** Opens a POSIX socket bound to a specific port and listens (`accept()`). When a responder requests data, it receives the payload reliably.
  * **Client Mode:** Uses `inet_pton` and `connect()` to target a responder's direct IP address. It then blasts the full JSON user profile (allergies, exact geolocation coordinates, emergency contacts, and AI pre-computation data) across the local network.
* **Why POSIX Sockets?** By bypassing Java's higher-level network libraries (like HttpURLConnection or OkHttp), the data transfer happens with absolute minimal overhead, reducing latency when every second counts.

---

## 4. The JNI Bridge (`native-lib.cpp` & `CMakeLists.txt`)
**Purpose:** To create the interoperability layer between Android's Java Virtual Machine (JVM) and our native C++ binaries.

### Core Mechanics
* **Memory Management:** The bridge safely translates Java datatypes (like `jstring` and `jbyteArray`) into C-style arrays (`const char*` and `uint8_t*`). It specifically ensures that memory allocated by the JVM is released after the C++ functions are finished (`ReleaseStringUTFChars`, `ReleaseByteArrayElements`) to prevent memory leaks during background execution.
* **CMake Build Configuration:** Configures the Android NDK compiler to bundle all the `.cpp` files into a single, dynamically linked shared library (`libaegismesh-native.so`), linking against standard Android native libraries for logging (`<android/log.h>`).

---

### Next Phase Transition
With the **Native Core Engine** complete, the architecture is ready to interface with the Android Framework. The next steps involve moving to the `frontend/java` layer to implement `GestureDetector.java` and `MeshService.java`, which will bind the physical hardware (Bluetooth/Wi-Fi antennas and physical sensors) to these C++ algorithms.