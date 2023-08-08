package com.example.adsapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.adsapplication.utils.MyConverter;
import com.example.adsapplication.utils.models.CustomResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

public class SendingActivity extends AppCompatActivity {

    private final String TAG = "ww";

    private static final int REQUEST_PERMISSION = 123;

    private String croppedVideoUriStr;    // 裁剪后的视频
    private String frameUriStr;    // 视频第一帧
    private String pathJsonStr;    // 绘制的路径
    private String imageSourceUriJsonStr;    // 添加的图片素材
    private String textSource;    // 添加的文本素材

    private CustomResponse response;    // 接收到的服务器响应数据
    private List<String> imagesUri;
    private String videoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        croppedVideoUriStr = getIntent().getStringExtra("croppedVideoUriStr");
        frameUriStr = getIntent().getStringExtra("frameUriStr");
        imageSourceUriJsonStr = getIntent().getStringExtra("imageSourceUriJsonStr");
        pathJsonStr = getIntent().getStringExtra("pathJsonStr");
        textSource = getIntent().getStringExtra("textSource");

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
            // 权限已被授予，执行您的文件操作
            newThreadAndSendRequest();
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
                // 权限已被授予，执行文件操作
                newThreadAndSendRequest();
            } else {
                // 权限被拒绝，可能需要提示用户或执行其他操作
                Log.e(TAG, "NoPermissions");
            }
        }
    }

    private void newThreadAndSendRequest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendRequest();
                } catch (Exception e) {
                    Log.e(TAG, "exception in click run: " + e.getMessage());
                }
            }
        }).start();
    }

    private void sendRequest() {

        List<File> imageSourceFiles = MyConverter.getFileListFromJson(SendingActivity.this, imageSourceUriJsonStr, getContentResolver());
        File frameFile = MyConverter.getImageFileFromUri(SendingActivity.this, Uri.parse(frameUriStr), "frame.png", getContentResolver());
        File videoFile = MyConverter.getVideoFileFromUri(SendingActivity.this, Uri.parse(croppedVideoUriStr));

        Map<String, String> params = new HashMap<>();
        params.put("mask", pathJsonStr);
        params.put("text_prompt", textSource);

        String url = "http://10.25.6.55:80/aigc";

        postADS(url, params, frameFile, imageSourceFiles, videoFile)
                .thenAccept(customResponse -> {

                    response = customResponse;
                    String status = response.getStatus();
                    String message = response.getMessage();
                    if (status.equals("Success")) {

                        String video = response.getVideo();
                        List<String> images = response.getImages();
                        try {
                            imagesUri = MyConverter.convertBase64ImagesToUris(SendingActivity.this, images);
                            videoUri = MyConverter.convertVideoToUri(SendingActivity.this, video);
                            Log.d(TAG, "imagesUri: " + imagesUri);
                            Log.d(TAG, "videoUri: " + videoUri);
                        } catch (Exception e) {
                            Log.e(TAG, "convert images and video: " + e.getMessage());
                        }

                        Intent intent = new Intent(this, DisplayResponseActivity.class);
                        intent.putStringArrayListExtra("imagesUri", new ArrayList<>(imagesUri));
                        intent.putExtra("videoUri", videoUri);
                        startActivity(intent);

                    } else {
                        Log.e(TAG, status + ": " + message);
                    }

                })
                .exceptionally(e -> {
                    // 处理异常
                    Log.e(TAG, "sendRequest: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                });
    }

    private CompletableFuture<CustomResponse> postADS(
            String url, Map<String, String> params, File imageFile, List<File> imageFiles, File videoFile) {

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        if (videoFile != null) {
            multipartBuilder.addFormDataPart("video", videoFile.getName(),
                    RequestBody.create(MediaType.parse("video/*"), videoFile));
        }
        if (imageFile != null) {
            multipartBuilder.addFormDataPart("image", imageFile.getName(),
                    RequestBody.create(MediaType.parse("image/*"), imageFile));
        }
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (File image : imageFiles) {
                if (image != null && image.exists()) {
                    multipartBuilder.addFormDataPart("image", image.getName(),
                            RequestBody.create(MediaType.parse("image/*"), image));
                }
            }
        }

        RequestBody requestBody = multipartBuilder.build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        Log.d(TAG, "built");

        CompletableFuture<CustomResponse> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {

                    Log.d(TAG, "received");

                    CustomResponse customResponse = new CustomResponse();
                    ResponseBody responseBody = response.body();

                    String responseString = responseBody.string();
//                    Log.d(TAG, "responseString" + responseString);
                    try {
                        JSONObject jsonObject = new JSONObject(responseString);

                        // 获取状态码
                        String statusKey = "Status";
                        if (jsonObject.has(statusKey)) {
                            String status = jsonObject.getString(statusKey);
                            if (status != null) {
                                customResponse.setStatus(status);
                            }
                        } else {
                            Log.d(TAG, statusKey + " not found in reply");
                        }

                        // 获取消息
                        String messageKey = "Message";
                        if (jsonObject.has(messageKey)) {
                            String message = jsonObject.getString(messageKey);
                            if (message != null) {
                                customResponse.setMessage(message);
                            }
                        } else {
                            Log.d(TAG, messageKey + " not found in reply");
                        }

                        // 获取视频
                        String videoKey = "VideoBytes";
                        if (jsonObject.has(videoKey)) {
                            String video = jsonObject.getString(videoKey);
                            if (video != null) {
                                customResponse.setVideo(video);
                            }
                        } else {
                            Log.d(TAG, videoKey + " not found in reply");
                        }

                        // 获取图像列表
                        String imagesKey = "ImageBytes";
                        if (jsonObject.has(imagesKey)) {
                            JSONArray imageArray = jsonObject.getJSONArray(imagesKey);
                            if (imageArray != null) {
                                List<String> images = new ArrayList<>();
                                for (int i = 0; i < imageArray.length(); i++) {
                                    String image = imageArray.getString(i);
                                    images.add(image);
                                }
                                customResponse.setImages(images);
                            }
                        } else {
                            Log.d(TAG, imagesKey + " not found in reply");
                        }

                        Log.d(TAG, "handled");
                    } catch (JSONException e) {
                        Log.e(TAG, "fail to convert to JSONObject: " + e.getMessage());

                    }

                    future.complete(customResponse);

                } catch (Exception e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

}