package com.fatmavatansever.mobileproje.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fatmavatansever.mobileproje.R;
import com.fatmavatansever.mobileproje.models.VisionBoard;

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

        // Tıklama olayını ayarla
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(visionBoard));
    }

    @Override
    public int getItemCount() {
        return visionBoards.size();
    }

    static class VisionBoardViewHolder extends RecyclerView.ViewHolder {
        ImageView visionBoardImageView;

        public VisionBoardViewHolder(@NonNull View itemView) {
            super(itemView);
            visionBoardImageView = itemView.findViewById(R.id.visionboard_image);
        }
    }
}
