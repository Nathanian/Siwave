package com.bro.siwave.session;

import static android.app.Service.START_STICKY;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.bro.siwave.util.NotificationHelper;

public class TrainingService extends Service {

    public static final String CHANNEL_ID = "siwave_training_channel";
    public static final int NOTIFICATION_ID = 1;

    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_FREQ_UP = "ACTION_FREQ_UP";
    public static final String ACTION_FREQ_DOWN = "ACTION_FREQ_DOWN";

    public static void updateNotification(Context context) {
        TrainingSessionManager manager = TrainingSessionManager.getInstance();
        if (manager == null) return;

        NotificationCompat.Builder builder = NotificationHelper.buildFullNotification(
                context,
                manager.isRunning(),
                manager.isPresetTraining(),
                manager.getCurrentFrequency(),
                manager.getRemainingSeconds()
        );

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // Noch kein Inhalt – Notification kommt beim Start
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationHelper.createTrainingChannel(this);
        updateNotification(this);
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




