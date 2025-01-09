package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.fatmavatansever.mobileproje.databinding.ActivityMainSwipeBinding;
import com.fatmavatansever.mobileproje.adapters.SwipeCardAdapter;
import com.fatmavatansever.mobileproje.models.SwipeCard;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yuyakaido.android.cardstackview.CardStackLayoutManager;
import com.yuyakaido.android.cardstackview.CardStackView;
import com.yuyakaido.android.cardstackview.Direction;
import com.yuyakaido.android.cardstackview.StackFrom;
import com.yuyakaido.android.cardstackview.SwipeableMethod;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SwipeActivity extends AppCompatActivity {

    private ActivityMainSwipeBinding binding;
    private CardStackView cardStackView;
    private CardStackLayoutManager cardStackLayoutManager;
    private SwipeCardAdapter swipeCardAdapter;
    private List<SwipeCard> selectedCards;
    private List<SwipeCard> allCards;
    private final Set<String> fetchedImageUrls = new HashSet<>();
    private String username;
    private String visionBoardId;

    private static final int MAX_LIKED_CARDS = 15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainSwipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<String> selectedTags = getIntent().getStringArrayListExtra("selectedTags");
        username = getIntent().getStringExtra("userId");
        visionBoardId = getIntent().getStringExtra("visionBoardId");

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Username not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedCards = new ArrayList<>();
        allCards = new ArrayList<>();
        setupCardStackView();

        if (savedInstanceState != null) {
            restoreSavedInstanceState(savedInstanceState);
        } else {
            if (selectedTags != null && !selectedTags.isEmpty()) {
                fetchImagesForTags(selectedTags);
            } else {
                Log.e("SwipeActivity", "No tags received");
                Toast.makeText(this, "No tags selected.", Toast.LENGTH_SHORT).show();
            }
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                showNavigationConfirmationDialog(MainActivity.class);
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                showNavigationConfirmationDialog(HistoryActivity.class);
                return true;
            }
            return false;
        });
    }

    private void setupCardStackView() {
        cardStackLayoutManager = new CardStackLayoutManager(this, new CardStackListener());
        cardStackLayoutManager.setStackFrom(StackFrom.Top);
        cardStackLayoutManager.setVisibleCount(3);
        cardStackLayoutManager.setTranslationInterval(8.0f);
        cardStackLayoutManager.setScaleInterval(0.95f);
        cardStackLayoutManager.setSwipeThreshold(0.3f);
        cardStackLayoutManager.setMaxDegree(20.0f);
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.Manual);

        cardStackView = binding.cardStackView;
        cardStackView.setLayoutManager(cardStackLayoutManager);
        swipeCardAdapter = new SwipeCardAdapter(allCards);
        cardStackView.setAdapter(swipeCardAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("selectedCards", (ArrayList<? extends Parcelable>) selectedCards);
        outState.putParcelableArrayList("allCards", (ArrayList<? extends Parcelable>) allCards);
        outState.putInt("currentPosition", cardStackLayoutManager.getTopPosition());
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        selectedCards = savedInstanceState.getParcelableArrayList("selectedCards");
        allCards = savedInstanceState.getParcelableArrayList("allCards");

        if (allCards != null) {
            swipeCardAdapter = new SwipeCardAdapter(allCards);
            cardStackView.setAdapter(swipeCardAdapter);
        }

        int currentPosition = savedInstanceState.getInt("currentPosition", 0);
        cardStackLayoutManager.scrollToPosition(currentPosition);
    }

    private void showNavigationConfirmationDialog(Class<?> destinationActivity) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Navigation")
                .setMessage("Your vision board creation progress will be lost. Are you sure you want to navigate away?")
                .setPositiveButton("Yes", (dialog, which) -> startActivity(new Intent(this, destinationActivity)))
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private class CardStackListener implements com.yuyakaido.android.cardstackview.CardStackListener {
        @Override
        public void onCardDragging(Direction direction, float ratio) {}

        @Override
        public void onCardSwiped(Direction direction) {
            int currentPosition = cardStackLayoutManager.getTopPosition() - 1;

            if (direction == Direction.Right) {
                SwipeCard likedCard = swipeCardAdapter.getSwipeCardList().get(currentPosition);
                selectedCards.add(likedCard);
                Toast.makeText(SwipeActivity.this, "Liked", Toast.LENGTH_SHORT).show();

                if (selectedCards.size() >= MAX_LIKED_CARDS) {
                    saveLikedImagesToFirestore();
                    navigateToCollageActivity();
                }
            } else if (direction == Direction.Left) {
                Toast.makeText(SwipeActivity.this, "Disliked", Toast.LENGTH_SHORT).show();
            }

            if (cardStackLayoutManager.getTopPosition() == allCards.size()) {
                saveLikedImagesToFirestore();
                navigateToCollageActivity();
            }
        }

        @Override
        public void onCardRewound() {}
        @Override
        public void onCardCanceled() {}
        @Override
        public void onCardAppeared(View view, int position) {}
        @Override
        public void onCardDisappeared(View view, int position) {}
    }

    private void navigateToCollageActivity() {
        Intent intent = new Intent(SwipeActivity.this, CollageActivity.class);
        intent.putParcelableArrayListExtra("selectedCards", (ArrayList<SwipeCard>) selectedCards);
        startActivity(intent);
        finish();
    }

    private void saveLikedImagesToFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        List<Map<String, String>> likedImages = new ArrayList<>();

        for (SwipeCard card : selectedCards) {
            Map<String, String> imageData = new HashMap<>();
            imageData.put("url", card.getImageUrl());
            imageData.put("tag", card.getTag());
            likedImages.add(imageData);
        }

        firestore.collection("users")
                .document(username)
                .collection("visionBoards")
                .document(visionBoardId)
                .update("likedImages", likedImages)
                .addOnSuccessListener(aVoid -> Log.d("SwipeActivity", "Liked images saved to Firestore"))
                .addOnFailureListener(e -> Log.e("SwipeActivity", "Error saving liked images: ", e));
    }

    private void fetchImagesForTags(List<String> tags) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        for (String tag : tags) {
            fetchImagesFromCloudinary(tag, null, requestQueue);
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
                            String imageTag = tag;

                            if (!fetchedImageUrls.contains(imageUrl)) {
                                fetchedImageUrls.add(imageUrl);
                                fetchedCards.add(new SwipeCard(imageUrl, imageTag));
                            }
                        }

                        runOnUiThread(() -> {
                            allCards.addAll(fetchedCards);
                            Collections.shuffle(allCards);
                            swipeCardAdapter.notifyDataSetChanged();
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
