package com.example.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {
    private InputStream inputStream;
    private BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;
    private ListView listViewDevices;
    private ArrayList<String> devicesList;
    private ArrayAdapter<String> devicesAdapter;

    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice selectedDevice;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID
    private OutputStream outputStream;
    private Handler messageHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        listViewDevices = findViewById(R.id.listViewDevices);
        devicesList = new ArrayList<>();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, devicesList);
        listViewDevices.setAdapter(devicesAdapter);
        messageHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String message = (String) msg.obj;
                Toast.makeText(MainActivity.this, "Received message: " + message, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        // Check if Bluetooth is supported
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            return;
        }

        // Enable Bluetooth if not already enabled
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Button to start discovery
        Button startDiscoveryButton = findViewById(R.id.btnStartDiscovery);
        startDiscoveryButton.setOnClickListener(v -> discoverBluetoothDevices());

        // Set item click listener for ListView
        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = devicesList.get(position);
            String deviceAddress = deviceInfo.substring(deviceInfo.length() - 18); // Extract MAC address
            try {
                selectedDevice = bluetoothAdapter.getRemoteDevice(deviceAddress.substring(0, deviceAddress.length() - 1));
                connectToDevice();
            } catch (IllegalArgumentException e) {
                Log.e("BluetoothAddress", "Invalid Bluetooth address: " + deviceAddress);
                Toast.makeText(MainActivity.this, "Invalid Bluetooth address", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void discoverBluetoothDevices() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        bluetoothAdapter.startDiscovery();

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get Bluetooth device from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Get the device's name and address
                String deviceName = device.getName();
                String deviceAddress = device.getAddress();

                // Add to the list if it's not already there
                if (!devicesList.contains(deviceName + " (" + deviceAddress + ")")) {
                    devicesList.add(deviceName + " (" + deviceAddress + ")");
                    devicesAdapter.notifyDataSetChanged();
                }
            }
        }
    };

    // Connect to the selected Bluetooth device
    private void connectToDevice() {
        if (selectedDevice == null) {
            Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();

                // Get the OutputStream from the BluetoothSocket
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                listenForMessages();
                // If the connection is successful, send a message
                sendMessage("Hello, Bluetooth Device!");

                // If the connection is successful, display a toast
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Connected to " + selectedDevice.getName(), Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to connect: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // Send a message to the connected Bluetooth device
    private void sendMessage(String message) {
        if (outputStream != null) {
            try {
                outputStream.write(message.getBytes());
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Message sent: " + message, Toast.LENGTH_SHORT).show());
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }
    }
    private void listenForMessages() {
        new Thread(() -> {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        byteArrayOutputStream.write(buffer, 0, bytes);
                        String receivedData = byteArrayOutputStream.toString("UTF-8");

                        // If the message contains a newline, process it
                        if (receivedData.contains("\n")) {
                            final String completeMessage = receivedData.trim();
                            byteArrayOutputStream.reset(); // Clear buffer after processing

                            // Send received message to handler to display it on the UI
                            if (messageHandler != null) {
                                Message message = messageHandler.obtainMessage(0, completeMessage);
                                messageHandler.sendMessage(message);
                            }
                        }
                    }
                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error receiving message: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    break;
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver); // Unregister the receiver

        // Close the Bluetooth socket if it's open
        if (bluetoothSocket != null) {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
