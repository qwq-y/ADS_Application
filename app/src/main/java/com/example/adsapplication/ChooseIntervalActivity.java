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
    private PopupWindow popupWindow;
    private ImageView frameImageView;

    private String videoUriStr;    // 原视频资源
    private int totalDuration;    // 原视频的总时长
    private int startMillis, endMillis, selectedDuration;    // 区间选择的开始点、结束点、总时长

    private HashMap<Integer, Bitmap> frameCache = new HashMap<>();    // 缓存视频帧
    private int frameInterval = 1000; // 获取帧的间隔（ms）

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
                // 视频预缓冲
                preCacheFrames(0, totalDuration, frameInterval);
            }
        });

        // 初始化 PopupWindow
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

                // 将视频跳转到选定的区间的起始处
                videoView.seekTo(startMillis);

                // 缓存中查找帧
                Bitmap cachedFrame = frameCache.get(progressStart);
                if (cachedFrame != null) {
                    // 缓存中有帧，直接设置到ImageView中
                    frameImageView.setImageBitmap(cachedFrame);
                } else {
//                    // 缓存中没有帧，执行异步任务获取帧
//                    GetVideoFrameTask task = new GetVideoFrameTask();
//                    task.execute(progressStart);
                }
            }

            @Override
            public void onStartTrackingTouch(final RangeSeekBar seekBar) {
                // 开始触摸滑块时显示 PopupWindow
                popupWindow.showAtLocation(videoView, Gravity.CENTER, 0, -videoView.getHeight() / 3);
            }

            @Override
            public void onStopTrackingTouch(final RangeSeekBar seekBar) {
                // 停止触摸滑块时隐藏 PopupWindow
                popupWindow.dismiss();
            }
        });
    }

    // 预缓冲视频帧
    private void preCacheFrames(int startMs, int endMs, int interval) {
        Log.d(TAG, "caching");
        for (int timeMs = startMs; timeMs <= endMs; timeMs += interval) {
            if (!frameCache.containsKey(timeMs)) {
                Bitmap frame = getVideoFrame(timeMs);
                if (frame != null) {
                    frameCache.put(timeMs, frame);
                }
            }
        }
        Log.d(TAG, "cached: " + frameCache.size());
    }

    // 获取视频指定位置的帧
    private Bitmap getVideoFrame(int timeMs) {
        Bitmap frame = frameCache.get(timeMs);
        if (frame == null) {
            MediaMetadataRetriever retriever = null;
            try {
                retriever = new MediaMetadataRetriever();
                retriever.setDataSource(ChooseIntervalActivity.this, Uri.parse(videoUriStr));

                // 获取视频帧，单位：微秒
                frame = retriever.getFrameAtTime(timeMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST);

                // 将帧添加到缓存中
                frameCache.put(timeMs, frame);

            } catch (Exception e) {
                Log.d(TAG, "get video frame: " + e.getMessage());
            } finally {
                if (retriever != null) {
                    retriever = null;
                }
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
