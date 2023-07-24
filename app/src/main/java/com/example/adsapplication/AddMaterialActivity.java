package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class AddMaterialActivity extends AppCompatActivity implements View.OnClickListener {

    private String croppedVideoUriStr;
    private String frameUriStr;
    private String pathJsonStr;

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

        } else if (v.getId() == R.id.retryButton) {

        } else if (v.getId() == R.id.imageView) {

        }
    }
}