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
