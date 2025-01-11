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
        binding = ActivityPreferencesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        username = getIntent().getStringExtra("userId");
        String visionBoardId = getIntent().getStringExtra("visionBoardId");

        if (username == null || username.isEmpty()) {
            finish();
            return;
        }

        goalsList = getGoals();
        goalAdapter = new GoalAdapter(goalsList);
        binding.goalsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.goalsRecyclerView.setAdapter(goalAdapter);

        binding.continueButton.setOnClickListener(v -> {
            List<String> selectedGoals = goalAdapter.getSelectedGoals();
            if (selectedGoals.isEmpty()) {
                Toast.makeText(this, "Please select at least one goal!", Toast.LENGTH_SHORT).show();
            } else {
                List<String> mappedGoals = mapSelectedGoals(selectedGoals);
                savePreferences(mappedGoals, visionBoardId);
            }
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                startActivity(new Intent(this, HistoryActivity.class));
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("selectedGoals", new ArrayList<>(goalAdapter.getSelectedGoals()));
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        List<String> restoredSelectedGoals = savedInstanceState.getStringArrayList("selectedGoals");
        if (restoredSelectedGoals != null) {
            goalAdapter.setSelectedGoals(restoredSelectedGoals);
        }
    }

    @NonNull
    private List<Goal> getGoals() {
        List<Goal> goals = new ArrayList<>();
        goals.add(new Goal(R.drawable.heath, "Health"));
        goals.add(new Goal(R.drawable.travel, "Travel"));
        goals.add(new Goal(R.drawable.ed_and_carr, "Education and Career"));
        goals.add(new Goal(R.drawable.relationship, "Relationship"));
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

    private void savePreferences(List<String> preferences, String visionBoardId) {
        Map<String, Object> visionBoardData = new HashMap<>();
        visionBoardData.put("preferences", preferences);
        visionBoardData.put("likedImages", new ArrayList<>());

        firestore.collection("users")
                .document(username)
                .collection("visionBoards")
                .document(visionBoardId)
                .set(visionBoardData)
                .addOnSuccessListener(aVoid -> {


                    Intent intent = new Intent(PreferenceActivity.this, SwipeActivity.class);
                    intent.putExtra("userId", username);
                    intent.putExtra("visionBoardId", visionBoardId);
                    intent.putStringArrayListExtra("selectedTags", new ArrayList<>(preferences));
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Vision Board could not saved: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
