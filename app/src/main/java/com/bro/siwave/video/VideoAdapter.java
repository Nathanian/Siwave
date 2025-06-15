package com.bro.siwave.video;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android  .widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bro.siwave.R;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(VideoItem item);
    }

    private final List<VideoItem> videos;
    private final OnVideoClickListener listener;

    public VideoAdapter(List<VideoItem> videos, OnVideoClickListener listener) {
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoItem item = videos.get(position);
        holder.title.setText(item.title);


        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            AssetFileDescriptor afd = holder.itemView.getContext()
                    .getResources().openRawResourceFd(item.resId);
            retriever.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            Bitmap frame = retriever.getFrameAtTime(0);
            holder.thumb.setImageBitmap(frame);
            retriever.release();
            afd.close();
        } catch (Exception e) {
            holder.thumb.setImageResource(R.drawable.ic_menu_333);
        }
        holder.itemView.setOnClickListener(v -> listener.onVideoClick(item));
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView thumb;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.video_title);
            thumb = itemView.findViewById(R.id.thumbnail);
        }
    }
}