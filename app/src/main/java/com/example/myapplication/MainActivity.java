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

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends Activity {
    private BluetoothAdapter bluetoothAdapter;
    private final int REQUEST_ENABLE_BT = 1;
    private ListView listViewDevices;
    private ArrayList<String> devicesList;
    private ArrayAdapter<String> devicesAdapter;

    private BluetoothSocket bluetoothSocket;
    private BluetoothDevice selectedDevice;
    private final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // SPP UUID
    private InputStream inputStream;
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

        messageHandler = new Handler(msg -> {
            String message = (String) msg.obj;
            Toast.makeText(MainActivity.this, "Received message: " + message, Toast.LENGTH_SHORT).show();
            return true;
        });

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        Button startDiscoveryButton = findViewById(R.id.btnStartDiscovery);
        startDiscoveryButton.setOnClickListener(v -> discoverBluetoothDevices());

        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            String deviceInfo = devicesList.get(position);
            String deviceAddress = deviceInfo.substring(deviceInfo.length() - 18);
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
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null && device.getName() != null) {
                    String deviceInfo = device.getName() + " (" + device.getAddress() + ")";
                    if (!devicesList.contains(deviceInfo)) {
                        devicesList.add(deviceInfo);
                        devicesAdapter.notifyDataSetChanged();
                    }
                }
            }
        }
    };

    private void connectToDevice() {
        if (selectedDevice == null) {
            Toast.makeText(this, "No device selected", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
                bluetoothSocket.connect();

                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();

                BluetoothConnection.bluetoothSocket = bluetoothSocket; // 👉 Save for use in MainActivity2

                listenForMessages();

                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Connected to " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();

                    // Navigate to MainActivity2
                    Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                    intent.putExtra("device_name", selectedDevice.getName());
                    startActivity(intent);
                });

            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Connection failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    private void listenForMessages() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;
            StringBuilder receivedBuilder = new StringBuilder();

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        String incoming = new String(buffer, 0, bytes, "UTF-8");
                        receivedBuilder.append(incoming);

                        if (incoming.contains("\n")) {
                            final String fullMessage = receivedBuilder.toString().trim();
                            receivedBuilder.setLength(0); // Clear buffer

                            Message message = messageHandler.obtainMessage(0, fullMessage);
                            messageHandler.sendMessage(message);
                        }
                    }
                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show());
                    break;
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
        try {
            if (bluetoothSocket != null) bluetoothSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
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
