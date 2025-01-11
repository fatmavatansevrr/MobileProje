package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fatmavatansever.mobileproje.adapters.VisionBoardAdapter;
import com.fatmavatansever.mobileproje.databinding.ActivityHistoryBinding;
import com.fatmavatansever.mobileproje.models.VisionBoard;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private ActivityHistoryBinding binding;
    private List<VisionBoard> visionBoardList;
    private VisionBoardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate binding ve layout'u ayarla
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Vision board listesini başlat
        visionBoardList = new ArrayList<>();
        // VisionBoardAdapter ile listeyi bağla
        adapter = new VisionBoardAdapter(visionBoardList, this::onVisionBoardClick);

        // RecyclerView için LinearLayoutManager ayarla
        binding.visionboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.visionboardRecyclerView.setAdapter(adapter);

        // Durum bilgisi varsa, vision board listelerini geri yükle
        if (savedInstanceState != null) {
            ArrayList<VisionBoard> restoredList = (ArrayList<VisionBoard>) savedInstanceState.getSerializable("visionBoardList");
            if (restoredList != null) {
                Log.d(TAG, "Restoring vision boards from saved instance state.");
                visionBoardList.addAll(restoredList);
                adapter.notifyDataSetChanged();
            }
        } else {
            Log.d(TAG, "Loading vision boards from storage.");
            // Eğer durum kaydedilmemişse, verileri depolamadan yükle
            loadVisionBoards();
        }

        // BottomNavigationView için dinleyici ekle
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                // MainActivity'e git
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                return true;
            } else {
                return false;
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Current vision board listesini kaydet
        outState.putSerializable("visionBoardList", new ArrayList<>(visionBoardList));
        Log.d(TAG, "Vision board list saved to instance state.");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Vision board listesini restore et
        ArrayList<VisionBoard> restoredList = (ArrayList<VisionBoard>) savedInstanceState.getSerializable("visionBoardList");
        if (restoredList != null) {
            visionBoardList.clear();
            visionBoardList.addAll(restoredList);
            adapter.notifyDataSetChanged();
            Log.d(TAG, "Vision board list restored from instance state.");
        }
    }

    private void loadVisionBoards() {
        // 'createdImages' klasörünü al
        File createdImagesFolder = new File(getFilesDir(), "createdImages");

        // Eğer klasör varsa, içerisindeki dosyaları kontrol et
        if (createdImagesFolder.exists() && createdImagesFolder.isDirectory()) {
            File[] files = createdImagesFolder.listFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".png")) {
                        Log.d(TAG, "Loading vision board: " + file.getAbsolutePath());
                        // Görüntü dosyasını vision board listesine ekle
                        visionBoardList.add(new VisionBoard(file));
                    }
                }

                // RecyclerView'u güncelle
                adapter.notifyDataSetChanged();
            } else {
                // Eğer hiç dosya bulunamazsa uyarı göster
                Log.w(TAG, "No vision boards found in storage.");
                Toast.makeText(this, "No vision boards found!", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 'createdImages' klasörü bulunamazsa uyarı göster
            Log.e(TAG, "'createdImages' folder not found.");
            Toast.makeText(this, "No 'createdImages' folder found!", Toast.LENGTH_SHORT).show();
        }
    }

    public void onVisionBoardClick(VisionBoard visionBoard) {
        // Eğer vision board geçersizse veya dosya null ise hata göster
        if (visionBoard == null || visionBoard.getCollageFile() == null) {
            Log.e(TAG, "Invalid vision board or file is null.");
            Toast.makeText(this, "Invalid vision board!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dosyanın varlığını kontrol et
        File file = visionBoard.getCollageFile();
        if (!file.exists()) {
            Log.e(TAG, "Vision board file does not exist: " + file.getAbsolutePath());
            Toast.makeText(this, "Vision board file not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Detay sayfasına geçiş yap
        Intent intent = new Intent(this, VisionBoardDetailActivity.class);
        intent.putExtra("visionBoardPath", file.getAbsolutePath());
        startActivity(intent);

        // ActivityResultLauncher ile sonucu al
        detailLauncher.launch(intent);
    }

    // Detay sayfasından geri dönüş almak için launcher
    private ActivityResultLauncher<Intent> detailLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                // Geri dönüşte bir dosya silindiyse, listeden sil
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    String deletedPath = result.getData().getStringExtra("deletedPath");
                    if (deletedPath != null) {
                        // Listeden silinen öğeyi kaldır
                        for (int i = 0; i < visionBoardList.size(); i++) {
                            if (visionBoardList.get(i).getCollageFile().getAbsolutePath().equals(deletedPath)) {
                                visionBoardList.remove(i);
                                adapter.notifyItemRemoved(i);
                                break;
                            }
                        }
                    }
                }
            }
    );
}
