package com.bro.siwave;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class TrainingFragment extends Fragment {

    private int frequency = 5;
    private final int MIN_FREQ = 5;
    private final int MAX_FREQ = 28;

    private double sessionTimeSec = 300;
    private final double MIN_TIME_SEC = 60;
    private final double MAX_TIME_SEC = 3600;

    private boolean started = false;
    private boolean timerRunning = false;
    private long lastClickTime = 0;

    private TextView statusText, timerText;
    private ImageButton btnStart, btnUp, btnDown, btnLeft, btnRight;
    private CountDownTimer countDownTimer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_training, container, false);

        statusText = view.findViewById(R.id.statusText);
        timerText = view.findViewById(R.id.timerText);
        btnStart = view.findViewById(R.id.btnStart);
        btnUp = view.findViewById(R.id.btnUp);
        btnDown = view.findViewById(R.id.btnDown);
        btnLeft = view.findViewById(R.id.btnLeft);
        btnRight = view.findViewById(R.id.btnRight);
        ImageButton btnMenu = view.findViewById(R.id.btnMenu);

        setButtonsEnabled(false);
        updateFrequencyDisplay();
        updateTimerDisplay();

        btnStart.setOnClickListener(v -> {
            if (!MainActivity.bluetoothHelper.isConnected()) return;
            if (started) stopSession(); else startSession();
        });

        btnMenu.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MenuFragment())
                    .addToBackStack(null) // optional: Zurück-Taste geht dann zurück
                    .commit();
        });

        btnUp.setOnClickListener(v -> {
            if (frequency < MAX_FREQ) frequency++;
            updateFrequencyDisplay();
            if (started) MainActivity.bluetoothHelper.sendCommandToArduino("#SETDAC:" + frequency + ":!");
        });

        btnDown.setOnClickListener(v -> {
            if (frequency > MIN_FREQ) frequency--;
            updateFrequencyDisplay();
            if (started) MainActivity.bluetoothHelper.sendCommandToArduino("#SETDAC:" + frequency + ":!");
        });

        btnLeft.setOnClickListener(v -> {
            sessionTimeSec = Math.max(sessionTimeSec - 30, MIN_TIME_SEC);
            updateTimerDisplay();
            if (started && timerRunning) restartTimerWithNewTime();
        });

        btnRight.setOnClickListener(v -> {
            sessionTimeSec = Math.min(sessionTimeSec + 30, MAX_TIME_SEC);
            updateTimerDisplay();
            if (started && timerRunning) restartTimerWithNewTime();
        });

        timerText.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - lastClickTime < 400) {
                if (started && timerRunning) {
                    countDownTimer.cancel();
                    sessionTimeSec = 300;
                    updateTimerDisplay();
                    startSession();
                } else {
                    sessionTimeSec = 300;
                    updateTimerDisplay();
                }
                Toast.makeText(getContext(), "Timer auf 5 Minuten zurückgesetzt", Toast.LENGTH_SHORT).show();
            }
            lastClickTime = now;
        });

        checkBluetoothStatus();

        return view;
    }

    private void checkBluetoothStatus() {
        if (!MainActivity.bluetoothHelper.isConnected() && !MainActivity.bluetoothHelper.isTryingToConnect()) {
            Toast.makeText(getContext(), "Bluetooth-Verbindung wird aufgebaut...", Toast.LENGTH_SHORT).show();
            MainActivity.bluetoothHelper.connectBluetooth();
        } else if (MainActivity.bluetoothHelper.isConnected()) {
            setButtonsEnabled(true);
        }
    }

    private void startSession() {
        MainActivity.bluetoothHelper.sendCommandToArduino("#SETDAC:" + frequency + ":!");
        SystemClock.sleep(100);
        MainActivity.bluetoothHelper.sendCommandToArduino("#SETOPTO:HIGH:!");

        btnStart.setImageResource(R.drawable.stop_d66d6d_red);
        statusText.setText(frequency + " Hz");
        started = true;

        countDownTimer = new CountDownTimer((long) sessionTimeSec * 1000, 1000) {
            @Override public void onTick(long millisUntilFinished) {
                sessionTimeSec = millisUntilFinished / 1000.0;
                updateTimerDisplay();
            }
            @Override public void onFinish() { stopSession(); }
        }.start();
        timerRunning = true;
    }

    private void stopSession() {
        MainActivity.bluetoothHelper.sendCommandToArduino("#SETDAC:0:!");
        SystemClock.sleep(100);
        MainActivity.bluetoothHelper.sendCommandToArduino("#SETOPTO:LOW:!");

        btnStart.setImageResource(R.drawable.play_333333_grey);
        statusText.setText("Gestoppt");
        started = false;

        if (timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
        }
    }

    private void updateFrequencyDisplay() {
        statusText.setText(String.format("%02d Hz", frequency));
    }

    private void updateTimerDisplay() {
        int minutes = (int) (sessionTimeSec / 60);
        int seconds = (int) (sessionTimeSec % 60);
        timerText.setText(String.format("%02d : %02d", minutes, seconds));
    }

    private void setButtonsEnabled(boolean enabled) {
        btnStart.setEnabled(enabled);
        btnUp.setEnabled(enabled);
        btnDown.setEnabled(enabled);
        btnLeft.setEnabled(enabled);
        btnRight.setEnabled(enabled);
    }

    private void restartTimerWithNewTime() {
        countDownTimer.cancel();
        countDownTimer = new CountDownTimer((long) sessionTimeSec * 1000, 1000) {
            @Override public void onTick(long millisUntilFinished) {
                sessionTimeSec = millisUntilFinished / 1000.0;
                updateTimerDisplay();
            }
            @Override public void onFinish() { stopSession(); }
        }.start();
    }
}
