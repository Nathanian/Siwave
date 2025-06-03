// VideoFragment.java
package com.bro.siwave;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoFragment extends Fragment {

    private RecyclerView recyclerView;
    private VideoAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video, container, false);
        recyclerView = view.findViewById(R.id.videoRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        List<String> videoFiles = getVideoFilesFromAssets();
        adapter = new VideoAdapter(requireContext(), videoFiles, fileName -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, VideoPlayerFragment.newInstance(fileName))
                    .addToBackStack(null)
                    .commit();
        });

        recyclerView.setAdapter(adapter);

        return view;
    }

    private List<String> getVideoFilesFromAssets() {
        List<String> result = new ArrayList<>();
        try {
            AssetManager assetManager = requireContext().getAssets();
            String[] files = assetManager.list("videos");
            if (files != null) {
                result = Arrays.asList(files);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
