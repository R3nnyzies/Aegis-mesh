***

# Aegis Mesh: Java Interface & Hardware Services
### Phase 1: The "Java Muscle" Documentation

## Overview
While the Native C++ layer acts as the "Brain" of Aegis Mesh—handling strict networking protocols and complex mathematical logic—Android's security and hardware sandboxing require all physical hardware requests to go through the Java Virtual Machine (JVM). 

The files `GestureDetector.java` and `MeshService.java` represent the "Muscle" of the application. They are responsible for turning on the physical antennas, reading raw data from the physical chips, and passing that data into the C++ layer using the Java Native Interface (JNI).

---

## 1. The Sensor Bridge (`GestureDetector.java`)
**Purpose:** To manage the device's physical IMU (Inertial Measurement Unit) and act as a high-speed data pipeline between Android's sensor hardware and the C++ gesture engine.

### Core Mechanics
* **JNI Integration (`System.loadLibrary`):** The class immediately loads the compiled `aegismesh-native.so` C++ library into memory. It defines `native` method signatures that link directly to the C++ functions we wrote earlier.
* **High-Frequency Polling (`SENSOR_DELAY_GAME`):** We register the `SensorManager` to listen to the default Accelerometer. By using `SENSOR_DELAY_GAME` (roughly 50 frames per second), we ensure the data pipeline is fast enough to accurately plot the G-force curve of a violent shake, preventing dropped data points.
* **The Data Pipeline (`onSensorChanged`):** Every time the hardware chip detects movement, Java captures the X, Y, and Z float values. Instead of doing math in Java, it immediately streams these values into `nativeProcessAccelerometer()`. 
* **The Observer Pattern (`GestureListener`):** The class utilizes an Interface callback. The UI (Activity) implements this interface. When the C++ engine finally returns `true` (meaning the 3-shake condition is met), Java fires `onSosTriggered()`, instantly notifying the UI layer to launch the emergency protocol without polling.

---

## 2. The Network Bridge (`MeshService.java`)
**Purpose:** An Android Background Service that controls the Bluetooth Low Energy (BLE) radio. It handles the transmission of the SOS "Flare" and continuously listens for other nearby victims, utilizing C++ to determine routing logic.

### Core Mechanics
* **Background Execution (`Service`):** By extending Android's `Service` class, this code is decoupled from the user interface. This ensures that even if the user minimizes the app or turns off the screen, the phone continues to listen for mesh network SOS signals.
* **App-Specific Filtering (`ParcelUuid`):** The Bluetooth space is incredibly noisy (headphones, smartwatches, etc.). We defined a custom UUID (`0000180F-0000-1000-8000-00805f9b34fb`). Android's hardware BLE scanner uses this to filter out junk packets at the hardware level, ensuring our app only wakes up when it detects an Aegis Mesh SOS.

* **Broadcasting Protocol (The Originator):**
  1. When an SOS is triggered, Java asks C++ for the payload via `nativeBuildSosPacket()`.
  2. Java packages this byte array into an `AdvertiseData` object.
  3. The `BluetoothLeAdvertiser` is set to `ADVERTISE_TX_POWER_HIGH` (maximum radio strength for maximum physical range) and starts broadcasting the payload.

* **Scanning & Multi-Hop Protocol (The Mesh Relay):**
  1. The `BluetoothLeScanner` continuously listens. When it catches a packet matching our UUID, it extracts the raw bytes.
  2. Java passes these bytes into C++ via `nativeProcessIncomingPacket()`.
  3. C++ analyzes the routing table and hop count. If C++ returns a *new* byte array, Java takes that array and immediately pushes it back to the `BluetoothLeAdvertiser`. This is the core of the decentralized multi-hop network.

---

### Integration Summary
This architecture creates a highly efficient, closed-loop system. The physical sensors (Java) feed the decision engine (C++). When the decision engine triggers an SOS, it notifies the UI (Java), which then commands the networking service (Java) to pull data packets from the routing engine (C++) and blast them out of the Bluetooth antenna.

***

