package com.fatmavatansever.mobileproje;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fatmavatansever.mobileproje.databinding.ActivityCollageBinding;
import com.fatmavatansever.mobileproje.models.SwipeCard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class CollageActivity extends AppCompatActivity {

    // ViewBinding ile layout'u bağla
    private ActivityCollageBinding binding;
    // Seçilen kartların bitmaplerini tutacak liste
    private List<Bitmap> imageBitmaps = new ArrayList<>();
    // Kolajın bitmap halini tutacak değişken
    private Bitmap collageBitmap;
    // SwipeCard nesnelerini tutacak liste
    private List<SwipeCard> selectedCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout dosyasını bağla
        binding = ActivityCollageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            // Durum geri yükleme (kolaj ve seçilen kartlar)
            selectedCards = (List<SwipeCard>) savedInstanceState.getSerializable("selectedCards");
            collageBitmap = savedInstanceState.getParcelable("collageBitmap");

            // Kolaj görüntüsünü geri yükle
            if (collageBitmap != null) {
                binding.collageImageView.setImageBitmap(collageBitmap);
            }
        } else {
            // Seçilen kartları SwipeActivity'den al
            selectedCards = (List<SwipeCard>) getIntent().getSerializableExtra("selectedCards");

            // Kartlardaki resimleri Glide ile yükle
            for (SwipeCard card : selectedCards) {
                Glide.with(this)
                        .asBitmap()
                        .load(card.getImageUrl())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                // Yüklenen resmi bitmap listesine ekle
                                imageBitmaps.add(resource);
                                // Tüm resimler yüklendiyse kolajı oluştur
                                if (imageBitmaps.size() == selectedCards.size()) {
                                    createCollage();
                                }
                            }

                            @Override
                            public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {
                                // Resim yüklenirken yapılan işlemler
                            }
                        });
            }
        }

        // Save to Library butonuna tıklama olayını ayarla
        binding.saveToLibraryButton.setOnClickListener(v -> {
            if (collageBitmap != null) {
                // Kolaj hazırsa kaydet
                saveImageToGallery(collageBitmap);
            } else {
                // Kolaj hazır değilse kullanıcıya mesaj göster
                Toast.makeText(this, "Collage is not ready yet.", Toast.LENGTH_SHORT).show();
            }
        });

        // BottomNavigationView için dinleyici ekle
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                // Ana ekrana git
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                // Geçmiş aktivitesine git
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Kolajın geçici yolu ve seçilen kartları kaydet
        File createdImagesFolder = new File(getFilesDir(), "createdImages");
        File collageFile = new File(createdImagesFolder, "collage_temp.png");
        if (collageFile.exists()) {
            outState.putString("collagePath", collageFile.getAbsolutePath());
        }
        outState.putSerializable("selectedCards", new ArrayList<>(selectedCards));
    }

    private void createCollage() {
        int width = 250;  // Her bir görüntünün genişliği
        int height = 300; // Her bir görüntünün yüksekliği
        int columns = 3;  // Kolajdaki sütun sayısı
        int rows = (int) Math.ceil((double) imageBitmaps.size() / columns); // Satır sayısını hesapla

        // Kolaj için boş bir bitmap oluştur
        collageBitmap = Bitmap.createBitmap(columns * width, rows * height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(collageBitmap);

        for (int i = 0; i < imageBitmaps.size(); i++) {
            // Resmi uygun boyutlarda ölçekle
            Bitmap scaledImage = Bitmap.createScaledBitmap(imageBitmaps.get(i), width, height, false);

            // Resmin konumunu hesapla ve çiz
            int row = i / columns;
            int col = i % columns;
            canvas.drawBitmap(scaledImage, col * width, row * height, null);
        }

        // Kolajı ImageView'da göster
        binding.collageImageView.setImageBitmap(collageBitmap);

        // Kolajı dahili depolamaya kaydet
        saveImageToCreatedImages(collageBitmap);
    }

    private void saveImageToCreatedImages(Bitmap bitmap) {
        try {
            // 'createdImages' klasörünü oluştur
            File createdImagesFolder = new File(getFilesDir(), "createdImages");
            if (!createdImagesFolder.exists()) {
                createdImagesFolder.mkdirs();
            }

            // Kolaj için benzersiz bir dosya adı oluştur
            File collageFile = new File(createdImagesFolder, "collage_" + System.currentTimeMillis() + ".png");

            // Bitmap'i dosyaya kaydet
            FileOutputStream fos = new FileOutputStream(collageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            // Hata oluşursa kullanıcıya mesaj göster
            Toast.makeText(this, "Error saving collage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Kolaj yolunu geri yükle ve kolajı ImageView'da göster
        String collagePath = savedInstanceState.getString("collagePath");
        if (collagePath != null) {
            collageBitmap = BitmapFactory.decodeFile(collagePath);
            binding.collageImageView.setImageBitmap(collageBitmap);
        }
    }

    private void saveImageToGallery(Bitmap bitmap) {
        // Galeriye kaydetmek için gerekli ContentValues'i oluştur
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Collage");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Collage created in app");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        // Android 10 ve sonrası için özel işlem
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Collages");
        }

        // Galeride yeni bir resim URI'si oluştur
        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        // Resmi kaydet
        if (imageUri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    Toast.makeText(this, "Collage saved to gallery!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                // Hata oluşursa kullanıcıya mesaj göster
                Toast.makeText(this, "Error saving collage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            // URI oluşturulamazsa hata mesajı göster
            Toast.makeText(this, "Failed to save collage.", Toast.LENGTH_SHORT).show();
        }
    }
}
