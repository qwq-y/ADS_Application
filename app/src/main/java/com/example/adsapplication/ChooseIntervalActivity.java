package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.HashMap;

import it.sephiroth.android.library.rangeseekbar.RangeSeekBar;

public class ChooseIntervalActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "ww";

    private TextView textView;
    private VideoView videoView;
    private Button okButton;
    private RangeSeekBar rangeSeekBar;

    private String videoUriStr;    // 原视频资源
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

        // 添加媒体播放器
        MediaController mediaController = new MediaController(this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);

        // 从上个页面获取视频
        videoUriStr = getIntent().getStringExtra("videoUriStr");
        videoView.setVideoURI(Uri.parse(videoUriStr));

        // 设置视频准备就绪监听器
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                // 获取视频总时长
                totalDuration = mediaPlayer.getDuration();
                // 设置滑块最大值
                rangeSeekBar.setMax(totalDuration);
                // 播放视频
                videoView.start();
            }
        });

        rangeSeekBar = findViewById(R.id.rangeSeekBar);

        // 设置滑块监听事件
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {

            @Override
            public void onProgressChanged(
                    final RangeSeekBar seekBar, final int progressStart, final int progressEnd, final boolean fromUser) {

                int startMillisNew = (int) ((float) progressStart / seekBar.getMax() * totalDuration);
                int endMillisNew = (int) ((float) progressEnd / seekBar.getMax() * totalDuration);

                // 手动判断哪个滑块移动了
                int startDiff = startMillisNew > startMillis ? startMillisNew - startMillis : startMillis - startMillisNew;
                int endDiff = endMillisNew > endMillis ? endMillisNew - endMillis : endMillis - endMillisNew;
                if (startDiff >= endDiff) {
                    // 从左边滑块处播放视频
                    videoView.seekTo(startMillis);
                } else {
                    // 从右边滑块播放视频
                    videoView.seekTo(endMillis);
                }

                // 更新视频起始时间、结束时间、总时长
                startMillis = startMillisNew;
                endMillis = endMillisNew;
                selectedDuration = endMillis - startMillis;

            }

            @Override
            public void onStartTrackingTouch(final RangeSeekBar seekBar) {
                // 开始触摸滑块时
            }

            @Override
            public void onStopTrackingTouch(final RangeSeekBar seekBar) {
                // 停止触摸滑块时
                videoView.seekTo(startMillis);
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
