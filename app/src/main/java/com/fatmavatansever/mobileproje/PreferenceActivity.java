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
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreferenceActivity extends AppCompatActivity {

    private ActivityPreferencesBinding binding; // Binding class for activity_preferences.xml
    private GoalAdapter goalAdapter;
    private List<Goal> goalsList;
    private List<String> selectedGoals; // Seçili hedefleri tutmak için liste
    private FirebaseFirestore firestore;
    private String username;
    private String visionBoardId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreferencesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();

        // Kullanıcı ve Vision Board bilgilerini al
        if (savedInstanceState != null) {
            username = savedInstanceState.getString("username");
            visionBoardId = savedInstanceState.getString("visionBoardId");
            selectedGoals = savedInstanceState.getStringArrayList("selectedGoals");
        } else {
            username = getIntent().getStringExtra("userId");
            visionBoardId = getIntent().getStringExtra("visionBoardId");
            selectedGoals = new ArrayList<>();
        }

        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "Error: User ID not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        goalsList = getGoals();
        goalAdapter = new GoalAdapter(goalsList, selectedGoals);
        binding.goalsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.goalsRecyclerView.setAdapter(goalAdapter);

        binding.continueButton.setOnClickListener(v -> {
            List<String> selectedGoals = goalAdapter.getSelectedGoals();
            if (selectedGoals.isEmpty()) {
                Toast.makeText(this, "Please select at least one goal!", Toast.LENGTH_SHORT).show();
            } else {
                // Map combined goals to their individual components
                List<String> mappedGoals = mapSelectedGoals(selectedGoals);

                for (String goal : mappedGoals) {
                    System.out.println("Mapped Goal: " + goal); // For debugging
                }

                savePreferences(mappedGoals, visionBoardId);
            }
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                // MainActivity'e git
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                // HistoryActivity'e git
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    // Dummy goals data
    @NonNull
    private List<Goal> getGoals() {
        List<Goal> goals = new ArrayList<>();
        goals.add(new Goal(R.drawable.heath, "Health"));
        goals.add(new Goal(R.drawable.travel, "Travel"));
        goals.add(new Goal(R.drawable.travel, "Education and Career")); // Single UI option
        goals.add(new Goal(R.drawable.heath, "Relationship"));
        return goals;
    }

    private List<String> mapSelectedGoals(List<String> selectedGoals) {
        List<String> mappedGoals = new ArrayList<>();
        for (String goal : selectedGoals) {
            if (goal.equals("Education and Career")) {
                mappedGoals.add("Education");
                mappedGoals.add("Career");
            } else {
                mappedGoals.add(goal);
            }
        }
        return mappedGoals;
    }

    // Save the Vision Board to Firestore
    private void savePreferences(List<String> preferences, String visionBoardId) {
        Map<String, Object> visionBoardData = new HashMap<>();
        visionBoardData.put("preferences", preferences);
        visionBoardData.put("likedImages", new ArrayList<>()); // Initialize empty liked images

        firestore.collection("users")
                .document(username)
                .collection("visionBoards")
                .document(visionBoardId)
                .set(visionBoardData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Vision Board saved successfully!", Toast.LENGTH_SHORT).show();

                    // Navigate to SwipeActivity to handle liked images
                    Intent intent = new Intent(PreferenceActivity.this, SwipeActivity.class);
                    intent.putExtra("userId", username);
                    intent.putExtra("visionBoardId", visionBoardId);
                    intent.putStringArrayListExtra("selectedTags", new ArrayList<>(preferences));
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving Vision Board: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", username);
        outState.putString("visionBoardId", visionBoardId);
        outState.putStringArrayList("selectedGoals", new ArrayList<>(selectedGoals));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        username = savedInstanceState.getString("username");
        visionBoardId = savedInstanceState.getString("visionBoardId");
        selectedGoals = savedInstanceState.getStringArrayList("selectedGoals");
    }
}
