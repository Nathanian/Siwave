package com.bro.siwave;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends Activity {

    private BluetoothHelper bluetoothHelper;
    private int frequency = 5;
    private final int MIN_FREQ = 5;
    private final int MAX_FREQ = 28;
    private boolean started = false;
    private boolean isConnected = false;

    private double sessionTimeSec = 300;
    private final double MIN_TIME_SEC = 60;
    private final double MAX_TIME_SEC = 3600;

    private TextView statusText, timerText;
    private ImageButton btnStart, btnUp, btnDown, btnLeft, btnRight;

    private CountDownTimer countDownTimer;
    private boolean timerRunning = false;
    private long lastClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        timerText = findViewById(R.id.timerText);
        btnStart = findViewById(R.id.btnStart);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);

        setButtonsEnabled(false);
        updateFrequencyDisplay();
        updateTimerDisplay();

        bluetoothHelper = new BluetoothHelper(
                this,
                this::onBluetoothConnected,
                this::onBluetoothDisconnected
        );
        bluetoothHelper.connectBluetooth();

        btnStart.setOnClickListener(v -> {
            if (!isConnected) return;
            if (started) {
                stopSession();
            } else {
                startSession();
            }
        });

        btnUp.setOnClickListener(v -> {
            if (frequency < MAX_FREQ) frequency++;
            updateFrequencyDisplay();
            if (started) {
                bluetoothHelper.sendCommandToArduino("#SETDAC:" + frequency + ":!");
            }
        });

        btnDown.setOnClickListener(v -> {
            if (frequency > MIN_FREQ) frequency--;
            updateFrequencyDisplay();
            if (started) {
                bluetoothHelper.sendCommandToArduino("#SETDAC:" + frequency + ":!");
            }
        });

        btnLeft.setOnClickListener(v -> {
            sessionTimeSec -= 30;
            if (sessionTimeSec < MIN_TIME_SEC) sessionTimeSec = MIN_TIME_SEC;
            updateTimerDisplay();
            if (started && timerRunning) restartTimerWithNewTime();
        });


        btnRight.setOnClickListener(v -> {
            sessionTimeSec += 30;
            if (sessionTimeSec > MAX_TIME_SEC) sessionTimeSec = MAX_TIME_SEC;
            updateTimerDisplay();
            if (started && timerRunning) restartTimerWithNewTime();
        });


        timerText.setOnClickListener(v -> {
            long now = System.currentTimeMillis();
            if (now - lastClickTime < 400) {
                if (timerRunning && started) {
                    countDownTimer.cancel(); // alten Timer abbrechen
                    sessionTimeSec = 300;
                    updateTimerDisplay();
                    startSession(); // neu starten
                } else {
                    sessionTimeSec = 300;
                    updateTimerDisplay();
                }
                Toast.makeText(this, "Timer auf 5 Minuten zurückgesetzt", Toast.LENGTH_SHORT).show();
            }
            lastClickTime = now;
        });

        ImageButton btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuActivity.class);
            startActivity(intent);
        });


    }

    private void onBluetoothConnected() {
        runOnUiThread(() -> {
            isConnected = true;
            setButtonsEnabled(true);
            btnStart.setImageResource(R.drawable.play_88d66d_green);
        });
    }

    private void onBluetoothDisconnected() {
        runOnUiThread(() -> {
            isConnected = false;
            setButtonsEnabled(false);
            btnStart.setImageResource(R.drawable.play_d66d6d_red);
        });
    }

    private void startSession() {
        bluetoothHelper.sendCommandToArduino("#SETDAC:" + frequency + ":!");
        SystemClock.sleep(100);
        bluetoothHelper.sendCommandToArduino("#SETOPTO:HIGH:!");

        btnStart.setImageResource(R.drawable.stop_d66d6d_red);
        statusText.setText(frequency + " Hz");
        started = true;

        countDownTimer = new CountDownTimer((long) sessionTimeSec * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sessionTimeSec = millisUntilFinished / 1000.0;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                stopSession();
            }
        }.start();
        timerRunning = true;
    }

    private void stopSession() {
        bluetoothHelper.sendCommandToArduino("#SETDAC:0:!");
        SystemClock.sleep(100);
        bluetoothHelper.sendCommandToArduino("#SETOPTO:LOW:!");
        SystemClock.sleep(100);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            Toast.makeText(this, "Zurück zur Startseite", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, MainDashboardActivity.class));
            return true;
        } else if (item.getItemId() == R.id.menu_freies_training) {
            Toast.makeText(this, "Freies Training", Toast.LENGTH_SHORT).show();
            return true;
        } else if (item.getItemId() == R.id.menu_fertige_trainings) {
            Toast.makeText(this, "Fertige Trainings", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, FertigeTrainingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void restartTimerWithNewTime() {
        countDownTimer.cancel();
        countDownTimer = new CountDownTimer((long) sessionTimeSec * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sessionTimeSec = millisUntilFinished / 1000.0;
                updateTimerDisplay();
            }

            @Override
            public void onFinish() {
                stopSession();
            }
        }.start();
    }


}
