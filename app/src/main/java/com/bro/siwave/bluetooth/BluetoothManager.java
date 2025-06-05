package com.bro.siwave.bluetooth;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {

    private static final String TAG = "BluetoothManager";
    private static final String TARGET_NAME = "HC-05"; // Passe bei Bedarf an
    private static final String TARGET_MAC = "FC:A8:9A:00:03:E7"; // Optional
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final Context context;
    private final Listener listener;

    private BluetoothSocket socket;
    private OutputStream output;

    public interface Listener {
        void onConnected();
        void onDisconnected(String reason);
    }

    public BluetoothManager(Context context, Listener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
    }

    public void connect() {
        new Thread(() -> {
            try {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                if (adapter == null) {
                    notifyError("Bluetooth nicht verfügbar");
                    return;
                }

                if (!adapter.isEnabled()) {
                    notifyError("Bluetooth ist deaktiviert");
                    return;
                }

                // Android 12+: Berechtigung prüfen
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                            != PackageManager.PERMISSION_GRANTED) {
                        notifyError("Bluetooth CONNECT-Berechtigung fehlt");
                        return;
                    }
                }

                BluetoothDevice target = findDevice(adapter.getBondedDevices());
                if (target == null) {
                    notifyError("Zielgerät nicht gefunden");
                    return;
                }

                socket = target.createRfcommSocketToServiceRecord(SPP_UUID);
                socket.connect();
                output = socket.getOutputStream();

                Log.d(TAG, "Verbindung hergestellt mit: " + target.getName());
                if (listener != null) listener.onConnected();

            } catch (IOException e) {
                notifyError("Verbindung fehlgeschlagen: " + e.getMessage());
                close();
            }
        }).start();
    }

    private BluetoothDevice findDevice(Set<BluetoothDevice> devices) {
        for (BluetoothDevice dev : devices) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            Log.d(TAG, "Gefunden: " + dev.getName() + " - " + dev.getAddress());
            if (dev.getName() != null && dev.getName().equals(TARGET_NAME)) {
                return dev;
            }
            if (dev.getAddress() != null && dev.getAddress().equals(TARGET_MAC)) {
                return dev;
            }
        }
        return null;
    }

    public void send(String command) {
        if (!isConnected()) {
            Log.w(TAG, "Nicht verbunden, sende nicht: " + command);
            return;
        }

        try {
            output.write(command.getBytes());
            output.flush();
            Log.d(TAG, "Gesendet: " + command);
        } catch (IOException e) {
            Log.e(TAG, "Fehler beim Senden: " + e.getMessage());
            disconnect();
        }
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && output != null;
    }

    public void disconnect() {
        close();
        if (listener != null) listener.onDisconnected("Manuell getrennt");
    }

    public void reconnectIfNeeded() {
        if (!isConnected()) {
            Log.d(TAG, "Versuche Reconnect...");
            connect();
        }
    }

    private void notifyError(String msg) {
        Log.e(TAG, msg);
        if (listener != null) listener.onDisconnected(msg);
    }

    private void close() {
        try {
            if (output != null) output.close();
        } catch (IOException ignored) {
        }
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
        socket = null;
        output = null;
    }
}
