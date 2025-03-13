# connect-android-app-with-esp32-using-Bluetooth
# Bluetooth Communication Between Android and ESP32

This project establishes a Bluetooth connection between an Android application and an ESP32 device using Serial Bluetooth Profile (SPP). The Android app scans for nearby Bluetooth devices, connects to the selected device, and enables bidirectional communication.

## Features
- Scan for available Bluetooth devices.
- Connect to an ESP32 device using Bluetooth.
- Send and receive messages between the Android app and ESP32.
- Display received messages in a toast message on Android.

## Technologies Used
- **Android (Java)**: Implements Bluetooth connectivity using `BluetoothAdapter`, `BluetoothDevice`, and `BluetoothSocket`.
- **ESP32 (C++)**: Uses `BluetoothSerial.h` to establish a Bluetooth serial communication channel.

## Android Application
### Dependencies
Ensure the following permissions are added to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```
### Key Components
- **Discovery & Pairing**:
  - Uses `BluetoothAdapter` to scan for devices.
  - Displays discovered devices in a `ListView`.
- **Connection Handling**:
  - Establishes a connection using `BluetoothSocket`.
  - Sends messages to the ESP32.
  - Listens for incoming messages.

### MainActivity.java
Handles Bluetooth setup, scanning, connection, and communication.
```java
// Start Bluetooth discovery
bluetoothAdapter.startDiscovery();

// Connect to selected device
bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
bluetoothSocket.connect();

// Send message
outputStream.write("Hello, Bluetooth Device!".getBytes());
```

## ESP32 Firmware
### Setup
Ensure your ESP32 is running the following code:
```cpp
#include "BluetoothSerial.h"

BluetoothSerial SerialBT;

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32test"); // Device name
  Serial.println("Bluetooth initialized!");
}

void loop() {
  if (Serial.available()) {
    SerialBT.write(Serial.read());
  }
  if (SerialBT.available()) {
    Serial.write(SerialBT.read());
  }
}
```
### Explanation
- Initializes Bluetooth using `SerialBT.begin()`.
- Reads and writes data between ESP32's serial interface and Bluetooth.

## How to Use
1. **Flash the ESP32 Code**:
   - Use the Arduino IDE or ESP-IDF to upload the code.
   - Open the Serial Monitor at `115200 baud`.
2. **Run the Android App**:
   - Install the APK or run the app from Android Studio.
   - Enable Bluetooth and scan for devices.
   - Select "ESP32test" to connect.
3. **Send & Receive Messages**:
   - Type messages on the Android app to send them to ESP32.
   - ESP32 echoes received messages back.

## Troubleshooting
- **Pairing Issues**: Ensure the device is discoverable.
- **Connection Fails**: Try restarting Bluetooth on both devices.
- **No Data Received**: Verify baud rate and ensure messages are being sent correctly.

## License
This project is licensed under the **Public Domain (CC0)**.

---

Enjoy coding! 🚀

