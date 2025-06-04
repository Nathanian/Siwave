package com.bro.siwave.session;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.bro.siwave.util.NotificationHelper;

public class TrainingService extends Service {

    public static final String CHANNEL_ID = "siwave_training_channel";
    public static final int NOTIFICATION_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        // Noch kein Inhalt – Notification kommt beim Start
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String title = "Training läuft";
        String content = "Deine SiWave-Session ist aktiv";

        NotificationCompat.Builder notification = NotificationHelper.buildBaseNotification(this, title, content);
        startForeground(NOTIFICATION_ID, notification.build());

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Wird aufgerufen wenn der Service manuell gestoppt wird
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // kein Binding nötig
    }
}
