package com.bro.siwave.bluetooth;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;

public class BluetoothConnector {
    private final Activity activity;
    private final BluetoothManager manager;

    public BluetoothConnector(Activity activity, BluetoothManager.Listener listener) {
        this.activity = activity;
        this.manager = new BluetoothManager(new BluetoothManager.Listener() {
            @Override
            public void onConnected() {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Bluetooth verbunden", Toast.LENGTH_SHORT).show());
                listener.onConnected();
            }

            @Override
            public void onDisconnected(String reason) {
                activity.runOnUiThread(() -> Toast.makeText(activity, "Bluetooth getrennt: " + reason, Toast.LENGTH_LONG).show());
                listener.onDisconnected(reason);
            }
        });
    }

    public void connect() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
                activity.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{android.Manifest.permission.BLUETOOTH_CONNECT}, 1001);
            Toast.makeText(activity, "Bluetooth-Rechte fehlen", Toast.LENGTH_SHORT).show();
            return;
        }
        manager.connect();
    }

    public boolean send(String cmd) {
        return manager.send(cmd);
    }

    public void disconnect() {
        manager.disconnect();
    }

    public void reconnectIfNeeded() {
        manager.reconnectIfNeeded();
    }

    public boolean isConnected() {
        return manager.isConnected();
    }

    public boolean isTryingToConnect() {
        return manager.isTryingToConnect();
    }

    public BluetoothManager getManager() {
        return manager;
    }
}
