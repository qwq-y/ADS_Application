package com.example.objectelimination;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.objectelimination.utils.models.CustomResponse;
import com.example.objectelimination.utils.models.ResponseCallback;
import com.example.planeinsertion.R;
import com.example.objectelimination.utils.MyConverter;
import com.example.objectelimination.utils.MyRequester;
import com.example.objectelimination.utils.models.Point;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


// TODO: 统一和后端的各个键名、url、图片收发顺序等
// 这里在选择 mask 的时候申请了运行时权限（并且回调函数也是 mask 相关），所以必须选 mask。

public class GetObjectActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private String TAG = "ww";

    private static final int REQUEST_PERMISSION = 123;
    private boolean isPermitted = false;
    private boolean hasMask = false;
    private ResponseCallback maskCallback;

    private ImageView imageView;
    private Button retryButton;
    private Button okButton;
    private TextView textView;
    AlertDialog alertDialog;

    private String videoUriStr;    // 视频
    private String frameUriStr;    // 视频第一帧
    private String startMillis, endMillis;

    private int x, y;
    private List<Point> points = new ArrayList<>();    // 用户的累积点击

    private String maskUriStr;     // 掩码
    private String frameWithMaskUriStr;    // 带掩码的视频第一帧（用于给用户预览）

    private String generatedVideoUriStr;    // 生成的视频

    private GestureDetector gestureDetector;

    int imageViewX, imageViewY, imageViewWidth, imageViewHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_object);

        maskCallback = new ResponseCallback() {
            @Override
            public void onSuccess(CustomResponse response) {
                handleOnMaskSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                handleOnMaskError(errorMessage);
            }
        };

        videoUriStr = getIntent().getStringExtra("videoUriStr");
        frameUriStr = getIntent().getStringExtra("frameUriStr");
        startMillis = getIntent().getStringExtra("startMillis");
        endMillis = getIntent().getStringExtra("endMillis");

        gestureDetector = new GestureDetector(this, new MyGestureListener());

        imageView = findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(frameUriStr));
        imageView.setOnTouchListener(this);

        textView = findViewById(R.id.textView);

        retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(this);

        okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("加载中");
        builder.setMessage("正在处理点击事件，请稍等...");
        builder.setCancelable(false);  // 设置对话框不可取消
        alertDialog = builder.create();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            setImageViewInfo();
        }
    }

    private void setImageViewInfo() {
        int[] location = new int[2];
        imageView.getLocationOnScreen(location);
        imageViewX = location[0];
        imageViewY = location[1];
        imageViewWidth = imageView.getWidth();
        imageViewHeight = imageView.getHeight();
//        Log.d(TAG, "image view info: \n" + imageViewX + "\n" + imageViewY + "\n" + imageViewWidth + "\n" + imageViewHeight);
//        Log.d(TAG, "[" + imageViewX + ", " + (imageViewX + imageViewWidth) + "][" + imageViewY + ", " + (imageViewY + imageViewHeight) + "]");
    }

    private boolean isCoordinateInsideImage(int x, int y) {
        return x >= 0 && x <= imageViewWidth && y >= 0 && y <= imageViewHeight;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.okButton) {
            if (hasMask) {
                // 把确定好的掩码、视频、视频第一帧发送给后端，生成视频

                List<String> imageFilesUri = new ArrayList<>();
                if (frameUriStr != null) {
                    imageFilesUri.add(frameUriStr);
                }
                if (maskUriStr != null) {
                    imageFilesUri.add(maskUriStr);
                } else {
                    imageFilesUri.add(frameUriStr);
                }
                Gson gson = new Gson();
                String imageFilesUriJsonStr = gson.toJson(imageFilesUri);

                Map<String, String> params = new HashMap<>();
                params.put("startMillis", startMillis);
                params.put("endMillis", endMillis);

                String url = "http://10.25.6.55:80/aigc";

                Log.d(TAG, "prepared");

                MyRequester.newThreadAndSendRequest(new ResponseCallback() {
                                                        @Override
                                                        public void onSuccess(CustomResponse response) {
                                                            Log.d(TAG, "onSuccess callback");
                                                            try {
                                                                String video = response.getVideo();
                                                                generatedVideoUriStr = MyConverter.convertVideoToUri(GetObjectActivity.this, video);
                                                            } catch (Exception e) {
                                                                Log.e(TAG, "convert video: " + e.getMessage());
                                                            }
                                                        }

                                                        @Override
                                                        public void onError(String errorMessage) {
                                                            Log.e(TAG, "onError callback: " + errorMessage);
                                                        }
                                                    }, this, getContentResolver(),
                        videoUriStr, null,
                        imageFilesUriJsonStr,
                        params, url);

                Intent intent = new Intent(this, DisplayResultActivity.class);
                intent.putExtra("generatedVideoUriStr", generatedVideoUriStr);
                startActivity(intent);
            }
        } else if (view.getId() == R.id.retryButton) {
            points = new ArrayList<>();
            maskUriStr = null;
            imageView.setImageURI(Uri.parse(frameUriStr));
        }
    }

    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.imageView) {
            gestureDetector.onTouchEvent(event);
            x = (int) event.getX();
            y = (int) event.getY();

            if (isCoordinateInsideImage(x, y)) {
                Log.d(TAG, "坐标：x = " + x + ", y = " + y);
            } else {
                Log.d(TAG, "不在图片范围内");
            }
            return true;
        }
        return false;
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent event) {
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            Point point = new Point(x, y, 1);
            points.add(point);
            processClickImage();
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event) {
            Point point = new Point(x, y, 0);
            points.add(point);
            processClickImage();
            return true;
        }
    }

    private void processClickImage() {
        alertDialog.show();

        textView.setText("识别中\n（单击选择区域，长按取消选择）");

        // 检查是否已经授予了所需的权限
        if (isPermitted || (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
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
            isPermitted = true;
            readyToRequestMask();
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
                isPermitted = true;
                readyToRequestMask();
            } else {
                // 权限被拒绝，可能需要提示用户或执行其他操作
                isPermitted = false;
                Log.e(TAG, "NoPermissions");
            }
        }
    }

    private void readyToRequestMask() {
        List<String> imageFilesUri = new ArrayList<>();
        imageFilesUri.add(frameUriStr);
        if (maskUriStr != null) {
            imageFilesUri.add(maskUriStr);
        }
        Gson gson = new Gson();
        String imageFilesUriJsonStr = gson.toJson(imageFilesUri);

        Map<String, String> params = new HashMap<>();
        String pointsJsonStr = gson.toJson(points);
        params.put("pointsJsonStr", pointsJsonStr);

        String url = "http://10.25.6.55:80/inpaint";

        MyRequester.newThreadAndSendRequest(maskCallback, this, getContentResolver(),
                null, null, imageFilesUriJsonStr,
                params, url);
    }

    private void handleOnMaskSuccess(CustomResponse response) {

        Log.d(TAG, "onSuccess callback");
        hasMask = true;

        try {

            List<String> imagesUri = MyConverter.convertBase64ImagesToUris(GetObjectActivity.this, response.getImages());
            maskUriStr = imagesUri.get(0);
            frameWithMaskUriStr = imagesUri.get(1);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageURI(Uri.parse(frameWithMaskUriStr));
                }
            });

            alertDialog.dismiss();

        } catch (Exception e) {
            Log.e(TAG, "convert images: " + e.getMessage());
        }
    }

    private void handleOnMaskError(String errorMessage) {
        Log.e(TAG, "onError callback: " + errorMessage);
    }

}