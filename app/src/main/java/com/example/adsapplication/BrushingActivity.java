package com.example.adsapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class BrushingActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "ww";

    private ImageView imageView;
    private ImageButton drawButton;
    private Button okButton;
    private Button retryButton;
    private TextView textView;

    String frameUriStr;
    String croppedVideoUriStr;

    private Bitmap baseBitmap;
    private Canvas canvas;
    private Path path;
    private Paint paint;


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
            Bitmap frameBitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            // 复制一份用于绘制
            baseBitmap = frameBitmap.copy(Bitmap.Config.ARGB_8888, true);
            canvas = new Canvas(baseBitmap);
            imageView.setImageBitmap(baseBitmap);

        } catch (Exception e) {
            Log.e(TAG, "iamgeView: " + e.getMessage());
        }

        textView = findViewById(R.id.textView);

        drawButton = findViewById(R.id.drawButton);
        drawButton.setOnClickListener(this);

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
            String filePath = Environment.getExternalStorageDirectory() + File.separator + "drawing_data.png";
            try {
                FileOutputStream fos = new FileOutputStream(filePath);
                baseBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                Toast.makeText(BrushingActivity.this, "Drawing saved to " + filePath, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(BrushingActivity.this, "Failed to save drawing", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.retryButton) {
            path.reset();
            canvas = new Canvas(baseBitmap);
            imageView.setImageBitmap(baseBitmap);
            imageView.invalidate();
        } else if (v.getId() == R.id.drawButton) {
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
        }
    }
}