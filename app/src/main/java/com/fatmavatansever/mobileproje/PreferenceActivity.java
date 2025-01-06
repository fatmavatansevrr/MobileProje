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

    private ActivityPreferencesBinding binding; // Binding class for activity_preferences.xml
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
        username = getIntent().getStringExtra("username");

        // Set up RecyclerView
        goalsList = getGoals();
        goalAdapter = new GoalAdapter(goalsList);
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

                savePreferences(mappedGoals);
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

    // Map combined goals to their components
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

    // Save preferences to Firestore
    public void savePreferences(List<String> preferences) {
        Map<String, Object> userPreferences = new HashMap<>();
        userPreferences.put("preferences", preferences);

        firestore.collection("users")
                .document(username)
                .set(userPreferences) // Use set() to create or update the document
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Preferences saved!", Toast.LENGTH_SHORT).show();

                    // Pass selected goals as tags to SwipeActivity
                    Intent intent = new Intent(PreferenceActivity.this, SwipeActivity.class);
                    intent.putStringArrayListExtra("selectedTags", new ArrayList<>(preferences));
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
