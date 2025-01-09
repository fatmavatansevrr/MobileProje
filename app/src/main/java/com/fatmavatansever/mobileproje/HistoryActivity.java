package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.fatmavatansever.mobileproje.adapters.VisionBoardAdapter;
import com.fatmavatansever.mobileproje.databinding.ActivityHistoryBinding;
import com.fatmavatansever.mobileproje.models.VisionBoard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private List<VisionBoard> visionBoardList;
    private VisionBoardAdapter adapter;
    private int recyclerViewPosition = 0; // Track the scroll position of RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate binding
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        visionBoardList = new ArrayList<>();
        adapter = new VisionBoardAdapter(visionBoardList, this::onVisionBoardClick);

        binding.visionboardRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.visionboardRecyclerView.setAdapter(adapter);

        if (savedInstanceState != null) {
            restoreSavedInstanceState(savedInstanceState);
        } else {
            // createdImages klasöründen dosyaları yükle
            loadVisionBoards();
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
                // Navigate to MainActivity
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (item.getItemId() == R.id.history_menu) {
                // Stay on HistoryActivity
                return true;
            } else {
                return false;
            }
        });
    }

    private void loadVisionBoards() {
        File createdImagesFolder = new File(getFilesDir(), "createdImages");

        if (createdImagesFolder.exists() && createdImagesFolder.isDirectory()) {
            File[] files = createdImagesFolder.listFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".png")) {
                        visionBoardList.add(new VisionBoard(file));
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "No vision boards found!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No 'createdImages' folder found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void onVisionBoardClick(VisionBoard visionBoard) {
        Intent intent = new Intent(this, VisionBoardDetailActivity.class);
        intent.putExtra("visionBoardPath", visionBoard.getCollageFile().getAbsolutePath());
        startActivity(intent);
    }

    // Save instance state to preserve RecyclerView scroll position and VisionBoard list
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the RecyclerView position
        recyclerViewPosition = ((LinearLayoutManager) binding.visionboardRecyclerView.getLayoutManager())
                .findFirstVisibleItemPosition();
        outState.putInt("recyclerViewPosition", recyclerViewPosition);

        // Save the VisionBoard list
        outState.putSerializable("visionBoardList", (ArrayList<VisionBoard>) visionBoardList);
    }

    private void restoreSavedInstanceState(Bundle savedInstanceState) {
        // Restore the VisionBoard list
        visionBoardList = (List<VisionBoard>) savedInstanceState.getSerializable("visionBoardList");
        if (visionBoardList == null) {
            visionBoardList = new ArrayList<>();
        }

        adapter = new VisionBoardAdapter(visionBoardList, this::onVisionBoardClick);
        binding.visionboardRecyclerView.setAdapter(adapter);

        // Restore the RecyclerView position
        recyclerViewPosition = savedInstanceState.getInt("recyclerViewPosition", 0);
        binding.visionboardRecyclerView.scrollToPosition(recyclerViewPosition);
    }
}
