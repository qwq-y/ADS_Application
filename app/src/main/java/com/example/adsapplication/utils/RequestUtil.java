//package com.example.adsapplication.utils;
//
//import android.Manifest;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.net.Uri;
//import android.provider.MediaStore;
//import android.util.Log;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.example.adsapplication.DisplayResponseActivity;
//import com.example.adsapplication.SendingActivity;
//import com.example.adsapplication.utils.models.CustomResponse;
//import com.google.gson.Gson;
//import com.google.gson.reflect.TypeToken;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.lang.reflect.Type;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.CompletableFuture;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.MediaType;
//import okhttp3.MultipartBody;
//import okhttp3.OkHttpClient;
//import okhttp3.RequestBody;
//import okhttp3.ResponseBody;
//
//public class RequestUtil extends AppCompatActivity {
//
//    private final String TAG = "ww";
//    private static final int REQUEST_PERMISSION = 123;
//    private CustomResponse response;
//
//    private CustomResponse sendADSRequest() {
//        // 检查是否已经授予了所需的权限
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
//                ContextCompat.checkSelfPermission(this,
//                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            // 如果权限没有被授予，请求权限
//            Log.d(TAG, "requestPermissions");
//            ActivityCompat.requestPermissions(this,
//                    new String[]{
//                            Manifest.permission.READ_EXTERNAL_STORAGE,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    },
//                    REQUEST_PERMISSION);
//        } else {
//            // 权限已被授予，执行您的文件操作
//            newThreadAndSendRequest();
//        }
//    }
//
//    private void newThreadAndSendRequest() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    sendRequest();
//                } catch (Exception e) {
//                    Log.e(TAG, "exception in click run: " + e.getMessage());
//                }
//            }
//        }).start();
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSION) {
//            if (grantResults.length > 0 &&
//                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
//                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
//                // 权限已被授予，执行文件操作
//                newThreadAndSendRequest();
//            } else {
//                // 权限被拒绝，可能需要提示用户或执行其他操作
//                Log.e(TAG, "NoPermissions");
//            }
//        }
//    }
//
//    private void sendRequest() {
//
//        List<File> imageSourceFiles = getFileListFromJson(imageSourceUriJsonStr);
//        File frameFile = getImageFileFromUri(SendingActivity.this, Uri.parse(frameUriStr));
//        File videoFile = getVideoFileFromUri(SendingActivity.this, Uri.parse(croppedVideoUriStr));
//        byte[] pickleData = getPickleData(imageSourceFiles, frameFile);
//
//        Map<String, String> params = new HashMap<>();
//        params.put("mask", pathJsonStr);
//        params.put("text_prompt", textSource);
//
//        String url = "http://10.25.6.55:80/aigc";
//
//        postADS(url, params, frameFile, imageSourceFiles, videoFile, pickleData)
//                .thenAccept(customResponse -> {
//
//                    response = customResponse;
//                    Intent intent = new Intent(this, DisplayResponseActivity.class);
//                    intent.putExtra("response", response);
////                    startActivity(intent);
//
//                })
//                .exceptionally(e -> {
//                    // 处理异常
//                    Log.e(TAG, "sendRequest: " + e.getMessage());
//                    e.printStackTrace();
//                    return null;
//                });
//    }
//
//    private CompletableFuture<CustomResponse> postADS(String url, Map<String, String> params, File imageFile, List<File> imageFiles, File videoFile, byte[] pickleData) {
//        OkHttpClient client = new OkHttpClient();
//
//        MultipartBody.Builder multipartBuilder = new MultipartBody.Builder()
//                .setType(MultipartBody.FORM);
//
//        if (params != null) {
//            for (Map.Entry<String, String> entry : params.entrySet()) {
//                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
//            }
//        }
//        if (videoFile != null) {
//            multipartBuilder.addFormDataPart("video", videoFile.getName(),
//                    RequestBody.create(MediaType.parse("video/*"), videoFile));
//        }
////        if (pickleData != null) {
////            multipartBuilder.addFormDataPart("encoded_images", "encoded_images",
////                    RequestBody.create(MediaType.parse("application/octet-stream"), pickleData));
////        }
//        if (imageFile != null) {
//            multipartBuilder.addFormDataPart("image", imageFile.getName(),
//                    RequestBody.create(MediaType.parse("image/*"), imageFile));
//        }
//        if (imageFiles != null && !imageFiles.isEmpty()) {
//            for (File image : imageFiles) {
//                if (image != null && image.exists()) {
//                    multipartBuilder.addFormDataPart("image", image.getName(),
//                            RequestBody.create(MediaType.parse("image/*"), image));
//                }
//            }
//        }
//
//        RequestBody requestBody = multipartBuilder.build();
//        okhttp3.Request request = new okhttp3.Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .build();
//
//        Log.d(TAG, "built");
//
//        CompletableFuture<CustomResponse> future = new CompletableFuture<>();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onResponse(Call call, okhttp3.Response response) throws IOException {
//                try {
//
//                    Log.d(TAG, "received");
//
//                    CustomResponse customResponse = new CustomResponse();
//                    ResponseBody responseBody = response.body();
//
//                    String responseString = responseBody.string();
////                    Log.d(TAG, "responseString" + responseString);
//                    try {
//                        JSONObject jsonObject = new JSONObject(responseString);
//
//                        // 获取状态码
//                        String statusKey = "Status";
//                        if (jsonObject.has(statusKey)) {
//                            String status = jsonObject.getString(statusKey);
//                            if (status != null) {
//                                customResponse.setStatus(status);
//                            }
//                        } else {
//                            Log.d(TAG, statusKey + " not found in reply");
//                        }
//
//                        // 获取消息
//                        String messageKey = "Message";
//                        if (jsonObject.has(messageKey)) {
//                            String message = jsonObject.getString(messageKey);
//                            if (message != null) {
//                                customResponse.setMessage(message);
//                            }
//                        } else {
//                            Log.d(TAG, messageKey + " not found in reply");
//                        }
//
//                        // 获取视频
//                        String videoKey = "VideoBytes";
//                        if (jsonObject.has(videoKey)) {
//                            String video = jsonObject.getString(videoKey);
//                            if (video != null) {
//                                customResponse.setVideo(video);
//                            }
//                        } else {
//                            Log.d(TAG, videoKey + " not found in reply");
//                        }
//
//                        // 获取图像列表
//                        String imagesKey = "ImageBytes";
//                        if (jsonObject.has(imagesKey)) {
//                            JSONArray imageArray = jsonObject.getJSONArray(imagesKey);
//                            if (imageArray != null) {
//                                List<String> images = new ArrayList<>();
//                                for (int i = 0; i < imageArray.length(); i++) {
//                                    String image = imageArray.getString(i);
//                                    images.add(image);
//                                }
//                                customResponse.setImages(images);
//                            }
//                        } else {
//                            Log.d(TAG, imagesKey + " not found in reply");
//                        }
//
//                        Log.d(TAG, "handled!");
//                    } catch (JSONException e) {
//                        Log.e(TAG, "fail to convert to JSONObject: " + e.getMessage());
//
//                    }
//
//                    future.complete(customResponse);
//
//                } catch (Exception e) {
//                    future.completeExceptionally(e);
//                }
//            }
//
//            @Override
//            public void onFailure(Call call, IOException e) {
//                future.completeExceptionally(e);
//            }
//        });
//
//        return future;
//    }
//
//    private File getImageFileFromUri(Context context, Uri uri) {
//
//        File file = null;
//
//        try {
//            InputStream inputStream = getContentResolver().openInputStream(uri);
//            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//            inputStream.close();
//
//            ByteArrayOutputStream stream = new ByteArrayOutputStream();
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
//            byte[] byteArray = stream.toByteArray();
//
//            file = new File(context.getCacheDir(), "frame.png");
//
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(byteArray);
//            fos.flush();
//            fos.close();
//
//        } catch (Exception e) {
//            Log.e(TAG, "stream: " + e.getMessage());
//        }
//
//        return file;
//    }
//
//    private File getVideoFileFromUri(Context context, Uri uri) {
//        String[] projection = {MediaStore.Video.Media.DATA};
//        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
//            if (cursor != null && cursor.moveToFirst()) {
//                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
//                String filePath = cursor.getString(columnIndex);
//                return new File(filePath);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//
//    private List<File> getFileListFromJson(String jsonStr) {
//
//        Gson gson = new Gson();
//        Type listType = new TypeToken<List<String>>() {}.getType();
//
//        List<String> stringList = gson.fromJson(jsonStr, listType);
//
//        List<File> fileList = new ArrayList<>();
//        for (String uriString : stringList) {
//            File imageFile = new File(uriString);
//            fileList.add(imageFile);
//        }
//
//        return fileList;
//    }
//}
