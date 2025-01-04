package com.fatmavatansever.mobileproje;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import com.fatmavatansever.mobileproje.adapters.SwipeCardAdapter;
import com.fatmavatansever.mobileproje.models.SwipeCard;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SwipeActivity extends AppCompatActivity {

    private CardStackView cardStackView;
    private CardStackLayoutManager cardStackLayoutManager;
    private SwipeCardAdapter swipeCardAdapter;
    private List<SwipeCard> selectedCards;
    private List<SwipeCard> allCards; // Cards loaded and shuffled
    private final Set<String> fetchedImageUrls = new HashSet<>(); // Track unique image URLs
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_swipe);

        cardStackView = findViewById(R.id.card_stack_view);

        // Retrieve selected tags from Intent
        List<String> selectedTags = getIntent().getStringArrayListExtra("selectedTags");
        Log.d("SwipeActivity", "Received tags: " + selectedTags);

        username = getIntent().getStringExtra("username");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Username not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize lists
        selectedCards = new ArrayList<>();
        allCards = new ArrayList<>();

        // Set up CardStackLayoutManager
        cardStackLayoutManager = new CardStackLayoutManager(this, new CardStackListener());
        cardStackLayoutManager.setStackFrom(StackFrom.Top);
        cardStackLayoutManager.setVisibleCount(3);
        cardStackLayoutManager.setTranslationInterval(8.0f);
        cardStackLayoutManager.setScaleInterval(0.95f);
        cardStackLayoutManager.setSwipeThreshold(0.3f);
        cardStackLayoutManager.setMaxDegree(20.0f);
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.Manual);

        cardStackView.setLayoutManager(cardStackLayoutManager);

        // Set up adapter with empty list
        swipeCardAdapter = new SwipeCardAdapter(allCards);
        cardStackView.setAdapter(swipeCardAdapter);

        // Fetch images based on tags
        if (selectedTags != null && !selectedTags.isEmpty()) {
            fetchImagesForTags(selectedTags);
        } else {
            Log.e("SwipeActivity", "No tags received");
            Toast.makeText(this, "No tags selected.", Toast.LENGTH_SHORT).show();
        }
    }

    private class CardStackListener implements com.yuyakaido.android.cardstackview.CardStackListener {
        @Override
        public void onCardDragging(Direction direction, float ratio) {
            // Handle dragging (optional logging or UI changes)
        }

        @Override
        public void onCardSwiped(Direction direction) {
            if (direction == Direction.Right) {
                int currentPosition = cardStackLayoutManager.getTopPosition() - 1;
                SwipeCard likedCard = swipeCardAdapter.getSwipeCardList().get(currentPosition);
                selectedCards.add(likedCard);
                Toast.makeText(SwipeActivity.this, "Liked", Toast.LENGTH_SHORT).show();
            } else if (direction == Direction.Left) {
                Toast.makeText(SwipeActivity.this, "Disliked", Toast.LENGTH_SHORT).show();
            }

            // If last card swiped, navigate to CollageActivity
            if (cardStackLayoutManager.getTopPosition() == allCards.size()) {
                saveLikedImagesToFirestore(username); // Save liked images before transitioning
                Intent intent = new Intent(SwipeActivity.this, CollageActivity.class);
                intent.putExtra("selectedCards", (ArrayList<SwipeCard>) selectedCards);
                startActivity(intent);
                finish();
            }
        }

        @Override
        public void onCardRewound() {
            // Optional: Handle card rewind
        }

        @Override
        public void onCardCanceled() {
            // Optional: Handle swipe cancellation
        }

        @Override
        public void onCardAppeared(View view, int position) {
            // Optional: Handle card appearance
        }

        @Override
        public void onCardDisappeared(View view, int position) {
            // Optional: Handle card disappearance
        }
    }
    private void saveLikedImagesToFirestore(String username) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        List<String> imageUrls = new ArrayList<>();

        for (SwipeCard card : selectedCards) {
            imageUrls.add(card.getImageUrl());
        }

        // Use update to add likedImages to the user's document
        firestore.collection("users")
                .document(username) // Use the actual username to identify the document
                .update("likedImages", imageUrls) // Update or create the likedImages field
                .addOnSuccessListener(aVoid -> Log.d("SwipeActivity", "Liked images saved to Firestore"))
                .addOnFailureListener(e -> {
                    Log.e("SwipeActivity", "Error saving liked images: ", e);
                    // If the document doesn't exist, create it
                    Map<String, Object> likedImages = new HashMap<>();
                    likedImages.put("likedImages", imageUrls);

                    firestore.collection("users")
                            .document(username)
                            .set(likedImages)
                            .addOnSuccessListener(aVoid1 -> Log.d("SwipeActivity", "Document created with liked images"))
                            .addOnFailureListener(e1 -> Log.e("SwipeActivity", "Error creating document: ", e1));
                });
    }



    private void fetchImagesForTags(List<String> tags) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        for (String tag : tags) {
            fetchImagesFromCloudinary(tag, null, requestQueue); // Initial request with no cursor
        }
    }

    private void fetchImagesFromCloudinary(String tag, String nextCursor, RequestQueue requestQueue) {
        String url = "https://api.cloudinary.com/v1_1/ddemfak9f/resources/image/tags/" + tag;
        if (nextCursor != null) {
            url += "?next_cursor=" + nextCursor;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> new Thread(() -> {
                    try {
                        JSONArray resources = response.getJSONArray("resources");
                        String cursor = response.optString("next_cursor");

                        List<SwipeCard> fetchedCards = new ArrayList<>();
                        for (int i = 0; i < resources.length(); i++) {
                            String imageUrl = resources.getJSONObject(i).getString("secure_url");

                            // Add to the list only if the URL is not already fetched
                            if (!fetchedImageUrls.contains(imageUrl)) {
                                fetchedImageUrls.add(imageUrl);
                                fetchedCards.add(new SwipeCard(imageUrl));
                            }
                        }

                        // Add fetched images to the main list and shuffle
                        runOnUiThread(() -> {
                            allCards.addAll(fetchedCards);
                            Collections.shuffle(allCards); // Shuffle to ensure randomness
                            swipeCardAdapter.notifyDataSetChanged();

                            // Fetch next page if available
                            if (!cursor.isEmpty()) {
                                fetchImagesFromCloudinary(tag, cursor, requestQueue);
                            }
                        });
                    } catch (Exception e) {
                        Log.e("Cloudinary", "Error parsing JSON response", e);
                    }
                }).start(),
                error -> Log.e("Cloudinary", "Error fetching images: " + error.getMessage())
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String auth = "635241544292974:5hFcjW9rKvl9-ppHOqhpewp2Ixo";
                headers.put("Authorization", "Basic " + Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP));
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }
}
