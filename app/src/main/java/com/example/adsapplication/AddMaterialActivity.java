package com.example.adsapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.File;

public class AddMaterialActivity extends AppCompatActivity implements View.OnClickListener {

    private final String TAG = "ww";

    private static final int REQUEST_IMAGE_PICK = 1;

    private String croppedVideoUriStr;    // 裁剪后的视频 uri
    private String frameUriStr;    // 视频第一帧 uri
    private String pathJsonStr;    // 绘制的路径

    private String imageSourcePath;    // 添加的图片素材路径
    private String textSource;    // 添加的文本素材

    private ImageView imageView;
    private EditText editText;
    private Button okButton;
    private Button retryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_material);

        croppedVideoUriStr = getIntent().getStringExtra("croppedVideoUriStr");
        frameUriStr = getIntent().getStringExtra("frameUriStr");
        pathJsonStr = getIntent().getStringExtra("pathJsonStr");

        imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener(this);

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
            intent.putExtra("imageSourcePath", imageSourcePath);
            intent.putExtra("textSource", textSource);
            startActivity(intent);

        } else if (v.getId() == R.id.retryButton) {

            imageView.setImageResource(R.drawable.ic_add);
            imageSourcePath = null;
            editText.setText("");
            textSource = null;

        } else if (v.getId() == R.id.imageView) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_IMAGE_PICK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            // 获取选择的图片URI
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // 将URI转换为图片路径
                imageSourcePath = getPathFromUri(selectedImageUri);

                // 将图片显示在ImageView上
                Bitmap bitmap = BitmapFactory.decodeFile(imageSourcePath);
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private String getPathFromUri(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        return null;
    }
}