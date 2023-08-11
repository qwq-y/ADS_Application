package com.example.objectelimination.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyConverter  {

    private static final String TAG = "ww";

    public static String convertVideoToUri(Context context, String videoData) throws IOException {
        File file = saveVideoToTempFile(context, videoData);
        return Uri.fromFile(file).toString();
    }

    public static List<String> convertBase64ImagesToUris(Context context, List<String> imageList) throws IOException {
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

    public static File saveBitmapToTempFile(Context context, Bitmap bitmap) throws IOException {
        byte[] imageData = bitmapToByteArray(bitmap, Bitmap.CompressFormat.JPEG, 100);
        return saveToTempFile(context, imageData, ".jpg");
    }

    // 将Base64编码的视频数据保存为临时视频文件
    public static File saveVideoToTempFile(Context context, String videoData) throws IOException {
        byte[] videoBytes = Base64.decode(videoData, Base64.DEFAULT);
        return saveToTempFile(context, videoBytes, ".mp4");
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap, Bitmap.CompressFormat format, int quality) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static File saveToTempFile(Context context, byte[] data, String fileExtension) throws IOException {

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

    public static File getImageFileFromUri(Context context, Uri uri, String name, ContentResolver contentResolver) {

        File file = null;

        try {
            InputStream inputStream = contentResolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            file = new File(context.getCacheDir(), name);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArray);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            Log.e(TAG, "stream: " + e.getMessage());
        }

        return file;
    }

    public static File getVideoFileFromUri(Context context, Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                String filePath = cursor.getString(columnIndex);
                return new File(filePath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<File> getFileListFromJson(Context context, String jsonStr, ContentResolver contentResolver) {

        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {
        }.getType();

        List<String> stringList = gson.fromJson(jsonStr, listType);

        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < stringList.size(); i++) {
            String uriString = stringList.get(i);
            File imageFile = getImageFileFromUri(context, Uri.parse(uriString), "image_" + i, contentResolver);
            fileList.add(imageFile);
        }

        return fileList;
    }

    public static Uri getUriFromBitmap(Context context, Bitmap bitmap) {
        String fileName = "image.jpg";
        File file = new File(context.getExternalCacheDir(), fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return Uri.fromFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
