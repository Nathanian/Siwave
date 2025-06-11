package com.bro.siwave.video;

import android.content.Intent;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class VideoLibraryFragment extends Fragment implements VideoAdapter.OnVideoClickListener {

    private final List<VideoItem> videos = new ArrayList<>();


    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_library, container, false);

        loadVideos();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerVideos);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new VideoAdapter(videos, this));

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
        Field[] fields = R.raw.class.getFields();
        for (Field field : fields) {
            try {
                int id = field.getInt(null);
                String name = field.getName().replace('_', ' ');
                if (!name.isEmpty()) {
                    name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
                }
                videos.add(new VideoItem(id, name));
            } catch (IllegalAccessException ignored) {
            }
        }
    }
    @Override
    public void onVideoClick(VideoItem item) {
        Intent intent = new Intent(requireContext(), FullscreenVideoActivity.class);
        intent.putExtra("videoResId", item.resId);
        startActivity(intent);
    }
}

