package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.fatmavatansever.mobileproje.adapters.GoalAdapter;
import com.fatmavatansever.mobileproje.databinding.ActivityPreferencesBinding;
import com.fatmavatansever.mobileproje.models.Goal;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferenceActivity extends AppCompatActivity {

    private ActivityPreferencesBinding binding;
    private GoalAdapter goalAdapter;
    private List<Goal> goalsList;
    private FirebaseFirestore firestore;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreferencesBinding.inflate(getLayoutInflater()); // Binding'in şişirilmesi
        setContentView(binding.getRoot()); // Layout'un root kısmının set edilmesi

        firestore = FirebaseFirestore.getInstance(); // Firebase Firestore'a bağlan
        username = getIntent().getStringExtra("userId"); // Kullanıcı adı (userId) al
        String visionBoardId = getIntent().getStringExtra("visionBoardId"); // Vision board ID al

        // Eğer username boş veya null ise activity'i sonlandır
        if (username == null || username.isEmpty()) {
            finish();
            return;
        }

        goalsList = getGoals(); // Hedef listesi alın
        goalAdapter = new GoalAdapter(goalsList); // Adapter oluşturuluyor
        binding.goalsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // RecyclerView için layout manager ayarlanıyor
        binding.goalsRecyclerView.setAdapter(goalAdapter); // Adapter RecyclerView'a atanıyor

        // Continue butonuna tıklanıldığında yapılacak işlemler
        binding.continueButton.setOnClickListener(v -> {
            List<String> selectedGoals = goalAdapter.getSelectedGoals(); // Seçilen hedefler alınır
            if (selectedGoals.isEmpty()) {
                // Eğer hedef seçilmemişse kullanıcıya bilgi verilir
                Toast.makeText(this, "Please select at least one goal!", Toast.LENGTH_SHORT).show();
            } else {
                // Seçilen hedefler haritalanır ve veritabanına kaydedilir
                List<String> mappedGoals = mapSelectedGoals(selectedGoals);
                savePreferences(mappedGoals, visionBoardId);
            }
        });

        // Alt navigation view tıklandığında yapılacak işlemler
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                // Ana sayfaya geçiş
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                // Geçmiş sayfasına geçiş
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    // Activity state kaydedildiğinde çalışacak fonksiyon
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Seçilen hedefler kaydediliyor
        outState.putStringArrayList("selectedGoals", new ArrayList<>(goalAdapter.getSelectedGoals()));
    }

    // Activity state geri yüklendiğinde çalışacak fonksiyon
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Kaydedilen hedefler geri yükleniyor
        List<String> restoredSelectedGoals = savedInstanceState.getStringArrayList("selectedGoals");
        if (restoredSelectedGoals != null) {
            goalAdapter.setSelectedGoals(restoredSelectedGoals); // Seçilen hedefler geri yükleniyor
        }
    }

    // Hedef listesi döndüren metod
    @NonNull
    private List<Goal> getGoals() {
        List<Goal> goals = new ArrayList<>();
        goals.add(new Goal(R.drawable.heath, "Health")); // Sağlık hedefi ekleniyor
        goals.add(new Goal(R.drawable.travel, "Travel")); // Seyahat hedefi ekleniyor
        goals.add(new Goal(R.drawable.ed_and_carr, "Education and Career")); // Eğitim ve kariyer hedefi ekleniyor
        goals.add(new Goal(R.drawable.relationship, "Relationship")); // İlişki hedefi ekleniyor
        return goals;
    }

    // Seçilen hedefleri haritalayan metod (örn. "Education and Career" hedefini iki hedefe ayırma)
    private List<String> mapSelectedGoals(List<String> selectedGoals) {
        List<String> mappedGoals = new ArrayList<>();
        for (String goal : selectedGoals) {
            if (goal.equals("Education and Career")) {
                mappedGoals.add("Education"); // Eğitim hedefi ekleniyor
                mappedGoals.add("Career"); // Kariyer hedefi ekleniyor
            } else {
                mappedGoals.add(goal); // Diğer hedefler olduğu gibi ekleniyor
            }
        }
        return mappedGoals;
    }

    // Seçilen hedefleri Firestore'a kaydeden metod
    private void savePreferences(List<String> preferences, String visionBoardId) {
        Map<String, Object> visionBoardData = new HashMap<>();
        visionBoardData.put("preferences", preferences); // Hedefler preferences olarak kaydediliyor
        visionBoardData.put("likedImages", new ArrayList<>()); // Beğenilen görseller boş bir liste olarak kaydediliyor

        firestore.collection("users")
                .document(username)
                .collection("visionBoards")
                .document(visionBoardId)
                .set(visionBoardData) // Veritabanına kaydetme işlemi
                .addOnSuccessListener(aVoid -> {
                    // Başarıyla kaydedildiğinde SwipeActivity'e geçiş yapılır
                    Intent intent = new Intent(PreferenceActivity.this, SwipeActivity.class);
                    intent.putExtra("userId", username);
                    intent.putExtra("visionBoardId", visionBoardId);
                    intent.putStringArrayListExtra("selectedTags", new ArrayList<>(preferences));
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Kaydetme işlemi başarısız olduğunda hata mesajı gösterilir
                    Toast.makeText(this, "Vision Board could not saved: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
