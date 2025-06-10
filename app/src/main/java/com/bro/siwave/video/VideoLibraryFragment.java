package com.bro.siwave.video;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bro.siwave.R;

import java.util.Arrays;
import java.util.List;

public class VideoLibraryFragment extends Fragment {

    private final List<VideoItem> videos = Arrays.asList(
            new VideoItem(R.raw.schwingen_mit_der_sinuswelle, "Einführung")
            // weitere Videos bei Bedarf
    );

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_video_library, container, false);
        LinearLayout list = root.findViewById(R.id.video_list);

        for (VideoItem video : videos) {
            View item = inflater.inflate(R.layout.item_video, list, false);
            TextView title = item.findViewById(R.id.video_title);
            ImageView thumb = item.findViewById(R.id.thumbnail);

            title.setText(video.title);
            thumb.setImageResource(R.drawable.ic_menu_333); // Platzhalter-Bild

            item.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), FullscreenVideoActivity.class);
                intent.putExtra("videoResId", video.resId);
                startActivity(intent);
            });

            list.addView(item);
        }

        return root;
    }
}
