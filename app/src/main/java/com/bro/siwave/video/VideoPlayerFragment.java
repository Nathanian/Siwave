// VideoPlayerFragment.java
package com.bro.siwave.video;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bro.siwave.R;

public class VideoPlayerFragment extends Fragment {

    private static final String ARG_VIDEO_NAME = "videoFileName";

    public static VideoPlayerFragment newInstance(String videoFileName) {
        VideoPlayerFragment fragment = new VideoPlayerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_VIDEO_NAME, videoFileName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_video_player, container, false);

        VideoView videoView = view.findViewById(R.id.videoView);
        String fileName = getArguments() != null ? getArguments().getString(ARG_VIDEO_NAME) : null;

        if (fileName != null) {
            String videoPath = "android.resource://" + requireContext().getPackageName() + "/assets/videos/" + fileName;
            Uri uri = Uri.parse("file:///android_asset/videos/" + fileName);
            videoView.setVideoURI(uri);

            MediaController mediaController = new MediaController(requireContext());
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);
            videoView.start();
        }

        return view;
    }
}
