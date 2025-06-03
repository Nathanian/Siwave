// VideoAdapter.java
package com.bro.siwave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(String videoFileName);
    }

    private final Context context;
    private final List<String> videoFileNames;
    private final OnVideoClickListener listener;

    public VideoAdapter(Context context, List<String> videoFileNames, OnVideoClickListener listener) {
        this.context = context;
        this.videoFileNames = videoFileNames;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        String fileName = videoFileNames.get(position);
        holder.videoTitle.setText(fileName);

        // Thumbnail optional entfernen, wenn instabil
       // holder.thumbnailImage.setImageResource(R.drawable.default_thumbnail);

        holder.itemView.setOnClickListener(v -> listener.onVideoClick(fileName));
    }

    @Override
    public int getItemCount() {
        return videoFileNames.size();
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImage;
        TextView videoTitle;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImage = itemView.findViewById(R.id.thumbnailImage);
            videoTitle = itemView.findViewById(R.id.videoTitle);
        }
    }
}
