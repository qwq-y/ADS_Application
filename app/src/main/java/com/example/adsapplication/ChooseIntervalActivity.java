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

import it.sephiroth.android.library.rangeseekbar.RangeSeekBar;

public class ChooseIntervalActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "ww";

    private TextView textView;
    private VideoView videoView;
    private Button okButton;
    private RangeSeekBar rangeSeekBar;
    private PopupWindow popupWindow;
    private ImageView frameImageView;

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

        // 设置视频准备就绪监听器：获取视频总时长，设置滑块最大值，播放视频
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                totalDuration = mediaPlayer.getDuration();
                rangeSeekBar.setMax(totalDuration);

                videoView.start();
            }
        });

        // 初始化PopupWindow
        View popupView = getLayoutInflater().inflate(R.layout.popup_frame_preview, null);
        frameImageView = popupView.findViewById(R.id.frameImageView);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popupWindow.setTouchable(false);

        rangeSeekBar = findViewById(R.id.rangeSeekBar);

        // 设置滑块监听事件
        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
            @Override
            public void onProgressChanged(
                    final RangeSeekBar seekBar, final int progressStart, final int progressEnd, final boolean fromUser) {

                // 根据滑块的位置信息计算视频起始时间、结束时间、总时长
                startMillis = (int) ((float) progressStart / seekBar.getMax() * totalDuration);
                endMillis = (int) ((float) progressEnd / seekBar.getMax() * totalDuration);
                selectedDuration = endMillis - startMillis;

                // 异步获取视频帧并显示在PopupWindow中
                GetVideoFrameTask task = new GetVideoFrameTask();
                task.execute(progressStart);

                // 将视频跳转到选定的区间的起始处
                videoView.seekTo(startMillis);

            }

            @Override
            public void onStartTrackingTouch(final RangeSeekBar seekBar) {
                // 开始触摸滑块时显示PopupWindow
                popupWindow.showAtLocation(videoView, Gravity.CENTER, 0, -videoView.getHeight() / 3);
            }

            @Override
            public void onStopTrackingTouch(final RangeSeekBar seekBar) {
                // 停止触摸滑块时隐藏PopupWindow
                popupWindow.dismiss();
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

    // 获取视频指定位置的帧
    private Bitmap getVideoFrame(int timeMs) {
        MediaMetadataRetriever retriever = null;
        Bitmap frame = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(videoUriStr);

            // 获取视频帧，单位：微秒
            frame = retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);

        } catch (Exception e) {
            Log.d(TAG, "get video frame: " + e.getMessage());
        } finally {
            if (retriever != null) {
                retriever = null;
            }
        }

        return frame;
    }

    // 异步获取视频帧
    private class GetVideoFrameTask extends AsyncTask<Integer, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(Integer... params) {
            int timeMs = params[0];
            return getVideoFrame(timeMs);
        }

        @Override
        protected void onPostExecute(Bitmap frame) {
            frameImageView.setImageBitmap(frame);
        }
    }

}