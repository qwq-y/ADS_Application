package com.example.adsapplication;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import org.json.JSONArray;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BrushingActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "ww";

    private ImageView imageView;
    private ImageButton brushButton;
    private Button okButton;
    private Button retryButton;
    private TextView textView;

    private String frameUriStr;
    private String croppedVideoUriStr;

    private Bitmap baseBitmap;
    private Bitmap paintedBitmap;
    private Canvas canvas;
    private Path path;
    private Paint paint;

    private List<List<Float>> pathPointsList;

    private AlertDialog brushAlertDialog;
    private float brushWidth = 25f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brushing);

        imageView = findViewById(R.id.imageView);
        try {
            croppedVideoUriStr = getIntent().getStringExtra("croppedVideoUriStr");

            Uri frameUri = Uri.parse(getIntent().getStringExtra("frameUriStr"));
            frameUriStr = frameUri.toString();

            // 转为 bitmap
            InputStream inputStream = getContentResolver().openInputStream(frameUri);
            baseBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // 复制一份用于绘制
            paintedBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(paintedBitmap);
            imageView.setImageBitmap(paintedBitmap);

        } catch (Exception e) {
            Log.e(TAG, "iamgeView: " + e.getMessage());
        }

        textView = findViewById(R.id.textView);

        brushButton = findViewById(R.id.brushButton);
        brushButton.setOnClickListener(this);

        okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(this);

        retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(this);

        path = new Path();

        pathPointsList = new ArrayList<>();
        List<Float> breakPoint = new ArrayList<>();
        breakPoint.add(-1f);
        breakPoint.add(-1f);
        pathPointsList.add(breakPoint);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(brushWidth);

    }

    private void openTouchListener() {
        imageView.setOnTouchListener(new View.OnTouchListener() {
            private float[] point = new float[2];

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Matrix matrix = new Matrix();
                imageView.getImageMatrix().invert(matrix);
                point[0] = event.getX();
                point[1] = event.getY();
                matrix.mapPoints(point);

                float x = point[0];
                float y = point[1];

                List<Float> point = new ArrayList<>();
                point.add(x);
                point.add(y);
                point.add(brushWidth);
                pathPointsList.add(point);

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        path.moveTo(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        path.lineTo(x, y);
                        canvas.drawPath(path, paint);
                        break;
                    case MotionEvent.ACTION_UP:
                        List<Float> breakPoint = new ArrayList<>();
                        breakPoint.add(-1f);
                        breakPoint.add(-1f);
                        pathPointsList.add(breakPoint);
                        break;
                }

                imageView.invalidate();
                return true;
            }

        });
    }

    private void closeTouchListener() {
        imageView.setOnTouchListener(null);
    }

    private void onChooseBrush(int size) {
        // -1 thin, 0 medium, 1 thick
        switch (size) {
            case -1:
                brushWidth = 5f;
                textView.setText("已拿起小号画笔");
                break;
            case 0:
                brushWidth = 25f;
                textView.setText("已拿起中号画笔");
                break;
            case 1:
                brushWidth = 60f;
                textView.setText("已拿起大号画笔");
                break;
        }

        paint.setStrokeWidth(brushWidth);

        openTouchListener();

        if (brushAlertDialog != null && brushAlertDialog.isShowing()) {
            brushAlertDialog.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.okButton) {

            Gson gson = new Gson();
            String pathJsonStr = gson.toJson(pathPointsList);

            Intent intent = new Intent(this, AddMaterialActivity.class);
            intent.putExtra("pathJsonStr", pathJsonStr);
            intent.putExtra("frameUriStr", frameUriStr);
            intent.putExtra("croppedVideoUriStr", croppedVideoUriStr);
            startActivity(intent);

        } else if (v.getId() == R.id.retryButton) {

            path.reset();

            pathPointsList = new ArrayList<>();
            List<Float> breakPoint = new ArrayList<>();
            breakPoint.add(-1f);
            breakPoint.add(-1f);
            pathPointsList.add(breakPoint);

            paintedBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(paintedBitmap);
            imageView.setImageBitmap(paintedBitmap);

        } else if (v.getId() == R.id.brushButton) {

            closeTouchListener();

            AlertDialog.Builder brushDialog =
                    new AlertDialog.Builder(BrushingActivity.this);
            final View dialogView = LayoutInflater.from(BrushingActivity.this)
                    .inflate(R.layout.popup_brush, null);

            ImageButton thickButton = dialogView.findViewById(R.id.btnThick);
            ImageButton mediumButton = dialogView.findViewById(R.id.btnMedium);
            ImageButton thinButton = dialogView.findViewById(R.id.btnThin);

            thickButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChooseBrush(1);
                }
            });

            mediumButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChooseBrush(0);
                }
            });

            thinButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onChooseBrush(-1);
                }
            });

            brushDialog.setView(dialogView);

            brushAlertDialog = brushDialog.create();

            // 设置对话框大小和位置
            Window window = brushAlertDialog.getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.gravity = Gravity.BOTTOM;
            layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);

            brushAlertDialog.show();

        }
    }
}