
# 📱 connect-android-app-with-esp32-using-Bluetooth  
# 🔌 Bluetooth Communication Between Android and ESP32

This project establishes Bluetooth communication between an **Android app** and an **ESP32 board** using the **Serial Port Profile (SPP)**. The Android app scans for available Bluetooth devices, connects to the ESP32, and allows toggling an LED using a simple command.

---

## ✨ Features

- Scan and display nearby Bluetooth devices on Android.
- Connect to ESP32 via Bluetooth SPP.
- Send a `"Relay_toggled"` command to toggle an LED.
- ESP32 responds and logs command via Serial Monitor.
- Display responses as toasts in Android app.

---

## 🧰 Technologies Used

- **Android (Java)**: Uses `BluetoothAdapter`, `BluetoothDevice`, and `BluetoothSocket`.
- **ESP32 (Arduino C++)**: Uses `BluetoothSerial.h` to handle Bluetooth SPP communication and GPIO control.

---

## 📱 Android Application

### 🔐 Permissions (`AndroidManifest.xml`)

```xml
<uses-permission android:name="android.permission.BLUETOOTH"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
```

### 🧩 Functionality

- Discover nearby Bluetooth devices.
- Connect to `ESP32test` device.
- Send `"Relay_toggled"` command on button press.
- Display message toast and receive responses.

---

### 🧪 Example Java Snippet

```java
bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
bluetoothSocket.connect();

outputStream.write("Relay_toggled\n".getBytes());
```

---

## 🔌 ESP32 Firmware

### ✅ Updated Arduino Code

```cpp
#include "BluetoothSerial.h"

#if !defined(CONFIG_BT_ENABLED) || !defined(CONFIG_BLUEDROID_ENABLED)
#error Bluetooth is not enabled! Please enable it in the configuration
#endif

BluetoothSerial SerialBT;

const int ledPin = 2; // GPIO 2 (often built-in LED)

void setup() {
  Serial.begin(115200);
  SerialBT.begin("ESP32test"); // Bluetooth device name
  pinMode(ledPin, OUTPUT);

  Serial.println("Bluetooth device started. Waiting for commands...");
}

void loop() {
  if (SerialBT.available()) {
    String command = SerialBT.readStringUntil('\n');
    command.trim();

    Serial.println("Received: " + command);

    if (command.equalsIgnoreCase("Relay_toggled")) {
      digitalWrite(ledPin, !digitalRead(ledPin));  // toggle LED
      Serial.println("LED toggled");
    }
  }

  delay(20);
}
```

---

## 🛠 Troubleshooting

| Issue              | Solution                                                       |
|-------------------|----------------------------------------------------------------|
| Device not found   | Check ESP32 power and Bluetooth availability                   |
| Can't connect      | Try restarting ESP32 or unpairing and re-pairing on Android    |
| LED not toggling   | Check wiring and GPIO pin used                                 |

---

## 📄 License

This project is licensed under the **Public Domain (CC0)**.

---

**Enjoy your Bluetooth-powered control project!** 💡🔗📲
