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

    private static final int MAX_LIKED_CARDS = 15;  // Maksimum beğenilen kart sayısı

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainSwipeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<String> selectedTags = getIntent().getStringArrayListExtra("selectedTags");  // Seçilen etiketler
        username = getIntent().getStringExtra("userId");  // Kullanıcı adı
        visionBoardId = getIntent().getStringExtra("visionBoardId");  // Vision Board ID

        // Kullanıcı adı eksikse uygulamayı sonlandır
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Username not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        selectedCards = new ArrayList<>();
        allCards = new ArrayList<>();

        setupCardStackManager();  // Kart yığını yöneticisini kur

        // Eğer önceki durum kaydedildiyse, onu geri yükle
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState);
        } else {
            if (selectedTags != null && !selectedTags.isEmpty()) {
                fetchImagesForTags(selectedTags);  // Etiketlere göre görselleri çek
            } else {
                Log.e("SwipeActivity", "No tags received");
                Toast.makeText(this, "No tags selected.", Toast.LENGTH_SHORT).show();
            }
        }

        // Bitir ve kaydet butonuna tıklanırsa
        binding.finishSaveButton.setOnClickListener(v -> {
            if (!selectedCards.isEmpty()) {
                navigateToCollageActivity();  // Kolaj aktivitesine git
            } else {
                Toast.makeText(this, "You must like at least one card before finishing.", Toast.LENGTH_SHORT).show();
            }
        });

        // Alt navigasyon menüsü öğelerine tıklama işlevi
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                showNavigationConfirmationDialog(MainActivity.class);  // Ana sayfaya gitmeden onay al
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                showNavigationConfirmationDialog(HistoryActivity.class);  // Geçmişe gitmeden onay al
                return true;
            } else {
                return false;
            }
        });
    }

    private void setupCardStackManager() {
        cardStackLayoutManager = new CardStackLayoutManager(this, new CardStackListener());
        cardStackLayoutManager.setStackFrom(StackFrom.Top);  // Kartlar üstten başlasın
        cardStackLayoutManager.setVisibleCount(3);  // Görünür kart sayısını 3 yap
        cardStackLayoutManager.setTranslationInterval(8.0f);  // Kartlar arasındaki yatay mesafe
        cardStackLayoutManager.setScaleInterval(0.95f);  // Kartların küçülme oranı
        cardStackLayoutManager.setSwipeThreshold(0.3f);  // Kaydırma eşiği
        cardStackLayoutManager.setMaxDegree(20.0f);  // Maksimum dönüş açısı
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.Manual);  // Manuel kaydırma

        binding.cardStackView.setLayoutManager(cardStackLayoutManager);  // Kart yığını görünümünü ayarla

        swipeCardAdapter = new SwipeCardAdapter(allCards);  // Kartları listeye ekle
        binding.cardStackView.setAdapter(swipeCardAdapter);
    }

    // Durum kaydını geri yükleme
    private void restoreInstanceState(Bundle savedInstanceState) {
        int restoredPosition = savedInstanceState.getInt("currentPosition", 0);  // Kartların mevcut konumu
        List<SwipeCard> restoredSelectedCards = savedInstanceState.getParcelableArrayList("selectedCards");  // Seçilen kartlar
        List<SwipeCard> restoredAllCards = savedInstanceState.getParcelableArrayList("allCards");  // Tüm kartlar

        if (restoredSelectedCards != null) {
            selectedCards.clear();
            selectedCards.addAll(restoredSelectedCards);
        }

        if (restoredAllCards != null) {
            allCards.clear();
            allCards.addAll(restoredAllCards);
            swipeCardAdapter.notifyDataSetChanged();  // Adaptörü güncelle
        }

        cardStackLayoutManager.scrollToPosition(restoredPosition);  // Kart yığını pozisyonunu geri yükle
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Durum verilerini kaydet
        outState.putInt("currentPosition", cardStackLayoutManager.getTopPosition());
        outState.putParcelableArrayList("selectedCards", new ArrayList<>(selectedCards));
        outState.putParcelableArrayList("allCards", new ArrayList<>(allCards));
    }

    // Kullanıcı, başka bir sayfaya gitmek istediğinde onay isteği göster
    private void showNavigationConfirmationDialog(Class<?> destinationActivity) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Navigation")
                .setMessage("Your vision board creation progress will be lost. Are you sure you want to navigate away?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    startActivity(new Intent(this, destinationActivity));  // Hedef sayfaya git
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())  // İptal et
                .show();
    }

    // Kart kaydırma işlemi tamamlandığında yapılacak işlemler
    private class CardStackListener implements com.yuyakaido.android.cardstackview.CardStackListener {
        @Override
        public void onCardSwiped(Direction direction) {
            if (direction == Direction.Right) {  // Sağ kaydırma (beğenme)
                int currentPosition = cardStackLayoutManager.getTopPosition() - 1;
                SwipeCard likedCard = allCards.get(currentPosition);
                selectedCards.add(likedCard);  // Beğenilen kartı ekle

                if (selectedCards.size() >= MAX_LIKED_CARDS) {  // Maksimum beğenilen kart sayısına ulaşıldıysa
                    navigateToCollageActivity();  // Kolaj aktivitesine geç
                }
            }

            // Tüm kartlar kaydırıldığında kolaj ekranına git
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

    // Kolaj aktivitesine git
    private void navigateToCollageActivity() {
        saveLikedImagesToFirestore();  // Beğenilen görselleri Firestore'a kaydet
        Intent intent = new Intent(SwipeActivity.this, CollageActivity.class);
        intent.putExtra("selectedCards", new ArrayList<>(selectedCards));  // Seçilen kartları gönder
        startActivity(intent);
        finish();
    }

    // Beğenilen görselleri Firestore'a kaydet
    private void saveLikedImagesToFirestore() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        List<Map<String, String>> likedImages = new ArrayList<>();
        for (SwipeCard card : selectedCards) {
            Map<String, String> imageData = new HashMap<>();
            imageData.put("url", card.getImageUrl());
            imageData.put("tag", card.getTag());
            likedImages.add(imageData);
        }

        firestore.collection("users")  // Kullanıcı koleksiyonu
                .document(username)  // Kullanıcı adıyla belge
                .collection("visionBoards")  // Vision board koleksiyonu
                .document(visionBoardId)  // Vision board ID
                .update("likedImages", likedImages)  // Beğenilen görselleri güncelle
                .addOnSuccessListener(aVoid -> Log.d("SwipeActivity", "Liked images saved"))
                .addOnFailureListener(e -> Log.e("SwipeActivity", "Error saving images", e));
    }

    // Etiketlere göre görselleri çek
    private void fetchImagesForTags(List<String> tags) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        for (String tag : tags) {
            fetchImagesFromCloudinary(tag, null, requestQueue);
        }
    }

    // Cloudinary'den görselleri çek
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
                                allCards.add(new SwipeCard(imageUrl, tag));  // Kartları ekle
                            }
                        }
                        Collections.shuffle(allCards);  // Kartları karıştır
                        swipeCardAdapter.notifyDataSetChanged();  // Adaptörü güncelle
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
