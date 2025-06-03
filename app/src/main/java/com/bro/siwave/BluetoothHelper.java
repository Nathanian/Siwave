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
import java.lang.reflect.Method;
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
    private boolean isConnected = false;

    private final String TARGET_DEVICE_NAME = "HC-05";
    private final String TARGET_MAC = "FC:A8:9A:00:03:E7";
    private final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public BluetoothHelper(Activity context, Runnable onConnected, Runnable onDisconnected) {
        this.context = context;
        this.onConnected = onConnected;
        this.onDisconnected = onDisconnected;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void connectBluetooth() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                context.requestPermissions(
                        new String[]{android.Manifest.permission.BLUETOOTH_CONNECT},
                        1001
                );
                showToast("Bluetooth-Rechte fehlen. Bitte erlauben.");
                log("BLUETOOTH_CONNECT wird angefragt.");
                return;
            }
        }

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled() || tryingToConnect) {
            log("Bluetooth nicht verfügbar oder bereits im Verbindungsversuch.");
            return;
        }

        tryingToConnect = true;

        new Thread(() -> {
            BluetoothDevice targetDevice = null;
            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            for (BluetoothDevice device : pairedDevices) {
                log("Gefundenes Gerät: " + device.getName() + " @ " + device.getAddress());
                if ((device.getName() != null && device.getName().equals(TARGET_DEVICE_NAME)) ||
                        device.getAddress().equals(TARGET_MAC)) {
                    targetDevice = device;
                    break;
                }
            }

            if (targetDevice == null) {
                try {
                    targetDevice = bluetoothAdapter.getRemoteDevice(TARGET_MAC);
                    log("Ungepairtes Gerät manuell geholt: " + targetDevice.getAddress());

                    Method method = targetDevice.getClass().getMethod("setPin", byte[].class);
                    method.invoke(targetDevice, new Object[]{"1234".getBytes()});
                    targetDevice.getClass().getMethod("createBond").invoke(targetDevice);
                    log("Pairing manuell gestartet mit PIN 1234");
                } catch (Exception ex) {
                    log("Fehler beim manuellen Pairing: " + ex.getMessage());
                    showToast("Gerät nicht gepairt und nicht gefunden.");
                    tryingToConnect = false;
                    return;
                }
            }

            try {
                socket = targetDevice.createRfcommSocketToServiceRecord(BT_UUID);
                socket.connect();
                SystemClock.sleep(200);
                outputStream = socket.getOutputStream();
                log("Bluetooth-Verbindung erfolgreich.");
                isConnected = true;
                context.runOnUiThread(onConnected);
            } catch (IOException e) {
                log("Verbindung fehlgeschlagen: " + e.getMessage());
                showToast("Bluetooth-Verbindung fehlgeschlagen.");
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
            isConnected = false;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    private void showToast(String text) {
        context.runOnUiThread(() -> Toast.makeText(context, text, Toast.LENGTH_SHORT).show());
    }

    private void log(String msg) {
        System.out.println("[BluetoothHelper] " + msg);
    }

    public void reconnectIfNeeded() {
        if (!isConnected && !tryingToConnect) {
            connectBluetooth();
        }
    }

    public boolean isTryingToConnect() {
        return tryingToConnect;
    }

    public BluetoothAdapter getAdapter() {
        return bluetoothAdapter;
    }
}
