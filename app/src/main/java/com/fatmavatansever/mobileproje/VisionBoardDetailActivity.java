package com.fatmavatansever.mobileproje;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fatmavatansever.mobileproje.databinding.ActivityVisionBoardDetailBinding;

import java.io.File;
import java.io.FileOutputStream;
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
        File visionBoardFile = new File(visionBoardPath);


        if (visionBoardPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(visionBoardPath);
            binding.visionboardDetailImage.setImageBitmap(bitmap);

            // İndirme butonu tıklama olayı
            binding.downloadButton.setOnClickListener(v -> saveImageToGallery(bitmap));
            // Buton referansı
            ImageButton wallpaperButton = findViewById(R.id.set_wallpaper_button);
            wallpaperButton.setOnClickListener(v -> setVisionBoardAsWallpaper(bitmap));
        }


        binding.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Are you sure you want to delete this?")
                    .setMessage("You are about to delete this VisionBoard. This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        boolean deleted = visionBoardFile.delete();
                        if (deleted) {
                            Intent intent = new Intent();
                            intent.putExtra("deletedPath", visionBoardPath);
                            setResult(RESULT_OK, intent);
                            finish(); // Sayfayı kapat
                        } else {
                            Toast.makeText(this, "Failed to delete VisionBoard!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });





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

    private Uri saveBitmapToCacheAndGetUri(Context context, Bitmap bitmap) {
        try {
            // Geçici bir dosya oluştur
            File cacheDir = new File(context.getFilesDir(), "shared");
            if (!cacheDir.exists()) cacheDir.mkdirs();

            File file = new File(cacheDir, "visionboard_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            // FileProvider üzerinden paylaşılabilir URI oluştur
            return androidx.core.content.FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void setVisionBoardAsWallpaper(Bitmap bitmap) {
        try {
            // Görseli geçici bir dosyaya kaydet
            Uri uri = saveBitmapToCacheAndGetUri(this, bitmap);

            // Geçici URI oluşturulduysa devam et
            if (uri != null) {
                // Duvar kağıdı ayarlamak için intent oluştur
                Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                intent.setDataAndType(uri, "image/*");
                intent.putExtra("mimeType", "image/*");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Kullanıcıya seçim ekranı göster
                startActivity(Intent.createChooser(intent, "Set as Wallpaper"));
            } else {
                Toast.makeText(this, "Error creating wallpaper file!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to set wallpaper!", Toast.LENGTH_SHORT).show();
        }
    }




}
