package com.example.adsapplication.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;

public class RangeSeekBar extends AppCompatSeekBar {

    private OnRangeSeekBarChangeListener onRangeSeekBarChangeListener;

    private int leftThumbValue = 0;
    private int rightThumbValue = 100;
    private int thumbWidth;

    public RangeSeekBar(Context context) {
        super(context);
        init();
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RangeSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        thumbWidth = getThumb().getIntrinsicWidth();
    }

    public void setLeftThumbValue(int value) {
        if (value >= 0 && value <= getMax()) {
            leftThumbValue = value;
            invalidate();
        }
    }

    public int getLeftThumbValue() {
        return leftThumbValue;
    }

    public void setRightThumbValue(int value) {
        if (value >= 0 && value <= getMax()) {
            rightThumbValue = value;
            invalidate();
        }
    }

    public int getRightThumbValue() {
        return rightThumbValue;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 绘制左侧拇指
        float leftThumbX = (float) leftThumbValue / getMax() * (getWidth() - thumbWidth);
        getThumb().setBounds((int) leftThumbX, 0, (int) leftThumbX + thumbWidth, getHeight());
        getThumb().draw(canvas);

        // 绘制右侧拇指
        float rightThumbX = (float) rightThumbValue / getMax() * (getWidth() - thumbWidth);
        getThumb().setBounds((int) rightThumbX, 0, (int) rightThumbX + thumbWidth, getHeight());
        getThumb().draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 确定点击的拇指
                if (isLeftThumbTouched(event.getX())) {
                    setPressed(true);
                    setSelected(true);
                } else if (isRightThumbTouched(event.getX())) {
                    setPressed(true);
                    setSelected(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isPressed()) {
                    // 移动拇指
                    if (isLeftThumbSelected()) {
                        setLeftThumbValue((int) (getMax() * event.getX() / getWidth()));
                    } else if (isRightThumbSelected()) {
                        setRightThumbValue((int) (getMax() * event.getX() / getWidth()));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                setSelected(false);
                break;
        }

        return true;
    }

    public void setOnRangeSeekBarChangeListener(OnRangeSeekBarChangeListener listener) {
        this.onRangeSeekBarChangeListener = listener;
    }

    private boolean isLeftThumbSelected() {
        return leftThumbValue < rightThumbValue;
    }

    private boolean isRightThumbSelected() {
        return rightThumbValue > leftThumbValue;
    }

    private boolean isLeftThumbTouched(float touchX) {
        float leftThumbX = (float) leftThumbValue / getMax() * (getWidth() - thumbWidth);
        return touchX >= leftThumbX && touchX <= leftThumbX + thumbWidth;
    }

    private boolean isRightThumbTouched(float touchX) {
        float rightThumbX = (float) rightThumbValue / getMax() * (getWidth() - thumbWidth);
        return touchX >= rightThumbX && touchX <= rightThumbX + thumbWidth;
    }

    public interface OnRangeSeekBarChangeListener {
        void onRangeSeekBarValuesChanged(RangeSeekBar bar, int minValue, int maxValue);
    }
}

