package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.os.Bundle;
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
import com.fatmavatansever.mobileproje.adapters.SwipeCardAdapter;
import com.fatmavatansever.mobileproje.databinding.ActivityMainSwipeBinding;
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

        setupCardStackManager();

        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            if (selectedTags != null && !selectedTags.isEmpty()) {
                fetchImagesForTags(selectedTags);
            } else {
                Log.e("SwipeActivity", "No tags received");
                Toast.makeText(this, "No tags selected.", Toast.LENGTH_SHORT).show();
            }
        }

        binding.finishSaveButton.setOnClickListener(v -> {
            if (!selectedCards.isEmpty()) {
                navigateToCollageActivity();
            } else {
                Toast.makeText(this, "You must like at least one card before finishing.", Toast.LENGTH_SHORT).show();
            }
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                showNavigationConfirmationDialog(MainActivity.class);
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                showNavigationConfirmationDialog(HistoryActivity.class);
                return true;
            } else {
                return false;
            }
        });
    }

    private void setupCardStackManager() {
        cardStackLayoutManager = new CardStackLayoutManager(this, new CardStackListener());
        cardStackLayoutManager.setStackFrom(StackFrom.Top);
        cardStackLayoutManager.setVisibleCount(3);
        cardStackLayoutManager.setTranslationInterval(8.0f);
        cardStackLayoutManager.setScaleInterval(0.95f);
        cardStackLayoutManager.setSwipeThreshold(0.3f);
        cardStackLayoutManager.setMaxDegree(20.0f);
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.Manual);

        binding.cardStackView.setLayoutManager(cardStackLayoutManager);

        swipeCardAdapter = new SwipeCardAdapter(allCards);
        binding.cardStackView.setAdapter(swipeCardAdapter);
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        int restoredPosition = savedInstanceState.getInt("currentPosition", 0);
        List<SwipeCard> restoredSelectedCards = savedInstanceState.getParcelableArrayList("selectedCards");
        List<SwipeCard> restoredAllCards = savedInstanceState.getParcelableArrayList("allCards");

        if (restoredSelectedCards != null) {
            selectedCards.clear();
            selectedCards.addAll(restoredSelectedCards);
        }

        if (restoredAllCards != null) {
            allCards.clear();
            allCards.addAll(restoredAllCards);
            swipeCardAdapter.notifyDataSetChanged();
        }

        cardStackLayoutManager.scrollToPosition(restoredPosition);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("currentPosition", cardStackLayoutManager.getTopPosition());
        outState.putParcelableArrayList("selectedCards", new ArrayList<>(selectedCards));
        outState.putParcelableArrayList("allCards", new ArrayList<>(allCards));
    }

    private void showNavigationConfirmationDialog(Class<?> destinationActivity) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Navigation")
                .setMessage("Your vision board creation progress will be lost. Are you sure you want to navigate away?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    startActivity(new Intent(this, destinationActivity));
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private class CardStackListener implements com.yuyakaido.android.cardstackview.CardStackListener {
        @Override
        public void onCardSwiped(Direction direction) {
            if (direction == Direction.Right) {
                int currentPosition = cardStackLayoutManager.getTopPosition() - 1;
                SwipeCard likedCard = allCards.get(currentPosition);
                selectedCards.add(likedCard);

                if (selectedCards.size() >= MAX_LIKED_CARDS) {
                    navigateToCollageActivity();
                }
            }

            if (cardStackLayoutManager.getTopPosition() == allCards.size()) {
                navigateToCollageActivity();
            }
        }

        @Override
        public void onCardDragging(Direction direction, float ratio) {}

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
        saveLikedImagesToFirestore();
        Intent intent = new Intent(SwipeActivity.this, CollageActivity.class);
        intent.putExtra("selectedCards", new ArrayList<>(selectedCards));
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
                .addOnSuccessListener(aVoid -> Log.d("SwipeActivity", "Liked images saved"))
                .addOnFailureListener(e -> Log.e("SwipeActivity", "Error saving images", e));
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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray resources = response.getJSONArray("resources");
                        for (int i = 0; i < resources.length(); i++) {
                            String imageUrl = resources.getJSONObject(i).getString("secure_url");
                            if (!fetchedImageUrls.contains(imageUrl)) {
                                fetchedImageUrls.add(imageUrl);
                                allCards.add(new SwipeCard(imageUrl, tag));
                            }
                        }
                        swipeCardAdapter.notifyDataSetChanged();
                    } catch (Exception e) {
                        Log.e("SwipeActivity", "Error parsing response", e);
                    }
                },
                error -> Log.e("SwipeActivity", "Error fetching images", error)) {
            @Override
            public Map<String, String> getHeaders() {
                String auth = "635241544292974:5hFcjW9rKvl9-ppHOqhpewp2Ixo";
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + Base64.encodeToString(auth.getBytes(), Base64.NO_WRAP));
                return headers;
            }
        };
        requestQueue.add(request);
    }
}