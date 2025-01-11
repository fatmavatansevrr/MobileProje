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

        // Görsel yolu geçersizse hata mesajı göster ve aktiviteyi bitir
        if (visionBoardPath == null || visionBoardPath.isEmpty()) {
            Toast.makeText(this, "Geçersiz görsel yolu!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Görsel dosyası bulunamazsa hata mesajı göster
        if (!visionBoardFile.exists()) {
            Toast.makeText(this, "Görsel dosyası bulunamadı!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Görseli yükle ve ImageView'de göster
        Bitmap bitmap = BitmapFactory.decodeFile(visionBoardPath);
        if (bitmap != null) {
            binding.visionboardDetailImage.setImageBitmap(bitmap);

            // İndirme butonuna tıklama olayı
            binding.downloadButton.setOnClickListener(v -> saveImageToGallery(bitmap));
            // Duvar kağıdı butonuna tıklama olayı
            ImageButton wallpaperButton = findViewById(R.id.set_wallpaper_button);
            wallpaperButton.setOnClickListener(v -> setVisionBoardAsWallpaper(bitmap));
        } else {
            // Görsel yüklenemezse hata mesajı göster
            Toast.makeText(this, "Görsel yüklenemedi!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Silme butonuna tıklama olayı
        binding.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Emin misiniz? Silmek istiyor musunuz?")
                    .setMessage("Bu VisionBoard'u silmek üzeresiniz. Bu işlem geri alınamaz.")
                    .setPositiveButton("Sil", (dialog, which) -> {
                        boolean deleted = visionBoardFile.delete();
                        if (deleted) {
                            // Silme işlemi başarılıysa, sonrasında gelen Intent ile yolu geri gönder
                            Intent intent = new Intent();
                            intent.putExtra("deletedPath", visionBoardPath);
                            setResult(RESULT_OK, intent);
                            finish(); // Sayfayı kapat
                        } else {
                            // Silme başarısızsa hata mesajı göster
                            Toast.makeText(this, "VisionBoard silinemedi!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("İptal", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // Bottom Navigation tıklama olaylarını ayarla
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                // Ana sayfaya git
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                // Geçmiş sayfasına git
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    // Görseli galeriyeye kaydetme fonksiyonu
    private void saveImageToGallery(Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "VisionBoard");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Uygulamadan indirildi");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VisionBoards");
        }

        // Galeriye kaydetmek için URI oluştur
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (uri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                // Görseli PNG formatında kaydet
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Toast.makeText(this, "Görsel galeriye kaydedildi!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                // Hata oluşursa mesaj göster
                Toast.makeText(this, "Görsel kaydedilirken hata oluştu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Bitmap'i geçici dosyaya kaydedip URI döndürme fonksiyonu
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

    // Görseli duvar kağıdı olarak ayarlama fonksiyonu
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
                startActivity(Intent.createChooser(intent, "Duvar Kağıdı Olarak Ayarla"));
            } else {
                // URI oluşturulamazsa hata mesajı göster
                Toast.makeText(this, "Duvar kağıdı dosyası oluşturulurken hata oluştu!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Duvar kağıdı ayarlanırken hata oluştu!", Toast.LENGTH_SHORT).show();
        }
    }
}
