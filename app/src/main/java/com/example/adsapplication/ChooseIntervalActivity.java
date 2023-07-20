package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.adsapplication.model.RangeSeekBar;

public class ChooseIntervalActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "ww";

    private TextView textView;
    private VideoView videoView;
    private Button okButton;
    private SeekBar seekBar;
    private RangeSeekBar rangeSeekBar;

    private int videoDuration; // 视频总时长
    private int startValue; // 区间选择的开始点
    private int endValue; // 区间选择的结束点

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_interval);

        textView = findViewById(R.id.textView);

        okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(this);

        seekBar = findViewById(R.id.seekBar);

        rangeSeekBar = findViewById(R.id.rangeSeekBar);

        videoView = findViewById(R.id.videoView);
        String videoUriStr = getIntent().getStringExtra("videoUriStr");
        videoView.setVideoURI(Uri.parse(videoUriStr));

        // 监听视频准备完成事件
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 获取视频的总时长
                videoDuration = videoView.getDuration();
                // 设置SeekBar的最大值为视频的总时长
                seekBar.setMax(videoDuration);

                // 启动视频播放
                videoView.start();

                // 更新SeekBar的进度
                updateSeekBarProgress();

                // 监听SeekBar的滑动事件
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        // 将视频播放位置设置为SeekBar的进度
                        if (fromUser) {
                            videoView.seekTo(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        // 暂停视频播放
                        videoView.pause();
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        // 继续视频播放
                        videoView.start();
                    }
                });

                // 设置RangeSeekBar的最大值为视频的总时长
                rangeSeekBar.setMax(videoDuration);
            }
        });

        // 监听区间选择事件
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, int minValue, int maxValue) {
                /// 更新选择的开始点和结束点
                startValue = minValue;
                endValue = maxValue;
            }
        });
    }

    // 更新SeekBar的进度
    private void updateSeekBarProgress() {
        seekBar.setProgress(videoView.getCurrentPosition());
        if (videoView.isPlaying()) {
            Runnable progressUpdate = new Runnable() {
                @Override
                public void run() {
                    updateSeekBarProgress();
                }
            };
            seekBar.postDelayed(progressUpdate, 100);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.okButton) {
            Intent intent = new Intent(this, BrushingActivity.class);
            intent.putExtra("startValue", startValue);
            intent.putExtra("endValue", endValue);
            startActivity(intent);
        }
    }

}