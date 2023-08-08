package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.adsapplication.utils.ImageAdapter;
import com.example.adsapplication.utils.models.CustomResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DisplayResponseActivity extends AppCompatActivity implements View.OnClickListener, ImageAdapter.OnImageClickListener {

    private final String TAG = "ww";

    private String videoUri;    // 视频的 uri 字符串
    private List<String> imagesUri;    // 图片的 uri 字符串

    private int index = 0;    // 当前视频使用的图片序号

    private VideoView videoView;
    private Button saveButton;
    private Button cancelButton;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private int itemSize;    // recyclerView 里每个 item 的宽度（用于设置高度）
    int spanCount = 4;    // recyclerView 里的列数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_response);

        imagesUri = getIntent().getStringArrayListExtra("imagesUri");
        videoUri = getIntent().getStringExtra("videoUri");

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        videoView = findViewById(R.id.videoView);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        videoView.setVideoURI(Uri.parse(videoUri));
        videoView.requestFocus();
        videoView.start();

        recyclerView = findViewById(R.id.recyclerView);
        itemSize = calculateItemSize(spanCount);

        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        imageAdapter = new ImageAdapter(imagesUri, itemSize);
        imageAdapter.setOnImageClickListener(this);
        recyclerView.setAdapter(imageAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            // TODO: 保存视频

        } else if (v.getId() == R.id.cancelButton) {
            // TODO: 返回主页面

        }
    }

    @Override
    public void onImageClick(int position) {
        // 用户点击图片时
        index = position;
        imageAdapter.setSelectedItemPosition(index);

    }

    private void displaySelectedVideo(Uri videoUri) {
        videoView.setVideoURI(videoUri);
        videoView.requestFocus();
        videoView.start();
    }

    private int calculateItemSize(int spanCount) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        return (screenWidth - (spanCount - 1) * itemSpacing) / spanCount;
    }

}