package com.fatmavatansever.mobileproje;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class VisionBoardDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_board_detail);

        ImageView visionBoardImageView = findViewById(R.id.visionboard_detail_image);

        // Intent'ten görsel yolunu al
        String visionBoardPath = getIntent().getStringExtra("visionBoardPath");

        if (visionBoardPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(visionBoardPath);
            visionBoardImageView.setImageBitmap(bitmap);
        }
    }
}
