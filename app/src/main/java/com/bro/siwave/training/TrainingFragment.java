package com.bro.siwave.training;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bro.siwave.MainActivity;
import com.bro.siwave.menu.MenuFragment;
import com.bro.siwave.R;
import com.bro.siwave.bluetooth.BluetoothManager;
import com.bro.siwave.bluetooth.BluetoothService;
import com.bro.siwave.session.SessionListener;
import com.bro.siwave.session.TrainingSessionManager;
import com.bro.siwave.util.DoubleClickHandler;

public class TrainingFragment extends Fragment {

    private TextView statusText, timerText;
    private ImageButton btnStart, btnUp, btnDown, btnLeft, btnRight, btnExit;

    private int frequency = 5;
    private final int MIN_FREQ = 5;
    private final int MAX_FREQ = 28;

    private int durationSec = 300;
    private final int MIN_SEC = 60;
    private final int MAX_SEC = 3600;

    private DoubleClickHandler doubleClickHandler = new DoubleClickHandler(400);

    private TrainingSessionManager sessionManager;
    private BluetoothManager bluetooth;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_training, container, false);

        // UI-Elemente
        statusText = view.findViewById(R.id.statusText);
        timerText = view.findViewById(R.id.timerText);
        btnStart = view.findViewById(R.id.btnStart);
        btnUp = view.findViewById(R.id.btnUp);
        btnDown = view.findViewById(R.id.btnDown);
        btnLeft = view.findViewById(R.id.btnLeft);
        btnRight = view.findViewById(R.id.btnRight);
       // ImageButton btnMenu = view.findViewById(R.id.btnMenu);
        btnExit = view.findViewById(R.id.btnExit);


        // Logik
        bluetooth = BluetoothService.getBluetoothManager();
        sessionManager = MainActivity.trainingSession;

        // Session-Status laden
        if (sessionManager.isRunning()) {
            frequency = sessionManager.getCurrentFrequency();
            durationSec = sessionManager.getRemainingSeconds();
        }

        updateFrequencyDisplay();
        updateTimerDisplay(durationSec);

        // Listener setzen
        sessionManager.setListener(new SessionListener() {
            @Override
            public void onTick(int sec) {
                updateTimerDisplay(sec);
            }

            @Override
            public void onFrequencyChanged(int hz) {
                bluetooth.send("#SETDAC:" + hz + ":!");
                statusText.setText(String.format("%02d Hz", hz));
            }

            @Override
            public void onSessionStarted() {
                bluetooth.send("#SETOPTO:HIGH:!");
                btnStart.setImageResource(R.drawable.stop_d66d6d_red);
            }

            @Override
            public void onStopped() {
                bluetooth.send("#SETOPTO:LOW:!");
                bluetooth.send("#SETDAC:0:!");
                btnStart.setImageResource(R.drawable.play_333333_grey);
                statusText.setText("Gestoppt");
            }
        });

        Log.d("TrainingFragment", "BluetoothManager: " + bluetooth);
        Log.d("TrainingFragment", "Ist verbunden? " + (bluetooth != null && bluetooth.isConnected()));


        // Button-Logik
        setButtonsEnabled(bluetooth != null && bluetooth.isConnected());

        btnStart.setOnClickListener(v -> {
            if (!bluetooth.isConnected()) return;
            if (sessionManager.isRunning()) {
                sessionManager.stop();
            } else {
                sessionManager.startFreiesTraining(frequency, durationSec);
            }
        });

        btnUp.setOnClickListener(v -> {
            if (frequency < MAX_FREQ) frequency++;
            updateFrequencyDisplay();
            if (sessionManager.isRunning()) bluetooth.send("#SETDAC:" + frequency + ":!");
        });

        btnDown.setOnClickListener(v -> {
            if (frequency > MIN_FREQ) frequency--;
            updateFrequencyDisplay();
            if (sessionManager.isRunning()) bluetooth.send("#SETDAC:" + frequency + ":!");
        });

        btnLeft.setOnClickListener(v -> {
            durationSec = Math.max(durationSec - 30, MIN_SEC);
            updateTimerDisplay(durationSec);
        });

        btnRight.setOnClickListener(v -> {
            durationSec = Math.min(durationSec + 30, MAX_SEC);
            updateTimerDisplay(durationSec);
        });

        timerText.setOnClickListener(v -> {
            if (doubleClickHandler.isDoubleClick()) {
                durationSec = 300;
                updateTimerDisplay(durationSec);
                if (sessionManager.isRunning()) {
                    sessionManager.startFreiesTraining(frequency, durationSec);
                }
                Toast.makeText(getContext(), "Timer auf 5 Minuten zurückgesetzt", Toast.LENGTH_SHORT).show();
            }
        });

        btnExit.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new MenuFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void updateFrequencyDisplay() {
        statusText.setText(String.format("%02d Hz", frequency));
    }

    private void updateTimerDisplay(int sec) {
        int min = sec / 60;
        int rest = sec % 60;
        timerText.setText(String.format("%02d : %02d", min, rest));
    }

    private void setButtonsEnabled(boolean enabled) {
        btnStart.setEnabled(enabled);
        btnUp.setEnabled(enabled);
        btnDown.setEnabled(enabled);
        btnLeft.setEnabled(enabled);
        btnRight.setEnabled(enabled);
    }
}
