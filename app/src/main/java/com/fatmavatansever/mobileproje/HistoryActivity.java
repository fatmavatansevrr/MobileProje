package com.fatmavatansever.mobileproje;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private static final String TAG = "HistoryActivity";
    private ActivityHistoryBinding binding;
    private List<VisionBoard> visionBoardList;
    private VisionBoardAdapter adapter;

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

        // Restore vision board list or load it for the first time
        if (savedInstanceState != null) {
            ArrayList<VisionBoard> restoredList = (ArrayList<VisionBoard>) savedInstanceState.getSerializable("visionBoardList");
            if (restoredList != null) {
                Log.d(TAG, "Restoring vision boards from saved instance state.");
                visionBoardList.addAll(restoredList);
                adapter.notifyDataSetChanged();
            }
        } else {
            Log.d(TAG, "Loading vision boards from storage.");
            loadVisionBoards();
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.bottom_home) {
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

        // Save the current vision board list
        outState.putSerializable("visionBoardList", new ArrayList<>(visionBoardList));
        Log.d(TAG, "Vision board list saved to instance state.");
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ArrayList<VisionBoard> restoredList = (ArrayList<VisionBoard>) savedInstanceState.getSerializable("visionBoardList");
        if (restoredList != null) {
            visionBoardList.clear();
            visionBoardList.addAll(restoredList);
            adapter.notifyDataSetChanged();
            Log.d(TAG, "Vision board list restored from instance state.");
        }
    }

    private void loadVisionBoards() {
        File createdImagesFolder = new File(getFilesDir(), "createdImages");

        if (createdImagesFolder.exists() && createdImagesFolder.isDirectory()) {
            File[] files = createdImagesFolder.listFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".png")) {
                        Log.d(TAG, "Loading vision board: " + file.getAbsolutePath());
                        visionBoardList.add(new VisionBoard(file));
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Log.w(TAG, "No vision boards found in storage.");
                Toast.makeText(this, "No vision boards found!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "'createdImages' folder not found.");
            Toast.makeText(this, "No 'createdImages' folder found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void onVisionBoardClick(VisionBoard visionBoard) {
        if (visionBoard == null || visionBoard.getCollageFile() == null) {
            Log.e(TAG, "Invalid vision board or file is null.");
            Toast.makeText(this, "Invalid vision board!", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = visionBoard.getCollageFile();
        if (!file.exists()) {
            Log.e(TAG, "Vision board file does not exist: " + file.getAbsolutePath());
            Toast.makeText(this, "Vision board file not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, VisionBoardDetailActivity.class);
        intent.putExtra("visionBoardPath", file.getAbsolutePath());
        startActivity(intent);
    }
}
