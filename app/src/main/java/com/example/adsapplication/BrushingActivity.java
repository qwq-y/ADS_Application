package com.example.adsapplication;

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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

    String frameUriStr;
    String croppedVideoUriStr;

    private Bitmap baseBitmap;
    private Bitmap paintedBitmap;
    private Canvas canvas;
    private Path path;
    private Paint paint;
    private boolean isBrushOn = true;

    private List<List<Float>> pathPointsList = new ArrayList<>();
    private String pathJsonStr;


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

            // 复制一份没有用于绘制
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

//        drawingView = findViewById(R.id.drawingView);

        path = new Path();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(20);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.okButton) {

            JSONArray jsonArray = new JSONArray();
            for (List<Float> point : pathPointsList) {
                JSONArray pointArray = new JSONArray();
                pointArray.put(point.get(0));
                pointArray.put(point.get(1));
                jsonArray.put(pointArray);
            }

            pathJsonStr = jsonArray.toString();

            Intent intent = new Intent(this, AddMaterialActivity.class);
            intent.putExtra("pathJsonStr", pathJsonStr);
            intent.putExtra("frameUriStr", frameUriStr);
            intent.putExtra("croppedVideoUriStr", croppedVideoUriStr);
            startActivity(intent);

        } else if (v.getId() == R.id.retryButton) {

            path.reset();
            paintedBitmap = baseBitmap.copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(paintedBitmap);
            imageView.setImageBitmap(paintedBitmap);

        } else if (v.getId() == R.id.brushButton) {

            isBrushOn = !isBrushOn;

            if (isBrushOn) {
                // 开启绘制功能
                textView.setText("请绘制生成区域（已拿起画笔）");
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
                                break;
                        }

                        imageView.invalidate();
                        return true;
                    }

                });
            } else {
                // 禁用绘制功能
                imageView.setOnTouchListener(null);
                textView.setText("请绘制生成区域（已放下画笔）");
            }
        }
    }
}