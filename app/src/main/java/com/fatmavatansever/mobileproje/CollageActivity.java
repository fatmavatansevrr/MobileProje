package com.fatmavatansever.mobileproje;
import android.graphics.Bitmap;
import java.util.List;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fatmavatansever.mobileproje.models.SwipeCard;
import java.util.ArrayList;

public class CollageActivity extends AppCompatActivity {

    private List<Bitmap> imageBitmaps = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collage);

        // Get the selected cards passed from SwipeActivity
        List<SwipeCard> selectedCards = (List<SwipeCard>) getIntent().getSerializableExtra("selectedCards");

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
    }

    private void displayCollage() {
        int collageWidth = 1080; // Screen width or desired width
        int collageHeight = 1920; // Screen height or desired height

        Bitmap collageBitmap = CollageUtils.createCollage(imageBitmaps, 2, collageWidth, collageHeight);

        // Display collage
        ImageView collageImageView = findViewById(R.id.collage_image_view);
        collageImageView.setImageBitmap(collageBitmap);
    }
}
