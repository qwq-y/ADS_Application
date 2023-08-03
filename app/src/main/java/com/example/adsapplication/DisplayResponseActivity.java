package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.adsapplication.utils.models.CustomResponse;

public class DisplayResponseActivity extends AppCompatActivity {

    private final String TAG = "ww";

    private CustomResponse response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_response);

        response = (CustomResponse) getIntent().getSerializableExtra("response");

    }
}