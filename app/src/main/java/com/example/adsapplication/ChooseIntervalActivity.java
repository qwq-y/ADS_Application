package com.example.adsapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class ChooseIntervalActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_interval);

        textView = findViewById(R.id.textView);

        // 从视频选择页面获取数据
        Bundle bundle = getIntent().getExtras();
        String message = bundle.getString("message");
        textView.setText(message);
    }
}