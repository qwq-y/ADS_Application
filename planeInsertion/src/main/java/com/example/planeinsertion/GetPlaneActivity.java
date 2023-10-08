package com.example.planeinsertion;

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

import com.example.planeinsertion.utils.MyConverter;
import com.example.planeinsertion.utils.MyRequester;
import com.example.planeinsertion.utils.models.CustomResponse;
import com.example.planeinsertion.utils.models.Point;
import com.example.planeinsertion.utils.models.ResponseCallback;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 这里在选择 mask 的时候申请了运行时权限（并且回调函数也是 mask 相关），所以必须选 mask。
// TODO: 第一帧图片适应屏幕大小

public class GetPlaneActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private String TAG = "ww";

    private static final int REQUEST_PERMISSION = 123;
    private boolean isPermitted = false;
    private boolean hasMask = false;
    private ResponseCallback maskCallback;

    private ImageView imageView;
    private Button retryButton;
    private Button okButton;
    private TextView textView;
    private AlertDialog alertDialog;

    private String user = "lzl";

    private String videoUriStr;    // 视频
    private String frameUriStr;    // 视频第一帧
    private String startMillis, endMillis;

    private int x, y;
    private List<Point> points = new ArrayList<>();    // 用户的累积点击

    private String fourChannelImageUriStr;     // 四通道图
    private String paintedImageUriStr;    // 带掩码的视频第一帧（用于给用户预览）

    private GestureDetector gestureDetector;

    int imageViewX, imageViewY, imageViewWidth, imageViewHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_plane);

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
        builder.setTitle("分析中...");
        builder.setMessage("");
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
            // TODO: 处理没有mask的情况（提示用户）
            if (hasMask) {
                Intent intent = new Intent(this, AddVideoActivity.class);
                intent.putExtra("videoUriStr", videoUriStr);
                intent.putExtra("startMillis", startMillis);
                intent.putExtra("endMillis", endMillis);
                intent.putExtra("frameUriStr", frameUriStr);
                intent.putExtra("fourChannelImageUriStr", fourChannelImageUriStr);
                startActivity(intent);
            }
        } else if (view.getId() == R.id.retryButton) {
            points = new ArrayList<>();
            fourChannelImageUriStr = null;
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
        Gson gson = new Gson();
        String imageFilesUriJsonStr = gson.toJson(imageFilesUri);

        Map<String, String> params = new HashMap<>();
        params.put("user", user);
        params.put("type", "plane");
        params.put("point_prompt", MyConverter.getPointPromptStr(points));
        params.put("points", MyConverter.getPointsStr(points));

        String url = "http://172.18.36.110:5005/SegmentFirstFrame";

        MyRequester.newThreadAndSendRequest(maskCallback, this, getContentResolver(),
                null, null, imageFilesUriJsonStr,
                params, url);
    }

    private void handleOnMaskSuccess(CustomResponse response) {

        Log.d(TAG, "onSuccess mask callback");
        hasMask = true;

        try {

            List<String> imagesUri = MyConverter.convertBase64ImagesToUris(GetPlaneActivity.this, response.getImages());
            paintedImageUriStr = imagesUri.get(0);
            fourChannelImageUriStr = imagesUri.get(1);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageURI(Uri.parse(paintedImageUriStr));
                    textView.setText("请选取图中平面\n（单击选择区域，长按取消选择）");
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