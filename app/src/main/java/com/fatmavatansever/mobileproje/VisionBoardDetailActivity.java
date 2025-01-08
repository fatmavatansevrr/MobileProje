package com.fatmavatansever.mobileproje;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fatmavatansever.mobileproje.databinding.ActivityVisionBoardDetailBinding;

import java.io.OutputStream;

public class VisionBoardDetailActivity extends AppCompatActivity {

    private ActivityVisionBoardDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ViewBinding ile layout'u bağla
        binding = ActivityVisionBoardDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Intent'ten görsel yolunu al
        String visionBoardPath = getIntent().getStringExtra("visionBoardPath");

        if (visionBoardPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(visionBoardPath);
            binding.visionboardDetailImage.setImageBitmap(bitmap);

            // İndirme butonu tıklama olayı
            binding.downloadButton.setOnClickListener(v -> saveImageToGallery(bitmap));
        }


        // Bottom Navigation tıklama olaylarını ayarla
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                // MainActivity'e git
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                // HistoryActivity'e git
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }
    private void saveImageToGallery(Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "VisionBoard");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Downloaded from app");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VisionBoards");
        }

        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Toast.makeText(this, "Image saved to gallery!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
