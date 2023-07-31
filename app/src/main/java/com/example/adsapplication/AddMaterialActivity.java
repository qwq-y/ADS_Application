package com.example.adsapplication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;
import com.zhihu.matisse.engine.impl.GlideEngine;
import com.zhihu.matisse.filter.Filter;

import java.util.ArrayList;
import java.util.List;

public class AddMaterialActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "ww";

    private int REQUEST_CODE_CHOOSE = 23;

    private String croppedVideoUriStr;    // 裁剪后的视频 uri
    private String frameUriStr;    // 视频第一帧 uri
    private String pathJsonStr;    // 绘制的路径

    private String textSource;    // 添加的文本素材
//    private List<String> imageSourceUriStrs = new ArrayList<>();    // 添加的图片素材 uri
    List<Uri> mSelected;

    private EditText editText;
    private Button okButton;
    private Button retryButton;
    private ImageButton addImageButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_material);

        croppedVideoUriStr = getIntent().getStringExtra("croppedVideoUriStr");
        frameUriStr = getIntent().getStringExtra("frameUriStr");
        pathJsonStr = getIntent().getStringExtra("pathJsonStr");

        addImageButton = findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(this);

        editText = findViewById(R.id.editText);

        okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(this);

        retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.okButton) {

            textSource = editText.getText().toString();

            Intent intent = new Intent(this, SendingActivity.class);
            intent.putExtra("croppedVideoUriStr", croppedVideoUriStr);
            intent.putExtra("frameUriStr", frameUriStr);
            intent.putExtra("pathJsonStr", pathJsonStr);
            // TODO: toString
//            intent.putExtra("selectedImageUris", imageSourceUriStrs.toString());
            intent.putExtra("textSource", textSource);
            startActivity(intent);

        } else if (v.getId() == R.id.retryButton) {

            // TODO: 清空图片
            editText.setText("");
            textSource = null;

        } else if (v.getId() == R.id.addImageButton) {

            Matisse.from(this)
                    .choose(MimeType.ofImage()) // 仅选择图片类型
                    .countable(true) // 显示选择图片的数量
                    .maxSelectable(9) // 最多可选择的图片数量
                    .capture(false) // 是否显示拍照
                    .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) // 限制选择图片的方向
                    .thumbnailScale(0.85f) // 缩略图缩放比例
                    .imageEngine(new GlideEngine()) // 图片加载引擎
                    .forResult(REQUEST_CODE_CHOOSE); // 设置请求码，用于在onActivityResult中接收结果

            Log.d(TAG, "over");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "call back");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mSelected = Matisse.obtainResult(data);
            Log.d("Matisse", "mSelected: " + mSelected);
        }
    }

}