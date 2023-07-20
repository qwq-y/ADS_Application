package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.VideoView;

public class ChooseVideoActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQUEST_VIDEO_PICK = 1;
    private VideoView videoView;
    private ImageButton addButton;
    private Button okButton;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_video);

        videoView = findViewById(R.id.videoView);

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
            // 跳转至区间选择页面（同时传递数据）
            Intent intent = new Intent(this, ChooseIntervalActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("message", "video chosen");
            intent.putExtras(bundle);
            startActivity(intent);
        } else if (v.getId() == R.id.retryButton) {
            // 重新选择视频
            pickVideo(v);
        } else if (v.getId() == R.id.addButton) {
            // 选择视频
            pickVideo(v);
        }
    }

    public void pickVideo(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_VIDEO_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_VIDEO_PICK && resultCode == RESULT_OK && data != null) {

            // 显示视频
            Uri videoUri = data.getData();
            displaySelectedVideo(videoUri);

            // 隐藏加号按钮
            addButton.setVisibility(View.GONE);

            // 显示确定和重选按钮
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