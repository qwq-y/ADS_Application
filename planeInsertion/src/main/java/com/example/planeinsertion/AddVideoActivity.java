package com.example.planeinsertion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.planeinsertion.utils.MyConverter;
import com.example.planeinsertion.utils.MyRequester;
import com.example.planeinsertion.utils.models.CustomResponse;
import com.example.planeinsertion.utils.models.ResponseCallback;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddVideoActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "ww";

    String user = "lzl";

    private String videoUriStr;    // 原视频
    private String frameUriStr;    // 第一帧原图
    private String fourChannelImageUriStr;    // 四通道图
    private String startMillis, endMillis;

    private String videoSourceUriStr;    // 准备插入的广告

    private String generatedVideoUriStr;    // 生成的视频

    private VideoView videoView;
    private ImageButton addButton;
    private Button okButton;
    private Button retryButton;
    private TextView textView;

    private static final int REQUEST_VIDEO_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_video);

        videoUriStr = getIntent().getStringExtra("videoUriStr");
        startMillis = getIntent().getStringExtra("startMillis");
        endMillis = getIntent().getStringExtra("endMillis");
        frameUriStr = getIntent().getStringExtra("frameUriStr");
        fourChannelImageUriStr = getIntent().getStringExtra("fourChannelImageUriStr");

        videoView = findViewById(R.id.videoView);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        textView = findViewById(R.id.textView);
        textView.setText("请选择插入平面的视频");

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

            List<String> imageFilesUri = new ArrayList<>();
//            if (frameUriStr != null) {
//                imageFilesUri.add(frameUriStr);
//            }
            if (fourChannelImageUriStr != null) {
                imageFilesUri.add(fourChannelImageUriStr);
            }
            Gson gson = new Gson();
            String imageFilesUriJsonStr = gson.toJson(imageFilesUri);

            Map<String, String> params = new HashMap<>();
            params.put("user", user);

            String url = "http://172.18.36.110:5005/Track";

            // TODO: 与后端测试通信
            MyRequester.newThreadAndSendRequest(new ResponseCallback() {
                                                    @Override
                                                    public void onSuccess(CustomResponse response) {
                                                        Log.d(TAG, "onSuccess callback");
                                                        try {
                                                            String video = response.getVideo();
                                                            generatedVideoUriStr = MyConverter.convertVideoToUri(AddVideoActivity.this, video);

                                                        } catch (Exception e) {
                                                            Log.e(TAG, "convert video: " + e.getMessage());
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(String errorMessage) {
                                                        Log.e(TAG, "onError callback: " + errorMessage);
                                                    }
                                                }, this, getContentResolver(),
                    videoUriStr, videoSourceUriStr,
                    imageFilesUriJsonStr,
                    params, url);

            Intent intent = new Intent(this, DisplayResultActivity.class);
            intent.putExtra("generatedVideoUriStr", generatedVideoUriStr);
            startActivity(intent);

        } else if (v.getId() == R.id.retryButton) {
            pickVideo(v);
        } else if (v.getId() == R.id.addButton) {
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
            videoSourceUriStr = videoUri.toString();
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