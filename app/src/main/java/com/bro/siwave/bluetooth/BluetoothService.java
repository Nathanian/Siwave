package com.bro.siwave.bluetooth;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bro.siwave.R;

public class BluetoothService extends Service {

    public static final String CHANNEL_ID = "siwave_bluetooth_channel";
    public static final int NOTIFICATION_ID = 2;
    private static BluetoothManager bluetoothManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("BluetoothService", "BluetoothService gestartet");

        // NEU: NotificationChannel erstellen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bluetooth Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        bluetoothManager = new BluetoothManager(getApplicationContext(), new BluetoothManager.Listener() {
            @Override
            public void onConnected() {
                Log.d("BluetoothService", "Bluetooth verbunden!");
            }

            @Override
            public void onDisconnected(String reason) {
                Log.w("BluetoothService", "Verbindung verloren: " + reason);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    Log.d("BluetoothService", "Versuche Reconnect...");
                    bluetoothManager.reconnectIfNeeded();
                }, 1000);
            }
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Log.d("BluetoothService", "Starte Verbindung...");
            bluetoothManager.connect();
        }, 1000);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Bluetooth-Verbindung")
                .setContentText("Bluetooth läuft im Hintergrund")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("BluetoothService", "onStartCommand");
        if (bluetoothManager != null && !bluetoothManager.isConnected()) {
            bluetoothManager.connect();
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.w("BluetoothService", "Service beendet – trenne Verbindung");
        if (bluetoothManager != null) bluetoothManager.disconnect();
        super.onDestroy();
    }

    public static BluetoothManager getBluetoothManager() {
        return bluetoothManager;
    }
}
