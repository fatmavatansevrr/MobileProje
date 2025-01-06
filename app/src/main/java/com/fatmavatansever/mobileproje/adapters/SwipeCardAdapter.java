package com.fatmavatansever.mobileproje.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fatmavatansever.mobileproje.R;
import com.fatmavatansever.mobileproje.models.SwipeCard;

import java.util.ArrayList;
import java.util.List;

public class SwipeCardAdapter extends RecyclerView.Adapter<SwipeCardAdapter.SwipeCardViewHolder> {

    private final List<SwipeCard> swipeCardList;

    public SwipeCardAdapter(List<SwipeCard> swipeCardList) {
        this.swipeCardList = swipeCardList != null ? swipeCardList : new ArrayList<>();
    }

    @NonNull
    @Override
    public SwipeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_card, parent, false);
        return new SwipeCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeCardViewHolder holder, int position) {
        SwipeCard swipeCard = swipeCardList.get(position);

        Glide.with(holder.itemView.getContext())
                .load(swipeCard.getImageUrl())
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return swipeCardList.size();
    }

    public List<SwipeCard> getSwipeCardList() {
        return swipeCardList;
    }

    static class SwipeCardViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public SwipeCardViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.card_image);
        }
    }
}
