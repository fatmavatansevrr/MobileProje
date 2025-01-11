package com.fatmavatansever.mobileproje;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
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

    private ActivityCollageBinding binding;
    private List<Bitmap> imageBitmaps = new ArrayList<>();
    private Bitmap collageBitmap;
    private List<SwipeCard> selectedCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCollageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (savedInstanceState != null) {
            // Restore saved state
            selectedCards = (List<SwipeCard>) savedInstanceState.getSerializable("selectedCards");
            collageBitmap = savedInstanceState.getParcelable("collageBitmap");

            // Display restored collage
            if (collageBitmap != null) {
                binding.collageImageView.setImageBitmap(collageBitmap);
            }
        } else {
            // Get the selected cards passed from SwipeActivity
            selectedCards = (List<SwipeCard>) getIntent().getSerializableExtra("selectedCards");

            // Load images into Bitmap list
            for (SwipeCard card : selectedCards) {
                Glide.with(this)
                        .asBitmap()
                        .load(card.getImageUrl())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                imageBitmaps.add(resource);
                                if (imageBitmaps.size() == selectedCards.size()) {
                                    createCollage();
                                }
                            }

                            @Override
                            public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {
                            }
                        });
            }
        }

        // Set up Save to Gallery button listener
        binding.saveToLibraryButton.setOnClickListener(v -> {
            if (collageBitmap != null) {
                saveImageToGallery(collageBitmap);
            } else {
                Toast.makeText(this, "Collage is not ready yet.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                // Navigate to MainActivity
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                // Navigate to HistoryActivity
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

        // Save selected cards
        outState.putSerializable("selectedCards", new ArrayList<>(selectedCards));

        // Save the generated collage bitmap
        outState.putParcelable("collageBitmap", collageBitmap);
    }

    private void createCollage() {
        int width = 300;  // Each image width
        int height = 300; // Each image height
        int columns = 3;  // Number of columns in the collage
        int rows = (int) Math.ceil((double) imageBitmaps.size() / columns);

        // Create a blank bitmap for the collage
        collageBitmap = Bitmap.createBitmap(columns * width, rows * height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(collageBitmap);

        for (int i = 0; i < imageBitmaps.size(); i++) {
            Bitmap scaledImage = Bitmap.createScaledBitmap(imageBitmaps.get(i), width, height, false);

            int row = i / columns;
            int col = i % columns;
            canvas.drawBitmap(scaledImage, col * width, row * height, null);
        }

        // Display collage in ImageView
        binding.collageImageView.setImageBitmap(collageBitmap);

        saveImageToCreatedImages(collageBitmap);
    }

    private void saveImageToCreatedImages(Bitmap bitmap) {
        try {
            // Create 'createdImages' folder in the app's internal storage
            File createdImagesFolder = new File(getFilesDir(), "createdImages");
            if (!createdImagesFolder.exists()) {
                createdImagesFolder.mkdirs();
            }

            // Create a unique file for the collage
            File collageFile = new File(createdImagesFolder, "collage_" + System.currentTimeMillis() + ".png");

            // Write the bitmap to the file
            FileOutputStream fos = new FileOutputStream(collageFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Toast.makeText(this, "Collage saved to 'createdImages' folder!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving collage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageToGallery(Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Collage");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Collage created in app");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Collages");
        }

        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (imageUri != null) {
            try (OutputStream outputStream = getContentResolver().openOutputStream(imageUri)) {
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    Toast.makeText(this, "Collage saved to gallery!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "Error saving collage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Failed to save collage.", Toast.LENGTH_SHORT).show();
        }
    }
}
