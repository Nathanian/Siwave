package com.bro.siwave;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bro.siwave.bluetooth.BluetoothService;
import com.bro.siwave.session.TrainingSessionManager;

public class MainActivity extends AppCompatActivity {
    public static TrainingSessionManager trainingSession;

    private static final String PREFS_NAME = "SiWavePrefs";
    private static final String PREF_START_WITH_TRAINING = "startWithTraining";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Training Session vorbereiten
        trainingSession = new TrainingSessionManager(this);

        // Layout setzen
        setContentView(R.layout.activity_main);

        // BluetoothService starten
        Intent intent = new Intent(this, BluetoothService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        // Fragment laden
        if (savedInstanceState == null) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean startWithTraining = prefs.getBoolean(PREF_START_WITH_TRAINING, false);
            Fragment fragment = startWithTraining ? new TrainingFragment() : new MenuFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }
}
