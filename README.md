
# 📱 connect-android-app-with-esp32-using-Bluetooth  
# 🔌 Bluetooth Communication Between Android and ESP32

This project creates a Bluetooth communication channel between an **Android application** and an **ESP32 device** using the **Serial Port Profile (SPP)**. The Android app scans for nearby Bluetooth devices, connects to the selected ESP32 device, and sends messages to control a GPIO pin (such as an LED or relay).

---

## ✨ Features

- Scan and display nearby Bluetooth devices.
- Connect to ESP32 using classic Bluetooth SPP.
- Send commands from Android to control GPIO.
- Toggle the LED or relay connected to ESP32 using a button.
- Show received messages as toast notifications in Android.

---

## 🧰 Technologies Used

- **Android (Java)**:  
  Bluetooth communication using `BluetoothAdapter`, `BluetoothDevice`, `BluetoothSocket`, and Java I/O streams.
- **ESP32 (Arduino C++)**:  
  Uses `BluetoothSerial.h` to communicate over SPP and control GPIO pins.

---

## 📱 Android Application

### 📋 Permissions

Ensure the following permissions are included in your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### 🧩 Key Components

- **Discovery & Pairing**:  
  Uses `BluetoothAdapter` to search for nearby devices and show them in a `ListView`.

- **Connection Handling**:  
  Uses `BluetoothSocket` to establish connection and communicate using input/output streams.

- **Communication**:  
  - Sends predefined strings (e.g., `Relay_ON`) on button click.  
  - Displays received data as a toast.

---

### 🔑 Sample Code Snippets

#### Start Discovery

```java
bluetoothAdapter.startDiscovery();
```

#### Connect to Device

```java
bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
bluetoothSocket.connect();
```

#### Send Message

```java
outputStream.write("Relay_ON\n".getBytes());
```

---

## 🔌 ESP32 Firmware

### ✅ Arduino Code

```cpp
#include "BluetoothSerial.h"

BluetoothSerial SerialBT;
const int ledPin = 2; // Built-in LED or relay pin

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32test"); // Device name
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Serial.println("Bluetooth initialized and ready.");
}

void loop() {
  if (SerialBT.available()) {
    String command = SerialBT.readStringUntil('\n');
    command.trim();

    Serial.println("Received: " + command);

    if (command.equalsIgnoreCase("Relay_ON")) {
      digitalWrite(ledPin, !digitalRead(ledPin)); // Toggle LED or relay
      Serial.println("Output toggled");
    } else if (command.equalsIgnoreCase("Relay_OFF")) {
      digitalWrite(ledPin, LOW);
      Serial.println("Output turned OFF");
    } else if (command.equalsIgnoreCase("Relay_ON_FIXED")) {
      digitalWrite(ledPin, HIGH);
      Serial.println("Output turned ON");
    } else {
      Serial.println("Unknown command");
    }
  }

  delay(20);
}
```

---

## 🧪 How to Use

1. **Flash the ESP32 Code**:
   - Upload the sketch using Arduino IDE.
   - Open Serial Monitor at 115200 baud to debug.

2. **Run the Android App**:
   - Install the APK or run from Android Studio.
   - Enable Bluetooth and Location.
   - Tap **Scan**, and select **ESP32test** to connect.

3. **Control ESP32**:
   - Tap the button in the app to send `"Relay_ON"` or similar commands.
   - The ESP32 toggles or sets the output pin.

---

## 🛠 Troubleshooting

| Issue                 | Solution                                                     |
|----------------------|--------------------------------------------------------------|
| Device not found      | Ensure ESP32 is powered and in range                        |
| Can’t connect         | Try unpairing, restarting Bluetooth, or resetting ESP32     |
| No output on pin      | Check wiring, correct GPIO number, and serial output logs   |
| No response on app    | Ensure you send newline `\n` and match command strings     |

---

## 📄 License

This project is licensed under the **Public Domain (CC0)** — completely free to use, modify, and distribute.

---

## 💡 Future Enhancements

- Control multiple GPIOs (`Relay1`, `Relay2`, etc.)
- Show real-time relay status on Android UI
- Add disconnect and reconnect functionality
- Receive messages and display them in a console/log view

---

**Enjoy building your wireless controller!** 💡📲💻
