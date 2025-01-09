package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fatmavatansever.mobileproje.databinding.ActivityCollageBinding;
import com.fatmavatansever.mobileproje.models.SwipeCard;

import java.io.File;
import java.io.FileOutputStream;
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

        // Inflate binding
        binding = ActivityCollageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                                displayCollage();
                            }
                        }

                        @Override
                        public void onLoadCleared(android.graphics.drawable.Drawable placeholder) {
                        }
                    });
        }

        // Set up Save to Library button listener
        binding.saveToLibraryButton.setOnClickListener(v -> {
            if (collageBitmap != null) {
                saveCollageToLibrary(collageBitmap);
            } else {
                Toast.makeText(this, "Collage is not ready yet.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayCollage() {
        int collageWidth = 1080; // Screen width or desired width
        int collageHeight = 1920; // Screen height or desired height

        collageBitmap = CollageUtils.createDynamicCollage(imageBitmaps, collageWidth, collageHeight);

        // Display collage using binding
        binding.collageImageView.setImageBitmap(collageBitmap);
    }

    private void saveCollageToLibrary(Bitmap collage) {
        try {
            File storageDir = getExternalFilesDir(null); // App's private storage directory
            if (storageDir != null && !storageDir.exists()) {
                storageDir.mkdirs();
            }
            File collageFile = new File(storageDir, "collage_" + System.currentTimeMillis() + ".png");

            FileOutputStream fos = new FileOutputStream(collageFile);
            collage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();

            Toast.makeText(this, "Collage saved to library!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save collage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
