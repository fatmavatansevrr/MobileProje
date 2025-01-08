package com.fatmavatansever.mobileproje.adapters;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fatmavatansever.mobileproje.R;
import com.fatmavatansever.mobileproje.models.VisionBoard;

import java.io.OutputStream;
import java.util.List;

public class VisionBoardAdapter extends RecyclerView.Adapter<VisionBoardAdapter.VisionBoardViewHolder> {

    private final List<VisionBoard> visionBoards;
    private final OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(VisionBoard visionBoard);
    }

    public VisionBoardAdapter(List<VisionBoard> visionBoards, OnItemClickListener onItemClickListener) {
        this.visionBoards = visionBoards;
        this.onItemClickListener = onItemClickListener;
    }
    private void saveImageToGallery(Context context, Bitmap bitmap) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "VisionBoard");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Downloaded from app");
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/png");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/VisionBoards");
        }

        Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if (uri != null) {
            try (OutputStream outputStream = context.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                Toast.makeText(context, "Image saved to gallery!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(context, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    @NonNull
    @Override
    public VisionBoardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_visionboard, parent, false);
        return new VisionBoardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VisionBoardViewHolder holder, int position) {
        VisionBoard visionBoard = visionBoards.get(position);

        // Kolaj dosyasını bitmap olarak yükle
        Bitmap bitmap = BitmapFactory.decodeFile(visionBoard.getCollageFile().getAbsolutePath());
        holder.visionBoardImageView.setImageBitmap(bitmap);




        // İndirme butonuna tıklama
        holder.downloadButton.setOnClickListener(v -> {
            saveImageToGallery(holder.itemView.getContext(), bitmap);
        });

        // Tıklama olayını ayarla
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(visionBoard));

        // Silme butonu tıklama olayı
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Are you sure you want to delete this?")
                    .setMessage("You are about to delete this VisionBoard. This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        // VisionBoard'u sil
                        boolean deleted = visionBoard.getCollageFile().delete();
                        if (deleted) {
                            visionBoards.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, visionBoards.size());
                            Toast.makeText(holder.itemView.getContext(), "VisionBoard deleted!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Could not be deleted!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return visionBoards.size();
    }

    static class VisionBoardViewHolder extends RecyclerView.ViewHolder {
        ImageView visionBoardImageView;
        ImageButton downloadButton; // Buton referansı
        ImageButton deleteButton;


        public VisionBoardViewHolder(@NonNull View itemView) {
            super(itemView);
            visionBoardImageView = itemView.findViewById(R.id.visionboard_image);
            downloadButton = itemView.findViewById(R.id.download_button); // indirme Buton bağlandı
            deleteButton = itemView.findViewById(R.id.delete_button); // Silme butonu

        }
    }

}
