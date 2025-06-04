package com.bro.siwave;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bro.siwave.bluetooth.BluetoothConnector;
import com.bro.siwave.bluetooth.BluetoothManager;
import com.bro.siwave.session.TrainingSessionManager;

public class MainActivity extends AppCompatActivity {

    public static BluetoothConnector bluetoothConnector;
    public static TrainingSessionManager trainingSession;
    private static final String PREFS_NAME = "SiWavePrefs";
    private static final String PREF_START_WITH_TRAINING = "startWithTraining";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        trainingSession = new TrainingSessionManager(this);
        setContentView(R.layout.activity_main); // enthält: <FrameLayout android:id="@+id/fragment_container" />

        bluetoothConnector = new BluetoothConnector(this, new BluetoothManager.Listener() {
            @Override public void onConnected() { }
            @Override public void onDisconnected(String reason) { }
        });
        bluetoothConnector.reconnectIfNeeded();


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
