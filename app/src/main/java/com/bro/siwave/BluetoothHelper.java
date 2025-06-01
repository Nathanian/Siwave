// BluetoothHelper.java
package com.bro.siwave;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {

    private BluetoothAdapter adapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private Context context;

    private Runnable onConnectedCallback;
    private Runnable onDisconnectedCallback;

    public BluetoothHelper(Context context, Runnable onConnectedCallback, Runnable onDisconnectedCallback) {
        this.context = context;
        this.onConnectedCallback = onConnectedCallback;
        this.onDisconnectedCallback = onDisconnectedCallback;
        adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connectBluetooth() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                    1001);
            return;
        }

        new Thread(() -> {
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            for (BluetoothDevice device : devices) {
                if (device.getName().startsWith("HC")) {
                    try {
                        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                        socket.connect();
                        outputStream = socket.getOutputStream();
                        runOnUi(() -> Toast.makeText(context, "Verbunden mit: " + device.getName(), Toast.LENGTH_SHORT).show());
                        if (onConnectedCallback != null) onConnectedCallback.run();
                    } catch (IOException e) {
                        runOnUi(() -> Toast.makeText(context, "Bluetooth-Verbindung fehlgeschlagen", Toast.LENGTH_SHORT).show());
                        if (onDisconnectedCallback != null) onDisconnectedCallback.run();
                    }
                    break;
                }
            }
        }).start();
    }

    public void sendCommandToArduino(String command) {
        if (outputStream == null) {
            runOnUi(() -> Toast.makeText(context, "Kein OutputStream verfügbar", Toast.LENGTH_SHORT).show());
            return;
        }
        try {
            outputStream.write(command.getBytes());
        } catch (IOException e) {
            runOnUi(() -> Toast.makeText(context, "Senden fehlgeschlagen", Toast.LENGTH_SHORT).show());
            if (onDisconnectedCallback != null) onDisconnectedCallback.run();
        }
    }

    private void runOnUi(Runnable action) {
        new Handler(Looper.getMainLooper()).post(action);
    }
}
