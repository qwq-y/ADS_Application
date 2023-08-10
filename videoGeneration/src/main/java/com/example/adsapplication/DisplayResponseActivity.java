package com.example.adsapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.adsapplication.utils.ImageAdapter;
import com.example.adsapplication.utils.MyConverter;
import com.example.adsapplication.utils.MyRequester;
import com.example.adsapplication.utils.models.CustomResponse;
import com.example.adsapplication.utils.models.ResponseCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


// 注意：如果程序中，之前的页面不一定会申请运行时权限的话，本页需要添加相关代码！！！（目前Sending页申请过了）

public class DisplayResponseActivity extends AppCompatActivity implements View.OnClickListener, ImageAdapter.OnImageClickListener {

    private final String TAG = "ww";

    private String originalVideoUri;    // 原视频
    private String frameUriStr;    // 视频第一帧
    private String pathJsonStr;    // 绘制的路径
    private String textSource;    // 添加的文本素材

    private String videoUri;    // 生成的视频
    private List<String> imagesUri;    // 生成的图片列表

    private int index = 0;    // 当前视频使用的图片序号

    private VideoView videoView;
    private TextView textView;
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
        originalVideoUri = getIntent().getStringExtra("originalVideoUri");
        frameUriStr = getIntent().getStringExtra("frameUriStr");
        pathJsonStr = getIntent().getStringExtra("pathJsonStr");
        textSource = getIntent().getStringExtra("textSource");

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        videoView = findViewById(R.id.videoView);

        textView = findViewById(R.id.textView);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        displaySelectedVideo(Uri.parse(videoUri));

        recyclerView = findViewById(R.id.recyclerView);
        itemSize = calculateItemSize(spanCount);

        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        imageAdapter = new ImageAdapter(imagesUri, itemSize);
        imageAdapter.setOnImageClickListener(this);
        recyclerView.setAdapter(imageAdapter);

        imageAdapter.setSelectedItemPosition(index);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            saveVideoToGallery(this, Uri.parse(videoUri));
            textView.setText("已保存");
            Log.d(TAG, "saved");

        } else if (v.getId() == R.id.cancelButton) {
            // TODO: 返回主页面
            Intent intent = new Intent(this, AddMaterialActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onImageClick(int position) {
        // 用户点击图片时
        index = position;
        imageAdapter.setSelectedItemPosition(index);
        textView.setText("生成中...");
        MyRequester.newThreadAndSendRequest(new ResponseCallback() {
                                                @Override
                                                public void onSuccess(CustomResponse response) {
                                                    Log.d(TAG, "onSuccess callback");
                                                    try {
                                                        videoUri = MyConverter.convertVideoToUri(DisplayResponseActivity.this, response.getVideo());
                                                        displaySelectedVideo(Uri.parse(videoUri));
                                                    } catch (Exception e) {
                                                        Log.e(TAG, "convert video: " + e.getMessage());
                                                    }
                                                }

                                                @Override
                                                public void onError(String errorMessage) {
                                                    Log.e(TAG, "onError: " + errorMessage);
                                                }
                                            }, this, getContentResolver(),
                originalVideoUri, frameUriStr,
                null, imagesUri.get(index),
                pathJsonStr, textSource);
    }

//    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void saveVideoToGallery(Context context, Uri videoUri) {
        try {
            ContentResolver contentResolver = context.getContentResolver();

            ContentValues values = new ContentValues();
            values.put(MediaStore.Video.Media.DISPLAY_NAME, "MyVideo.mp4");
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
            values.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            Uri videoUriInMediaStore = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);

            if (videoUriInMediaStore != null) {
                copyFileToMediaStore(context, videoUri, videoUriInMediaStore);
            } else {
                Log.e("VideoUtils", "Error inserting video into MediaStore.");
            }
        } catch (Exception e) {
            Log.e("VideoUtils", "Error saving video: " + e.getMessage());
        }
    }

    private static void copyFileToMediaStore(Context context, Uri sourceUri, Uri destUri) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(context.getContentResolver().openFileDescriptor(sourceUri, "r").getFileDescriptor());
             FileOutputStream outputStream = new FileOutputStream(context.getContentResolver().openFileDescriptor(destUri, "w").getFileDescriptor())) {

            FileChannel inChannel = inputStream.getChannel();
            FileChannel outChannel = outputStream.getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
    }


    private void displaySelectedVideo(Uri videoUri) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoView.setVideoURI(videoUri);
                videoView.requestFocus();
                videoView.start();

                textView.setText("点击下方图片可生成对应视频");
            }
        });
    }

    private int calculateItemSize(int spanCount) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;
        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        return (screenWidth - (spanCount - 1) * itemSpacing) / spanCount;
    }

}