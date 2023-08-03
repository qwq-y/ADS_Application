package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.example.adsapplication.utils.models.CustomResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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

    private String croppedVideoUriStr;    // 裁剪后的视频
    private String frameUriStr;    // 视频第一帧
    private String pathJsonStr;    // 绘制的路径
    private String imageSourceUriJsonStr;    // 添加的图片素材
    private String textSource;    // 添加的文本素材

    private CustomResponse response;    // 接收到的服务器响应数据

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        croppedVideoUriStr = getIntent().getStringExtra("croppedVideoUriStr");
        frameUriStr = getIntent().getStringExtra("frameUriStr");
        imageSourceUriJsonStr = getIntent().getStringExtra("imageSourceUriJsonStr");
        pathJsonStr = getIntent().getStringExtra("pathJsonStr");
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

        List<File> imageSourceFiles = getFileListFromJson(imageSourceUriJsonStr);
        File frameFile = getImageFileFromUri(SendingActivity.this, Uri.parse(frameUriStr));
        File videoFile = getVideoFileFromUri(SendingActivity.this, Uri.parse(croppedVideoUriStr));

        // TODO: 统一键名和 url
        Map<String, String> params = new HashMap<>();
        params.put("pathJsonStr", pathJsonStr);
        params.put("textSource", textSource);

        String url = "http://172.18.36.107:1200/video";

        postADS(url, params, frameFile, imageSourceFiles, videoFile)
                .thenAccept(customResponse -> {

                    response = customResponse;
                    Intent intent = new Intent(this, DisplayResponseActivity.class);
                    intent.putExtra("response", response);
                    startActivity(intent);

                })
                .exceptionally(e -> {
                    // 处理异常
                    Log.d(TAG, "sendRequest: " + e.getMessage());
                    return null;
                });
    }

    private CompletableFuture<CustomResponse> postADS(String url, Map<String, String> params, File imageFile, List<File> imageFiles, File videoFile) {
        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);

        // TODO: 确定数据格式和关键字，测试发送

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
                    multipartBuilder.addFormDataPart("imagesList", image.getName(),
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

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, okhttp3.Response response) throws IOException {
                try {
                    CustomResponse customResponse = new CustomResponse();
                    ResponseBody responseBody = response.body();

                    // TODO: 确定数据格式和关键字，测试接收

                    String responseString = responseBody.string();
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

                    // 获取图像
                    String imageKey = "Image";
                    if (jsonObject.has(imageKey)) {
                        String image= jsonObject.getString(imageKey);
                        if (image != null) {
                            customResponse.setImage(image);
                        }
                    } else {
                        Log.d(TAG, imageKey + " not found in reply");
                    }

                    // 获取视频
                    String videoKey = "Video";
                    if (jsonObject.has(videoKey)) {
                        String video = jsonObject.getString(videoKey);
                        if (video != null) {
                            customResponse.setVideo(video);
                        }
                    } else {
                        Log.d(TAG, videoKey + " not found in reply");
                    }

                    // 获取坐标链表
                    String coordinatesKey = "Coordinates";
                    if (jsonObject.has(coordinatesKey)) {
                        JSONArray messageArray = jsonObject.getJSONArray(messageKey);
                        if (messageArray != null) {
                            List<List<Float>> coordinates = getCoordinatesListFromJsonArray(messageArray);
                            customResponse.setCoordinates(coordinates);
                        }
                    } else {
                        Log.d(TAG, coordinatesKey + " not found in reply");
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

    private static List<List<Float>> getCoordinatesListFromJsonArray(JSONArray jsonArray) throws JSONException {

        List<List<Float>> dataList = new ArrayList<>();

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONArray innerArray = jsonArray.getJSONArray(i);
            List<Float> innerList = new ArrayList<>();

            for (int j = 0; j < innerArray.length(); j++) {
                innerList.add((float) innerArray.getDouble(j));
            }

            dataList.add(innerList);
        }

        return dataList;
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

    private List<File> getFileListFromJson(String jsonStr) {

        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();

        List<String> stringList = gson.fromJson(jsonStr, listType);

        List<File> fileList = new ArrayList<>();
        for (String uriString : stringList) {
            File imageFile = new File(uriString);
            fileList.add(imageFile);
        }

        return fileList;
    }

}