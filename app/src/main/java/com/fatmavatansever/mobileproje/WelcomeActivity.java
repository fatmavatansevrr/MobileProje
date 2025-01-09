package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Şu anda bu sınıfta bir durum kaydı gerekmediği için burada herhangi bir veri kaydedilmiyor.
        // Eğer gelecekte kullanıcı durumu eklenirse buraya ilgili kod eklenebilir.
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Şu anda bu sınıfta bir durum kaydı gerekmediği için burada herhangi bir veri geri yüklenmiyor.
        // Eğer gelecekte kullanıcı durumu eklenirse buraya ilgili kod eklenebilir.
    }
}
