package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.bluetooth.BluetoothSocket;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity2 extends AppCompatActivity {
    private OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Get device name passed from MainActivity
        String deviceName = getIntent().getStringExtra("device_name");

        TextView textView = findViewById(R.id.textView);
        textView.setText("Connected to: " + deviceName);

        Button sendButton = findViewById(R.id.button);
        sendButton.setOnClickListener(v -> sendData("Relay_toggled\n"));

        // Try to get the output stream from the Bluetooth socket
        try {
            BluetoothSocket socket = BluetoothConnection.bluetoothSocket;

            if (socket != null && socket.isConnected()) {
                outputStream = socket.getOutputStream();
            } else {
                Toast.makeText(this, "Bluetooth is not connected", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error getting output stream: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Function to send data over Bluetooth
    private void sendData(String data) {
        if (outputStream != null) {
            try {
                outputStream.write(data.getBytes());
                Toast.makeText(this, "Sent: " + data, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to send: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Output stream is null", Toast.LENGTH_SHORT).show();
        }
    }
}
