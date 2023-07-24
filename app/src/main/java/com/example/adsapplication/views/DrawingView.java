package com.example.adsapplication.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class DrawingView extends View {

    private final String TAG = "ww";
    private Bitmap bitmap;
    private Canvas canvas;
    private Paint brushPaint;
    private Path brushPath;
    private List<Path> pathsList = new ArrayList<>();

    public DrawingView(Context context) {
        super(context);
        initCanvas();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCanvas();
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCanvas();
    }

    private void initCanvas() {
        try {
            brushPaint = new Paint();
            brushPaint.setColor(Color.BLACK);
            brushPaint.setStrokeWidth(10);
            brushPaint.setStyle(Paint.Style.STROKE);

            brushPath = new Path();
        } catch (Exception e) {
            Log.e(TAG, "initCanvas: " + e.getMessage());
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);

        // 绘制已保存的笔迹
        for (Path path : pathsList) {
            canvas.drawPath(path, brushPaint);
        }

        // 绘制正在绘制的笔迹
        canvas.drawPath(brushPath, brushPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                brushPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                brushPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                canvas.drawPath(brushPath, brushPaint);
                pathsList.add(new Path(brushPath));
                brushPath.reset();
                break;
            default:
                return false;
        }

        invalidate(); // 强制重绘
        return true;
    }
}
