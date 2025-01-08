package com.fatmavatansever.mobileproje.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fatmavatansever.mobileproje.databinding.ItemCardBinding;
import com.fatmavatansever.mobileproje.models.SwipeCard;

import java.util.ArrayList;
import java.util.List;

public class SwipeCardAdapter extends RecyclerView.Adapter<SwipeCardAdapter.SwipeCardViewHolder> {

    private final List<SwipeCard> swipeCardList;

    // Constructor
    public SwipeCardAdapter(List<SwipeCard> swipeCardList) {
        this.swipeCardList = swipeCardList != null ? swipeCardList : new ArrayList<>();
    }

    @NonNull
    @Override
    public SwipeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use View Binding to inflate the layout
        ItemCardBinding binding = ItemCardBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new SwipeCardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeCardViewHolder holder, int position) {
        SwipeCard swipeCard = swipeCardList.get(position);

        // Load the image into the ImageView using Glide
        Glide.with(holder.binding.getRoot().getContext())
                .load(swipeCard.getImageUrl())
                .into(holder.binding.cardImage);

        // The tag is not displayed but can be used for logic or storage
        String tag = swipeCard.getTag();
        // Example: Use the tag for logging or additional logic
        // Log.d("SwipeCardAdapter", "Tag: " + tag);
    }

    @Override
    public int getItemCount() {
        return swipeCardList.size();
    }

    // Public method to access the list of cards
    public List<SwipeCard> getSwipeCardList() {
        return swipeCardList;
    }

    // ViewHolder class using View Binding
    static class SwipeCardViewHolder extends RecyclerView.ViewHolder {
        final ItemCardBinding binding;

        public SwipeCardViewHolder(@NonNull ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
