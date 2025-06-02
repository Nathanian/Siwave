// BluetoothHelper.java
package com.bro.siwave;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothHelper {
    private final Activity context;
    private final Runnable onConnected;
    private final Runnable onDisconnected;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private boolean tryingToConnect = false;

    private final String TARGET_DEVICE_NAME = "HC-05"; // Ändere bei Bedarf
    private final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothHelper(Activity context, Runnable onConnected, Runnable onDisconnected) {
        this.context = context;
        this.onConnected = onConnected;
        this.onDisconnected = onDisconnected;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connectBluetooth() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled() || tryingToConnect) {
            log("Bluetooth nicht verfügbar oder bereits im Verbindungsversuch.");
            return;
        }

        tryingToConnect = true;

        if (context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            context.runOnUiThread(() -> Toast.makeText(context, "Bluetooth-Rechte fehlen!", Toast.LENGTH_SHORT).show());
            tryingToConnect = false;
            return;
        }


        new Thread(() -> {
            BluetoothDevice targetDevice = null;
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                if (device.getName().equals(TARGET_DEVICE_NAME)) {
                    targetDevice = device;
                    break;
                }
            }

            if (targetDevice == null) {
                showToast("Bluetooth-Gerät nicht gefunden");
                log("Zielgerät nicht gefunden.");
                tryingToConnect = false;
                return;
            }

            try {
                socket = targetDevice.createRfcommSocketToServiceRecord(BT_UUID);
                socket.connect();
                outputStream = socket.getOutputStream();
                log("Bluetooth-Verbindung erfolgreich.");
                context.runOnUiThread(onConnected);
            } catch (IOException e) {
                log("Verbindung fehlgeschlagen: " + e.getMessage());
                closeConnection();
                context.runOnUiThread(onDisconnected);
            } finally {
                tryingToConnect = false;
            }
        }).start();
    }

    public boolean sendCommandToArduino(String command) {
        if (socket == null || outputStream == null) {
            log("Kann nicht senden – Socket oder OutputStream null");
            return false;
        }

        try {
            outputStream.write(command.getBytes());
            log("Gesendet: " + command);
            return true;
        } catch (IOException e) {
            log("Sende-Fehler: " + e.getMessage());
            closeConnection();
            context.runOnUiThread(onDisconnected);
            return false;
        }
    }

    public void closeConnection() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            log("Fehler beim Schließen: " + e.getMessage());
        } finally {
            socket = null;
            outputStream = null;
        }
    }

    private void showToast(String text) {
        context.runOnUiThread(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
    }

    private void log(String msg) {
        System.out.println("[BluetoothHelper] " + msg);
    }
}
