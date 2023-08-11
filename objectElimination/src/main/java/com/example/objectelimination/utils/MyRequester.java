package com.example.objectelimination.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.objectelimination.utils.models.CustomResponse;
import com.example.objectelimination.utils.models.ResponseCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyRequester {

    private static final String TAG = "ww";

    public static void newThreadAndSendRequest(ResponseCallback callback, Context context, ContentResolver resolver,
                                               String croppedVideoUriStr, String sourceVideoUriStr,
                                               String imageFilesUriJsonStr,
                                               Map<String, String> params, String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendRequest(context, resolver,
                            croppedVideoUriStr, sourceVideoUriStr,
                            imageFilesUriJsonStr,
                            params, url)
                            .thenAccept(customResponse -> {

                                callback.onSuccess(customResponse);

                            })
                            .exceptionally(e -> {

                                callback.onError(e.getMessage());
                                return null;

                            });
                } catch (Exception e) {
                    Log.e(TAG, "newThreadAndSendRequest: " + e.getMessage());
                }
            }
        }).start();
    }

    private static CompletableFuture<CustomResponse> sendRequest(Context context, ContentResolver resolver,
                                                                 String croppedVideoUriStr, String sourceVideoUriStr,
                                                                 String imageFilesUriJsonStr,
                                                                 Map<String, String> params, String url) {

        Log.d(TAG, "to sendRequest");

        CompletableFuture<CustomResponse> future = new CompletableFuture<>();

        List<File> videoFiles = new ArrayList<>();
        if (croppedVideoUriStr != null) {
            File croppedVideo = MyConverter.getVideoFileFromUri(context, Uri.parse(croppedVideoUriStr));
            videoFiles.add(croppedVideo);
        }
        if (sourceVideoUriStr != null) {
            File sourceVideo = MyConverter.getVideoFileFromUri(context, Uri.parse(sourceVideoUriStr));
            videoFiles.add(sourceVideo);
        }

        List<File> imageFiles = null;
        if (imageFilesUriJsonStr != null) {
            imageFiles = MyConverter.getFileListFromJson(context, imageFilesUriJsonStr, resolver);;
        }

        postADS(url, params, videoFiles, imageFiles)
                .thenAccept(customResponse -> {

                    future.complete(customResponse);

                })
                .exceptionally(e -> {

                    Log.e(TAG, "sendRequest: " + e.getMessage());
                    future.completeExceptionally(e);
                    return null;

                });

        return future;
    }

    private static CompletableFuture<CustomResponse> postADS(
            String url, Map<String, String> params,
            List<File> videoFiles, List<File> imageFiles) {

        Log.d(TAG, "to postADS");

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // 填写要发送的数据
        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
            }
        }
        if (videoFiles != null) {
            for (File video : videoFiles) {
                if (video != null && video.exists()) {
                    multipartBuilder.addFormDataPart("video", video.getName(),
                            RequestBody.create(MediaType.parse("video/*"), video));
                }
            }
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

        CompletableFuture<CustomResponse> future = new CompletableFuture<>();

        // 异步发送请求
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d(TAG, "received");

                try {
                    if (response.isSuccessful()) {

                        String responseBody = response.body().string();
                        CustomResponse customResponse = parseCustomResponse(responseBody);
                        Log.d(TAG, "handled");
                        future.complete(customResponse);

                    } else {
                        future.completeExceptionally(new Exception("Response not successful"));
                    }
                } catch (Exception e) {
                    future.completeExceptionally(e);                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }
        });

        return future;
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

        } catch (JSONException e) {
            Log.e(TAG, "fail to convert to JSONObject: " + e.getMessage());

        }

        return customResponse;
    }

}
