package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.fatmavatansever.mobileproje.databinding.ActivityWelcomeBinding;
public class WelcomeActivity extends AppCompatActivity {
    private ActivityWelcomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the binding
        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up Sign In button listener
        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to Sign In activity
                Intent intent = new Intent(WelcomeActivity.this, SignInActivity.class);
                startActivity(intent);
            }
        });
    }
}
