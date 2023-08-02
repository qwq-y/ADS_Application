package com.example.adsapplication;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.adsapplication.utils.ImageAdapter;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class AddMaterialActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "ww";

    private String croppedVideoUriStr;    // 裁剪后的视频 uri
    private String frameUriStr;    // 视频第一帧 uri
    private String pathJsonStr;    // 绘制的路径

    private String textSource;    // 添加的文本素材
    private List<String> imageSourceUriStrs = new ArrayList<>();    // 添加的图片素材 uri

    private EditText editText;
    private Button okButton;
    private Button retryButton;
    private Button addImageButton;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;


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

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3)); // 3列网格布局
        imageAdapter = new ImageAdapter(imageSourceUriStrs);
        recyclerView.setAdapter(imageAdapter);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.okButton) {

            textSource = editText.getText().toString();

            Intent intent = new Intent(this, SendingActivity.class);
            Gson gson = new Gson();
            String imageSourceUriJsonStr = gson.toJson(imageSourceUriStrs);
            intent.putExtra("imageSourceUriJsonStr", imageSourceUriJsonStr);
            intent.putExtra("croppedVideoUriStr", croppedVideoUriStr);
            intent.putExtra("frameUriStr", frameUriStr);
            intent.putExtra("pathJsonStr", pathJsonStr);
            intent.putExtra("textSource", textSource);
            startActivity(intent);

        } else if (v.getId() == R.id.retryButton) {

            imageSourceUriStrs.clear();
            imageAdapter.setImageUrls(imageSourceUriStrs);

            editText.setText("");
            textSource = null;

        } else if (v.getId() == R.id.addImageButton) {

            Intent intent = new Intent();
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"Select Picture"), 1);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "onActivityResult: " + requestCode);
            return;
        }

        if (requestCode == 1) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                Log.d(TAG, "多张图片：" + clipData.getItemCount());
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    Uri currentUri = clipData.getItemAt(i).getUri();
                    Log.d(TAG, currentUri.toString());
                    imageSourceUriStrs.add(currentUri.toString());
                }
            } else {
                Uri currentUri = data.getData();
                Log.d(TAG, "单张图片: \n" + currentUri);
                imageSourceUriStrs.add(currentUri.toString());
            }

            imageAdapter.setImageUrls(imageSourceUriStrs);
            Log.d(TAG, "共 " + imageSourceUriStrs.size() +  " 张图片");


        }
    }

}