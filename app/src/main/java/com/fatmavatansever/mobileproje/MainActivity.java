package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.fatmavatansever.mobileproje.databinding.ActivityMainBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding; // View Binding for MainActivity
    private FirebaseFirestore firestore; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();

        // Set up "Create VisionBoard" button listener
        binding.createButton.setOnClickListener(v -> {
            // Generate a unique user ID for this device
            String uniqueUserId = UUID.randomUUID().toString();
            createUserAndNavigate(uniqueUserId);
        });

        binding.historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        binding.settingsButton.setOnClickListener(v -> {
            Toast.makeText(this, "Settings feature not implemented yet", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Create a user in Firestore and navigate to PreferenceActivity
     *
     * @param userId The unique user ID for the device
     */
    private void createUserAndNavigate(String userId) {
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
    }
}
