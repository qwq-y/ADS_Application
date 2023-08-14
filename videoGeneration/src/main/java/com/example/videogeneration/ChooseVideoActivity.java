package com.example.videogeneration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.adsapplication.R;

public class ChooseVideoActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "ww";

    private VideoView videoView;
    private ImageButton addButton;
    private Button okButton;
    private Button retryButton;
    private TextView textView;

    private static final int REQUEST_VIDEO_PICK = 1;

    private String videoUriStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_video);

        videoView = findViewById(R.id.videoView);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        textView = findViewById(R.id.textView);

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(this);

        okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(this);
        okButton.setVisibility(View.GONE);

        retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(this);
        retryButton.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.okButton) {
            // 跳转至区间选择页面（同时传递视频 uri）
            Intent intent = new Intent(this, ChooseIntervalActivity.class);
            intent.putExtra("videoUriStr", videoUriStr);
            startActivity(intent);
        } else if (v.getId() == R.id.retryButton) {
            // 重新选择视频
            pickVideo(v);
        } else if (v.getId() == R.id.addButton) {
            // 选择视频
            pickVideo(v);
        }
    }

    private void pickVideo(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK && data != null) {

            // 显示视频
            Uri videoUri = data.getData();
            videoUriStr = videoUri.toString();
            displaySelectedVideo(videoUri);

            // 隐藏加号按钮、显示确定和重选按钮
            addButton.setVisibility(View.GONE);
            okButton.setVisibility(View.VISIBLE);
            retryButton.setVisibility(View.VISIBLE);
        }
    }

    private void displaySelectedVideo(Uri videoUri) {
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.start();
    }
}