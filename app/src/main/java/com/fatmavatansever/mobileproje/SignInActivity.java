package com.fatmavatansever.mobileproje;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fatmavatansever.mobileproje.databinding.ActivitySignupBinding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        firestore = FirebaseFirestore.getInstance();
    }

    public void signInClicked(View view) {
        String username = binding.usernameEditText.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_LONG).show();
            return;
        }
        firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Username already exists
                        Toast.makeText(this, "Username already taken. Please try another.", Toast.LENGTH_LONG).show();
                    } else {
                        // Save username to Firestore
                        saveUserToFirestore(username);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking username: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveUserToFirestore(String username) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);

        // Save data to Firestore
        firestore.collection("users").add(userData).addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Welcome, " + username, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(SignInActivity.this, PreferenceActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                    finish();
                    Log.d("SignInActivity", "Navigating to PreferenceActivity with username: " + username);

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving user: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    Log.e("SignInActivity", "Firestore save error: " + e.getLocalizedMessage());
                });
    }

}
