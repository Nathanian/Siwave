package com.bro.siwave.ui.preset;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bro.siwave.R;
import com.bro.siwave.preset.PresetProgram;

import java.util.List;

public class PresetAdapter extends RecyclerView.Adapter<PresetAdapter.PresetViewHolder> {

    public interface OnPresetClickListener {
        void onPresetClick(PresetProgram preset);
    }

    private final List<PresetProgram> presets;
    private final OnPresetClickListener listener;

    public PresetAdapter(List<PresetProgram> presets, OnPresetClickListener listener) {
        this.presets = presets;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PresetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preset, parent, false);
        return new PresetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PresetViewHolder holder, int position) {
        PresetProgram preset = presets.get(position);
        holder.name.setText(preset.name);
        holder.duration.setText(preset.duration + " Sek.");

        holder.itemView.setOnClickListener(v -> listener.onPresetClick(preset));
    }

    @Override
    public int getItemCount() {
        return presets.size();
    }

    public static class PresetViewHolder extends RecyclerView.ViewHolder {
        TextView name, duration;

        public PresetViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.textPresetName);
            duration = itemView.findViewById(R.id.textPresetDuration);
        }
    }
}
