package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import java.io.IOException;

public class BrushingActivity extends AppCompatActivity implements View.OnClickListener {

    private String TAG = "ww";

    private ImageView imageView;
    private ImageButton drawButton;
    private Button okButton;
    private Button retryButton;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brushing);

        imageView = findViewById(R.id.imageView);
        Uri frameUri = Uri.parse(getIntent().getStringExtra("frameUriStr"));
        imageView.setImageURI(frameUri);

        textView = findViewById(R.id.textView);

        drawButton = findViewById(R.id.drawButton);
        drawButton.setOnClickListener(this);

        okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(this);

        retryButton = findViewById(R.id.retryButton);
        retryButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.okButton) {

        } else if (v.getId() == R.id.retryButton) {

        } else if (v.getId() == R.id.drawButton) {

        }
    }
}