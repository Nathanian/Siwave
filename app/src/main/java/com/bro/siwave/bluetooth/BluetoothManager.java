package com.bro.siwave.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {
    public interface Listener {
        void onConnected();
        void onDisconnected(String reason);
    }

    private final String TARGET_NAME = "HC-05";
    private final String TARGET_MAC = "FC:A8:9A:00:03:E7";
    private final UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter adapter;
    private final Listener listener;

    private BluetoothSocket socket;
    private OutputStream outputStream;
    private boolean tryingToConnect = false;
    private boolean connected = false;

    public BluetoothManager(Listener listener) {
        this.adapter = BluetoothAdapter.getDefaultAdapter();
        this.listener = listener;
    }

    public void connect() {
        if (adapter == null || !adapter.isEnabled() || tryingToConnect) return;

        tryingToConnect = true;

        new Thread(() -> {
            try {
                BluetoothDevice target = findDevice();
                if (target == null) {
                    tryingToConnect = false;
                    listener.onDisconnected("Gerät nicht gefunden");
                    return;
                }

                socket = target.createRfcommSocketToServiceRecord(BT_UUID);
                socket.connect();
                outputStream = socket.getOutputStream();

                connected = true;
                tryingToConnect = false;
                listener.onConnected();
            } catch (Exception e) {
                tryingToConnect = false;
                disconnectInternal();
                listener.onDisconnected("Verbindungsfehler: " + e.getMessage());
            }
        }).start();
    }

    private BluetoothDevice findDevice() {
        Set<BluetoothDevice> paired = adapter.getBondedDevices();
        for (BluetoothDevice d : paired) {
            if (TARGET_NAME.equals(d.getName()) || TARGET_MAC.equals(d.getAddress())) {
                return d;
            }
        }
        return null;
    }

    public boolean send(String cmd) {
        if (!connected || outputStream == null) return false;
        try {
            outputStream.write(cmd.getBytes());
            return true;
        } catch (IOException e) {
            disconnectInternal();
            listener.onDisconnected("Senden fehlgeschlagen");
            return false;
        }
    }

    public void disconnect() {
        disconnectInternal();
    }

    private void disconnectInternal() {
        try {
            if (outputStream != null) outputStream.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
        outputStream = null;
        socket = null;
        connected = false;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isTryingToConnect() {
        return tryingToConnect;
    }

    public void reconnectIfNeeded() {
        if (!connected && !tryingToConnect) connect();
    }
}
