package com.example.planeinsertion;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

public class DisplayResultActivity extends AppCompatActivity {

    private String TAG = "ww";

    private String generatedVideoUriStr;    // 生成的视频

    private VideoView videoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);

        generatedVideoUriStr = getIntent().getStringExtra("generatedVideoUriStr");

        videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        videoView.setVideoURI(Uri.parse(generatedVideoUriStr));
        videoView.requestFocus();
        videoView.start();
    }
}