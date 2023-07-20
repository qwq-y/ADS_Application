package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import it.sephiroth.android.library.rangeseekbar.RangeSeekBar;

public class ChooseIntervalActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "ww";

    private TextView textView;
    private VideoView videoView;
    private Button okButton;
    private RangeSeekBar rangeSeekBar;

    private int totalDuration;    // 原视频的总时长
    private int startMillis, endMillis, selectedDuration;    // 区间选择的开始点、结束点、总时长

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_interval);

        textView = findViewById(R.id.textView);

        okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(this);

        videoView = findViewById(R.id.videoView);

        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        String videoUriStr = getIntent().getStringExtra("videoUriStr");
        Log.d(TAG, "videoUriStr" + videoUriStr);
        videoView.setVideoURI(Uri.parse(videoUriStr));

        // 设置视频准备就绪监听器：获取视频总时长，设置滑块最大值，播放视频
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                totalDuration = mediaPlayer.getDuration();
                rangeSeekBar.setMax(totalDuration);

                videoView.start();
            }
        });

        rangeSeekBar = findViewById(R.id.rangeSeekBar);
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    final RangeSeekBar seekBar, final int progressStart, final int progressEnd, final boolean fromUser) {

                // 根据滑块的位置信息计算视频起始时间、结束时间、总时长
                startMillis = (int) ((float) progressStart / seekBar.getMax() * totalDuration);
                endMillis = (int) ((float) progressEnd / seekBar.getMax() * totalDuration);
                selectedDuration = endMillis - startMillis;

                // 将视频跳转到选定的区间的起始处
                videoView.seekTo(startMillis);

            }

            @Override
            public void onStartTrackingTouch(final RangeSeekBar seekBar) {
                // 开始触摸滑块时调用
            }

            @Override
            public void onStopTrackingTouch(final RangeSeekBar seekBar) {
                // 停止触摸滑块时调用
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.okButton) {
            textView.setText("[" + startMillis + ", " + endMillis + "], 总时长: " + selectedDuration + " ms");
//            Intent intent = new Intent(this, BrushingActivity.class);
//            intent.putExtra("startMillis", startMillis);
//            intent.putExtra("endMillis", endMillis);
//            startActivity(intent);
        }
    }

}