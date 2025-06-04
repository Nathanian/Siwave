package com.bro.siwave.session;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        TrainingSessionManager manager = TrainingSessionManager.getInstance();

        if (manager == null) return;

        switch (action) {
            case TrainingService.ACTION_STOP:
                manager.stop();
                break;
            case TrainingService.ACTION_START:
                manager.startFreiesTraining(manager.getCurrentFrequency(), manager.getRemainingSeconds());
                break;
            case TrainingService.ACTION_FREQ_UP:
                manager.incrementFrequency();
                break;
            case TrainingService.ACTION_FREQ_DOWN:
                manager.decrementFrequency();
                break;
        }
    }
}
