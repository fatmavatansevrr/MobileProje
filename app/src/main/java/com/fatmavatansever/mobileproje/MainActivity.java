package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.content.SharedPreferences;
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

    private ActivityMainBinding binding; // MainActivity için View Binding
    private FirebaseFirestore firestore; // Firestore örneği
    private SharedPreferences sharedPreferences; // SharedPreferences örneği
    private static final String SHARED_PREFS_NAME = "VisionBoardAppPrefs"; // SharedPreferences adı
    private static final String USER_ID_KEY = "userId"; // Kullanıcı ID anahtarı
    private String userId; // Kullanıcı ID'sini saklamak için değişken

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Binding'i şişir (inflate)
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Firestore'u başlat
        firestore = FirebaseFirestore.getInstance();

        // SharedPreferences'ı başlat
        sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);

        // "VisionBoard Oluştur" butonuna tıklama olayını ayarla
        userId = getOrCreateUserId(); // Kullanıcı ID'sini al veya oluştur

        // VisionBoard oluşturma butonuna tıklama işlemi
        binding.createButton.setOnClickListener(v -> {
            String visionBoardId = UUID.randomUUID().toString(); // Yeni bir VisionBoard ID'si oluştur
            navigateToPreferences(userId, visionBoardId); // PreferenceActivity'ye geçiş yap
        });

        // Geçmiş (History) butonuna tıklama işlemi
        binding.historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class); // Geçmiş sayfasına geçiş
            startActivity(intent);
        });
    }

    // Kullanıcı ID'si almak veya oluşturmak için kullanılan metot
    private String getOrCreateUserId() {
        // SharedPreferences'ta bir kullanıcı ID'si olup olmadığını kontrol et
        String existingUserId = sharedPreferences.getString(USER_ID_KEY, null);

        if (existingUserId == null) {
            // Kullanıcı ID'si yoksa, yeni bir ID oluştur ve SharedPreferences'a kaydet
            String newUserId = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(USER_ID_KEY, newUserId).apply();

            // Firestore'da kullanıcıyı oluştur
            createUserInFirestore(newUserId);
            return newUserId; // Yeni oluşturulan ID'yi döndür
        } else {
            return existingUserId; // Mevcut kullanıcı ID'sini döndür
        }
    }

    /**
     * Firestore'da bir kullanıcı oluşturur.
     *
     * @param userId Cihaz için benzersiz kullanıcı ID'si.
     */
    private void createUserInFirestore(String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId); // Kullanıcı ID'sini veritabanına kaydet

        firestore.collection("users") // "users" koleksiyonuna veri ekle
                .document(userId) // Kullanıcıyı benzersiz ID ile tanımla
                .set(userData)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Hoşgeldiniz!", Toast.LENGTH_SHORT).show()) // Başarı mesajı
                .addOnFailureListener(e -> Toast.makeText(this, "Kullanıcı oluşturulurken hata: " + e.getMessage(), Toast.LENGTH_SHORT).show()); // Hata mesajı
    }

    /**
     * Kullanıcıyı PreferenceActivity'ye yönlendirir.
     *
     * @param userId Gelecek activity'ye gönderilecek kullanıcı ID'si.
     */
    private void navigateToPreferences(String userId, String visionBoardId) {
        Intent intent = new Intent(MainActivity.this, PreferenceActivity.class); // PreferenceActivity'ye geçiş
        intent.putExtra("userId", userId); // Kullanıcı ID'sini intent ile gönder
        intent.putExtra("visionBoardId", visionBoardId); // VisionBoard ID'sini intent ile gönder
        startActivity(intent); // Activity'yi başlat
    }

    /**
     * Firestore'da bir kullanıcı oluşturur ve PreferenceActivity'ye yönlendirir.
     *
     * @param userId Cihaz için benzersiz kullanıcı ID'si.
     */
    /*private void createUserAndNavigate(String userId) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userId", userId);
        userData.put("createdAt", System.currentTimeMillis());
        firestore.collection("users")
                .document(userId) // Kullanıcı belgesi ID'si, benzersiz kullanıcı ID'si ile
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Kullanıcı başarıyla oluşturuldu!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(MainActivity.this, PreferenceActivity.class);
                    intent.putExtra("userId", userId);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Kullanıcı oluşturulurken hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }*/
}
