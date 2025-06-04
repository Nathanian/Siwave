package com.bro.siwave.session;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bro.siwave.preset.PresetProgram;
import com.bro.siwave.preset.PresetStep;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TrainingSessionManager {

    private static TrainingSessionManager instance;

    private final Context context;
    private SessionListener listener;

    private boolean isRunning = false;
    private boolean isPresetTraining = false;
    private int frequency = 5;
    private int remainingSec = 0;
    private Timer timer;

    public TrainingSessionManager(Context context) {
        this.context = context.getApplicationContext();
        instance = this;
    }

    public static TrainingSessionManager getInstance() {
        return instance;
    }

    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public int getCurrentFrequency() {
        return frequency;
    }

    public int getRemainingSeconds() {
        return remainingSec;
    }

    public boolean isPresetTraining() {
        return isPresetTraining;
    }

    public void incrementFrequency() {
        frequency += 1;
        if (listener != null) listener.onFrequencyChanged(frequency);
        TrainingService.updateNotification(context);
    }

    public void decrementFrequency() {
        frequency = Math.max(1, frequency - 1);
        if (listener != null) listener.onFrequencyChanged(frequency);
        TrainingService.updateNotification(context);
    }

    public void startFreiesTraining(int hz, int durationSeconds) {
        stop();
        isPresetTraining = false;
        frequency = hz;
        remainingSec = durationSeconds;
        isRunning = true;

        startTimer();

        if (listener != null) listener.onSessionStarted();
        TrainingService.updateNotification(context);
        startService();
    }

    public void startVordefiniert(PresetProgram preset) {
        stop();
        isPresetTraining = true;
        frequency = 5;
        remainingSec = preset.duration;
        isRunning = true;

        startTimer();

        if (listener != null) listener.onSessionStarted();
        TrainingService.updateNotification(context);
        startService();

        for (PresetStep step : preset.steps) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                handlePresetAction(step.action);
            }, step.after * 1000L);
        }
    }

    private void handlePresetAction(String action) {
        try {
            String[] parts = action.split(" ");
            String cmd = parts[0];
            int count = Integer.parseInt(parts[1]);

            for (int i = 0; i < count; i++) {
                if ("up".equalsIgnoreCase(cmd)) {
                    incrementFrequency();
                } else if ("down".equalsIgnoreCase(cmd)) {
                    decrementFrequency();
                }
            }
        } catch (Exception e) {
            Log.e("PresetAction", "Fehler bei Aktion: " + action, e);
        }
    }

    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                remainingSec--;
                if (listener != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onTick(remainingSec);
                    });
                }
                TrainingService.updateNotification(context);

                if (remainingSec <= 0) {
                    stop();
                }
            }
        }, 1000, 1000);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        isRunning = false;
        if (listener != null) listener.onStopped();
        TrainingService.updateNotification(context);
        stopService();
    }

    private void startService() {
        Intent serviceIntent = new Intent(context, TrainingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        }
    }

    private void stopService() {
        Intent serviceIntent = new Intent(context, TrainingService.class);
        context.stopService(serviceIntent);
    }
}
