package com.example.videogeneration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.adsapplication.R;
import com.example.videogeneration.utils.MyConverter;
import com.example.videogeneration.utils.MyRequester;
import com.example.videogeneration.utils.models.CustomResponse;
import com.example.videogeneration.utils.models.ResponseCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SendingActivity extends AppCompatActivity {

    private final String TAG = "ww";

    private static final int REQUEST_PERMISSION = 123;

    ResponseCallback callback;

    private String videoUriStr;    // 裁剪后的视频
    private String frameUriStr;    // 视频第一帧
    private String pathJsonStr;    // 绘制的路径
    private String imageSourceUriJsonStr;    // 添加的图片素材
    private String textSource;    // 添加的文本素材
    private String startMillis, endMillis;

    private List<String> imagesUri;
    private String videoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        callback = new ResponseCallback() {
            @Override
            public void onSuccess(CustomResponse response) {
                handleOnSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                handleOnError(errorMessage);
            }
        };

        videoUriStr = getIntent().getStringExtra("videoUriStr");
        frameUriStr = getIntent().getStringExtra("frameUriStr");
        imageSourceUriJsonStr = getIntent().getStringExtra("imageSourceUriJsonStr");
        pathJsonStr = getIntent().getStringExtra("pathJsonStr");
        textSource = getIntent().getStringExtra("textSource");
        startMillis = getIntent().getStringExtra("startMillis");
        endMillis = getIntent().getStringExtra("endMillis");

        // 检查是否已经授予了所需的权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 如果权限没有被授予，请求权限
            Log.d(TAG, "requestPermissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    REQUEST_PERMISSION);
        } else {
            // 权限已被授予，新建线程发送请求
            readyToRequest();
        }
    }

    // 处理权限请求的结果
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // 权限已被授予，新建线程发送请求
                readyToRequest();
            } else {
                // 权限被拒绝，可能需要提示用户或执行其他操作
                Log.e(TAG, "NoPermissions");
            }
        }
    }

    private void readyToRequest() {

        Map<String, String> params = new HashMap<>();
        params.put("mask", pathJsonStr);
        params.put("text_prompt", textSource);
        params.put("startMillis", startMillis);
        params.put("endMillis", endMillis);

        String url = "http://10.25.6.55:80/aigc";

        MyRequester.newThreadAndSendRequest(callback, this, getContentResolver(),
                videoUriStr, frameUriStr,
                imageSourceUriJsonStr, null,
                params, url);
    }

    private void handleOnSuccess(CustomResponse response) {

        Log.d(TAG, "onSuccess callback");

        String status = response.getStatus();
        String message = response.getMessage();
        if (status != null && status.equals("Success")) {

            String video = response.getVideo();
            List<String> images = response.getImages();
            try {
                imagesUri = MyConverter.convertBase64ImagesToUris(SendingActivity.this, images);
                videoUri = MyConverter.convertVideoToUri(SendingActivity.this, video);

            } catch (Exception e) {
                Log.e(TAG, "convert images and video: " + e.getMessage());
            }

            Intent intent = new Intent(this, DisplayResponseActivity.class);
            intent.putStringArrayListExtra("imagesUri", new ArrayList<>(imagesUri));
            intent.putExtra("videoUri", videoUri);
            intent.putExtra("originalVideoUri", videoUriStr);
            intent.putExtra("startMillis", String.valueOf(startMillis));
            intent.putExtra("endMillis", String.valueOf(endMillis));
            intent.putExtra("frameUriStr", frameUriStr);
            intent.putExtra("pathJsonStr", pathJsonStr);
            intent.putExtra("textSource", textSource);
            startActivity(intent);

        } else {
            Log.e(TAG, status + ": " + message);
        }
    }

    private void handleOnError(String errorMessage) {
        Log.e(TAG, "handleOnError: " + errorMessage);
    }

}