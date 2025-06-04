package com.bro.siwave.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.bro.siwave.R;
import com.bro.siwave.session.TrainingService;

public class NotificationHelper {

    public static void createTrainingChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    TrainingService.CHANNEL_ID,
                    "SiWave Training",
                    NotificationManager.IMPORTANCE_LOW // nicht laut vibrieren
            );
            channel.setDescription("Hält deine Trainingseinheit aktiv");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static NotificationCompat.Builder buildBaseNotification(Context context, String title, String content) {
        return new NotificationCompat.Builder(context, TrainingService.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_stat_training) // selbst anlegen, z. B. weißes Blitzsymbol
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(true);
    }
}
