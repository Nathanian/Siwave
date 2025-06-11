package com.bro.siwave.menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bro.siwave.R;
import com.bro.siwave.training.TrainingFragment;
import com.bro.siwave.ui.preset.PresetFragment;
import com.bro.siwave.video.VideoLibraryFragment;

public class MenuFragment extends Fragment {

    private static final String PREFS_NAME = "SiWavePrefs";
    private static final String PREF_START_WITH_TRAINING = "startWithTraining";

    private CheckBox checkboxStartWithTraining;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        checkboxStartWithTraining = view.findViewById(R.id.checkbox_start_with_training);
        Button btnStartTraining = view.findViewById(R.id.btn_start_training);
        Button btnExit = view.findViewById(R.id.btn_exit);
        Button btnMinimize = view.findViewById(R.id.btn_minimize);

        Button btnVideo = view.findViewById(R.id.btnVideo);
        Button btnPresets = view.findViewById(R.id.btnPresets);
        Button btnYoutube = view.findViewById(R.id.btnYoutube);

        // Lade gespeicherten Zustand
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean savedState = prefs.getBoolean(PREF_START_WITH_TRAINING, false);
        checkboxStartWithTraining.setChecked(savedState);

        checkboxStartWithTraining.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(PREF_START_WITH_TRAINING, isChecked).apply();
        });

        btnVideo.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new com.bro.siwave.video.VideoLibraryFragment())
                    .addToBackStack(null)
                    .commit();
        });
        btnYoutube.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new com.bro.siwave.video.YoutubeFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnPresets.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new PresetFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnStartTraining.setOnClickListener(v -> {
            // Fragment-Wechsel zum TrainingFragment
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new TrainingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnMinimize.setOnClickListener(v -> requireActivity().moveTaskToBack(true));

        btnExit.setOnClickListener(v -> {
            requireActivity().finishAffinity(); // schließt alle Activities
        });

        return view;
    }
}
