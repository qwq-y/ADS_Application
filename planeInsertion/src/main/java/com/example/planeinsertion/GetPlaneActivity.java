package com.example.planeinsertion;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.planeinsertion.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class GetPlaneActivity extends AppCompatActivity {

    private String TAG = "ww";

    private ImageView imageView;
    private Button okButton;
    private TextView textView;

    private String frameUriStr;
    private String croppedVideoUriStr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_plane);

        croppedVideoUriStr = getIntent().getStringExtra("croppedVideoUriStr");
        frameUriStr = getIntent().getStringExtra("frameUriStr");

        imageView = findViewById(R.id.imageView);
        imageView.setImageURI(Uri.parse(frameUriStr));

        textView = findViewById(R.id.textView);

        okButton = findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(this, AddMaterialActivity.class);
//                intent.putExtra("frameUriStr", frameUriStr);
//                intent.putExtra("croppedVideoUriStr", croppedVideoUriStr);
//                startActivity(intent);
            }
        });

    }


}