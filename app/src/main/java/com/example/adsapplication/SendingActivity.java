package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.example.adsapplication.models.CustomResponse;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

public class SendingActivity extends AppCompatActivity {

    private final String TAG = "ww";

    private String croppedVideoUriStr;    // 裁剪后的视频 uri
    private String frameUriStr;    // 视频第一帧 uri
    private String pathJsonStr;    // 绘制的路径
    private String imageSourcePath;    // 添加的图片素材路径
    private String textSource;    // 添加的文本素材

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        croppedVideoUriStr = getIntent().getStringExtra("croppedVideoUriStr");
        frameUriStr = getIntent().getStringExtra("frameUriStr");
        pathJsonStr = getIntent().getStringExtra("pathJsonStr");
        imageSourcePath = getIntent().getStringExtra("imageSourcePath");
        textSource = getIntent().getStringExtra("textSource");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendRequest();
                } catch (Exception e) {
                    Log.d(TAG, "exception in click run: " + e.getMessage());
                }
            }
        }).start();

    }

    private void sendRequest() {
        File imageSourceFile = new File(imageSourcePath);
        File frameFile = getImageFileFromUri(SendingActivity.this, Uri.parse(frameUriStr));
        File videoFile = getVideoFileFromUri(SendingActivity.this, Uri.parse(croppedVideoUriStr));

        Map<String, String> params = new HashMap<>();
        params.put("pathJsonStr", pathJsonStr);
        params.put("textSource", textSource);

        String url = "http://172.18.36.107:1200/video";

        postADS(url, params, null, videoFile)
                .thenAccept(customResponse -> {
                    File video = customResponse.getVideo();

                    if (video != null) {

                    }
                })
                .exceptionally(e -> {
                    // 处理异常
                    Log.d(TAG, "sendRequest: " + e.getMessage());
                    return null;
                });
    }

    private CompletableFuture<CustomResponse> postADS(String url, Map<String, String> params, File imageFile, File videoFile) {
        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        if (imageFile != null) {
            multipartBuilder.addFormDataPart("image", imageFile.getName(),
                    RequestBody.create(MediaType.parse("image/*"), imageFile));
        }
        if (videoFile != null) {
            multipartBuilder.addFormDataPart("video", videoFile.getName(),
                    RequestBody.create(MediaType.parse("video/*"), videoFile));
        }

        RequestBody requestBody = multipartBuilder.build();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        CompletableFuture<CustomResponse> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    CustomResponse customResponse = new CustomResponse();
                    ResponseBody responseBody = response.body();

                    String filePath = SendingActivity.this.getFilesDir() + File.separator + "video.mp4";
                    File videoFile = new File(filePath);
                    BufferedSink bufferedSink = Okio.buffer(Okio.sink(videoFile));
                    bufferedSink.writeAll(responseBody.source());
                    bufferedSink.close();

                    customResponse.setVideo(videoFile);

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


    private File getImageFileFromUri(Context context, Uri uri) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return new File(filePath);
        }
        return null;
    }

    private File getVideoFileFromUri(Context context, Uri uri) {
        String[] filePathColumn = { MediaStore.Video.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return new File(filePath);
        }
        return null;
    }

}