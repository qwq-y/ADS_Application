package com.example.objectelimination;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.VideoView;

import com.example.planeinsertion.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class DisplayResultActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "ww";

    private String generatedVideoUriStr;    // 生成的视频

    private VideoView videoView;

    Button saveButton;
    Button cancelButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);

        generatedVideoUriStr = getIntent().getStringExtra("generatedVideoUriStr");

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        videoView.setVideoURI(Uri.parse(generatedVideoUriStr));
        videoView.requestFocus();
        videoView.start();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveButton) {
            saveVideoToGallery(this, Uri.parse(generatedVideoUriStr));
            Log.d(TAG, "saved");
            exit();
        } else if (v.getId() == R.id.cancelButton) {
            exit();
        }
    }

    private void exit() {
        // TODO: 返回主页面
        Intent intent = new Intent(this, ChooseVideoActivity.class);
        startActivity(intent);
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
}