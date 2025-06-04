package com.bro.siwave.session;

import android.os.Build;
import android.os.CountDownTimer;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import com.bro.siwave.session.TrainingService;


public class TrainingSessionManager {
    private SessionListener listener;
    private CountDownTimer timer;
    private boolean isRunning = false;

    private SessionType type;
    private int currentFrequency;
    private int durationSec;

    private List<SessionStep> steps;
    private int currentStepIndex;

    private Context context;

    public TrainingSessionManager(Context context) {
        this.context = context.getApplicationContext();
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

    public void setListener(SessionListener listener) {
        this.listener = listener;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void startFreiesTraining(int hz, int seconds) {
        stop(); // vorher abbrechen
        type = SessionType.FREI;
        currentFrequency = hz;
        durationSec = seconds;
        isRunning = true;
        if (listener != null) {
            listener.onFrequencyChanged(hz);
            listener.onSessionStarted();
        }
        startTimer(seconds);
    }

    public void startVordefiniert(List<SessionStep> steps) {
        stop();
        type = SessionType.VORGEGEBEN;
        this.steps = steps;
        currentStepIndex = 0;
        isRunning = true;
        startNextStep();
    }

    private void startNextStep() {
        if (currentStepIndex >= steps.size()) {
            stop();
            return;
        }

        SessionStep step = steps.get(currentStepIndex);
        currentFrequency = step.frequency;
        if (listener != null) {
            listener.onFrequencyChanged(step.frequency);
            listener.onSessionStarted();
        }
        startTimer(step.durationSec);
        currentStepIndex++;
    }

    private void startTimer(int seconds) {
        timer = new CountDownTimer(seconds * 1000L, 1000) {
            int remaining = seconds;
            @Override
            public void onTick(long millisUntilFinished) {
                remaining--;
                if (listener != null) {
                    listener.onTick(remaining);
                }
            }
            @Override
            public void onFinish() {
                if (type == SessionType.VORGEGEBEN) {
                    startNextStep();
                } else {
                    stop();
                }
            }
        }.start();
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        isRunning = false;
        if (listener != null) {
            listener.onSessionStopped();
        }
    }

    public int getCurrentFrequency() {
        return currentFrequency;
    }

    public SessionType getType() {
        return type;
    }
}
