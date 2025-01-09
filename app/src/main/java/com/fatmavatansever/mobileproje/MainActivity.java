package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.fatmavatansever.mobileproje.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // View Binding for MainActivity
    private FirebaseFirestore firestore; // Firestore instance
    private SharedPreferences sharedPreferences; // SharedPreferences instance
    private static final String SHARED_PREFS_NAME = "VisionBoardAppPrefs";
    private static final String USER_ID_KEY = "userId";
    private String userId; // Store user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);

        if (savedInstanceState != null) {
            // Restore userId from savedInstanceState
            userId = savedInstanceState.getString("userId");
        } else {
            // Get or create a new user ID
            userId = getOrCreateUserId();
        }

        // Set up "Create VisionBoard" button listener
        binding.createButton.setOnClickListener(v -> {
            String visionBoardId = UUID.randomUUID().toString();
            navigateToPreferences(userId, visionBoardId);
        });

        // Set up "History" button listener
        binding.historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        // Set up "Settings" button listener
        binding.settingsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Settings feature not implemented yet", Toast.LENGTH_SHORT).show();
        });
    }

    private String getOrCreateUserId() {
        // Check if a user ID already exists in SharedPreferences
        String existingUserId = sharedPreferences.getString(USER_ID_KEY, null);

        if (existingUserId == null) {
            // Generate a new user ID and save it to SharedPreferences
            String newUserId = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(USER_ID_KEY, newUserId).apply();

            // Create the user in Firestore
            createUserInFirestore(newUserId);
            return newUserId;
        } else {
            return existingUserId;
        }
    }

    /**
     * Create a user in Firestore.
     *
     * @param userId The unique user ID for the device.
     */
    private void createUserInFirestore(String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);

        firestore.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error creating user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /**
     * Navigate to the PreferenceActivity.
     *
     * @param userId The user ID to pass to the next activity.
     */
    private void navigateToPreferences(String userId, String visionBoardId) {
        Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
        intent.putExtra("userId", userId);
        intent.putExtra("visionBoardId", visionBoardId);
        startActivity(intent);
    }

    // Save instance state to preserve the userId
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("userId", userId); // Save userId
    }

    // Restore instance state for userId
    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        userId = savedInstanceState.getString("userId"); // Restore userId
    }

    /**
     * Create a user in Firestore and navigate to PreferenceActivity
     *
     * @param userId The unique user ID for the device
     */
    /*private void createUserAndNavigate(String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("createdAt", System.currentTimeMillis());
        firestore.collection("users")
                .document(userId) // User document ID is the unique user ID
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "User created successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }*/
}
