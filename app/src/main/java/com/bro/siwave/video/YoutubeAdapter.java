package com.bro.siwave.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bro.siwave.R;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class YoutubeAdapter extends RecyclerView.Adapter<YoutubeAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(YoutubeVideo video);
    }

    private final List<YoutubeVideo> videos;
    private final OnVideoClickListener listener;

    public YoutubeAdapter(List<YoutubeVideo> videos, OnVideoClickListener listener) {
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        YoutubeVideo item = videos.get(position);
        holder.title.setText(item.title);
        new LoadThumbnailTask(holder.thumb).execute(item.videoId);
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

    private static class LoadThumbnailTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        LoadThumbnailTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... ids) {
            try {
                URL url = new URL("https://img.youtube.com/vi/" + ids[0] + "/0.jpg");
                InputStream stream = url.openStream();
                return BitmapFactory.decodeStream(stream);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}