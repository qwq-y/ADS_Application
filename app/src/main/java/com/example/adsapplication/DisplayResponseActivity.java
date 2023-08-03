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

public class DisplayResponseActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "ww";

    private CustomResponse response;
    private String video;    // 视频的 uri 字符串
    private List<String> images;    // 图片的 uri 字符串

    private int index = 0;    // 当前视频使用的图片序号

    private VideoView videoView;
    private Button okButton;
    private Button cancelButton;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private int itemSize;    // recyclerView 里每个 item 的宽度（用于设置高度）
    int spanCount = 4;    // recyclerView 里的列数


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_response);

        response = (CustomResponse) getIntent().getSerializableExtra("response");
        try {
            images = convertBase64ImagesToUris(DisplayResponseActivity.this, response.getImages());
            video = convertVideoToUri(DisplayResponseActivity.this, response.getVideo());
        } catch (Exception e) {
            Log.e(TAG, "get images and video: " + e.getMessage());
        }

        okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(this);

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this);

        videoView = findViewById(R.id.videoView);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        videoView.setVideoURI(Uri.parse(video));
        videoView.requestFocus();
        videoView.start();

        recyclerView = findViewById(R.id.recyclerView);
        itemSize = calculateItemSize(spanCount);

        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        imageAdapter = new ImageAdapter(images, itemSize);
        recyclerView.setAdapter(imageAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.okButton) {

        } else if (v.getId() == R.id.cancelButton) {

        }
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

    private String convertVideoToUri(Context context, String videoData) throws IOException{
        File file = saveVideoToTempFile(context, videoData);
        return Uri.fromFile(file).toString();    // TODO: fromFile可能用不成
    }

    public List<String> convertBase64ImagesToUris(Context context, List<String> imageList) throws IOException {
        List<String> uriList = new ArrayList<>();
        for (String imageStr : imageList) {
            byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            File tempFile = saveBitmapToTempFile(context, bitmap);

            Uri uri = Uri.fromFile(tempFile);
            uriList.add(uri.toString());
        }
        return uriList;
    }

    // 将Bitmap保存到临时图片文件
    public File saveBitmapToTempFile(Context context, Bitmap bitmap) throws IOException {
        byte[] imageData = bitmapToByteArray(bitmap, Bitmap.CompressFormat.JPEG, 100);
        return saveToTempFile(context, imageData, ".jpg");
    }

    // 将Base64编码的视频数据保存为临时视频文件
    public File saveVideoToTempFile(Context context, String videoData) throws IOException {
        byte[] videoBytes = Base64.decode(videoData, Base64.DEFAULT);
        return saveToTempFile(context, videoBytes, ".mp4");
    }

    // 将Bitmap转换为字节数组
    private byte[] bitmapToByteArray(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    // 将数据保存到临时文件
    private File saveToTempFile(Context context, byte[] data, String fileExtension) throws IOException {

        File cacheDir = context.getCacheDir(); // context.getExternalFilesDir(null)

        String tempFileName = "temp_" + System.currentTimeMillis() + fileExtension;
        File tempFile = new File(cacheDir, tempFileName);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(tempFile);
            outputStream.write(data);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "writeStream: " + e.getMessage());
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "closeStream: " + e.getMessage());
                }
            }
        }

        return tempFile;
    }
}