package com.bro.siwave.video;



import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.ImageButton;
import com.bro.siwave.menu.MenuFragment;

import com.bro.siwave.R;

import java.util.ArrayList;
import java.util.List;

public class YoutubeFragment extends Fragment implements YoutubeAdapter.OnVideoClickListener {

    private final List<YoutubeVideo> videos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_youtube_gallery, container, false);

        loadVideos();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerYoutube);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new YoutubeAdapter(videos, this));

        ImageButton btnExit = view.findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new MenuFragment())
                        .commit());

        return view;
    }

    private void loadVideos() {
        videos.clear();
        videos.add(new YoutubeVideo("CxpYagTYJIU", "Video 1"));
        // Add more YoutubeVideo items here
    }

    @Override
    public void onVideoClick(YoutubeVideo video) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + video.videoId));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/watch?v=" + video.videoId));
        try {
            startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            startActivity(webIntent);
        }
    }
}
