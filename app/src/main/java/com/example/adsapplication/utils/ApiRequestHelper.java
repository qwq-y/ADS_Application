package com.example.adsapplication.utils;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.adsapplication.utils.models.CustomResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiRequestHelper {

    private static final String TAG = "ww";
    private static final int REQUEST_PERMISSION = 123;

    public interface ApiResponseCallback {
        void onSuccess(CustomResponse response);
        void onError(String errorMessage);
    }

    public static void makeApiRequest(ApiResponseCallback callback, Context context, ContentResolver resolver,
                                      String croppedVideoUriStr, String frameUriStr,
                                      String imageSourceUriJsonStr, String generatedImageUriStr,
                                      String pathJsonStr, String textSource) {
        // 看是否有运行时权限
        if (hasStoragePermissions(context)) {
            // 有权限，post
             newThreadAndSendRequest(callback, context, resolver,
                     croppedVideoUriStr, frameUriStr,
                     imageSourceUriJsonStr, generatedImageUriStr,
                     pathJsonStr, textSource);
        } else {
            // 没权限，请求权限，然后在回调方法里 post
            requestStoragePermissions(context);
        }
    }

    private static boolean hasStoragePermissions(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private static void requestStoragePermissions(Context context) {
        ActivityCompat.requestPermissions((AppCompatActivity) context,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                REQUEST_PERMISSION);
    }

    private static CustomResponse parseCustomResponse(String responseBody) {
        // 解析 JSON 响应数据并返回 CustomResponse 对象
        CustomResponse customResponse = new CustomResponse();
        try {
            JSONObject jsonObject = new JSONObject(responseBody);

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
        return new CustomResponse(); // 示例，实际解析逻辑需要根据实际情况实现
    }

    private static void newThreadAndSendRequest(ApiResponseCallback callback, Context context, ContentResolver resolver,
                                         String croppedVideoUriStr, String frameUriStr,
                                         String imageSourceUriJsonStr, String generatedImageUriStr,
                                         String pathJsonStr, String textSource) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendRequest(callback, context, resolver,
                            croppedVideoUriStr, frameUriStr,
                            imageSourceUriJsonStr, generatedImageUriStr,
                            pathJsonStr, textSource);
                } catch (Exception e) {
                    Log.e(TAG, "exception in click run: " + e.getMessage());
                }
            }
        }).start();
    }

    private static void sendRequest(ApiResponseCallback callback, Context context, ContentResolver resolver,
                             String croppedVideoUriStr, String frameUriStr,
                             String imageSourceUriJsonStr, String generatedImageUriStr,
                             String pathJsonStr, String textSource) {

        File videoFile = MyConverter.getVideoFileFromUri(context, Uri.parse(croppedVideoUriStr));
        File frameFile = MyConverter.getImageFileFromUri(context, Uri.parse(frameUriStr), "frame.png", resolver);
        List<File> imageSource = MyConverter.getFileListFromJson(context, imageSourceUriJsonStr, resolver);
        File generatedImage = MyConverter.getImageFileFromUri(context, Uri.parse(generatedImageUriStr), "generated.png", resolver);

        Map<String, String> params = new HashMap<>();
        params.put("mask", pathJsonStr);
        params.put("text_prompt", textSource);

        String url = "http://10.25.6.55:80/aigc";

        postADS(callback, url, params, videoFile, frameFile, imageSource, generatedImage);
    }

    private static void postADS(
            ApiResponseCallback callback, String url, Map<String, String> params,
            File videoFile, File frameImage, List<File> imageSource, File generatedImage) {

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
        if (frameImage != null) {
            multipartBuilder.addFormDataPart("frame", frameImage.getName(),
                    RequestBody.create(MediaType.parse("image/*"), frameImage));
        }
        if (generatedImage != null) {
            multipartBuilder.addFormDataPart("generated", generatedImage.getName(),
                    RequestBody.create(MediaType.parse("image/*"), generatedImage));
        }
        if (imageSource != null && !imageSource.isEmpty()) {
            for (File image : imageSource) {
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

        // 异步发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        CustomResponse customResponse = parseCustomResponse(responseBody);
                        callback.onSuccess(customResponse);
                    } else {
                        callback.onError("Response not successful");
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onError(e.getMessage());
            }
        });
    }

}
