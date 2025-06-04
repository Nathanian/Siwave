package com.bro.siwave.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.bro.siwave.MainActivity;
import com.bro.siwave.R;
import com.bro.siwave.session.TrainingService;

import java.util.Locale;

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

    public static NotificationCompat.Builder buildFullNotification(Context context, boolean isRunning, boolean isPresetTraining, int hz, int secLeft) {
        PendingIntent openAppIntent = PendingIntent.getActivity(
                context,
                0,
                new Intent(context, MainActivity.class), // öffnet App bei Klick
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent stopIntent = PendingIntent.getBroadcast(
                context, 1,
                new Intent(TrainingService.ACTION_STOP).setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent startIntent = PendingIntent.getBroadcast(
                context, 2,
                new Intent(TrainingService.ACTION_START).setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent upIntent = PendingIntent.getBroadcast(
                context, 3,
                new Intent(TrainingService.ACTION_FREQ_UP).setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent downIntent = PendingIntent.getBroadcast(
                context, 4,
                new Intent(TrainingService.ACTION_FREQ_DOWN).setPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String title = isRunning ? "Training läuft" : "Training pausiert";
        String content = String.format(Locale.getDefault(), "%02d Hz · %02d:%02d",
                hz, secLeft / 60, secLeft % 60);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TrainingService.CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(openAppIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setOngoing(isRunning);

        // Buttons je nach Modus hinzufügen
        if (!isPresetTraining) {
            builder.addAction(R.drawable.down_333333_grey, "-", downIntent);
        }

        builder.addAction(
                isRunning ? R.drawable.stop_d66d6d_red : R.drawable.play_88d66d_green,
                isRunning ? "Stop" : "Start",
                isRunning ? stopIntent : startIntent
        );

        if (!isPresetTraining) {
            builder.addAction(R.drawable.up_333333_grey, "+", upIntent);
        }

        return builder;
    }
}
