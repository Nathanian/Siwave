package com.bro.siwave;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    public static BluetoothHelper bluetoothHelper;

    private static final String PREFS_NAME = "SiWavePrefs";
    private static final String PREF_START_WITH_TRAINING = "startWithTraining";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // enthält: <FrameLayout android:id="@+id/fragment_container" />

        bluetoothHelper = new BluetoothHelper(
                this,
                () -> {},  // Optional: z. B. UI updaten
                () -> {}   // Optional: z. B. UI updaten
        );
        bluetoothHelper.reconnectIfNeeded();

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
