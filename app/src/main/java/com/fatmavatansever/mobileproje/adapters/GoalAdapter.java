package com.fatmavatansever.mobileproje.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fatmavatansever.mobileproje.R;
import com.fatmavatansever.mobileproje.models.Goal;

import java.util.ArrayList;
import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    private final List<Goal> goalList;
    private final List<String> selectedGoals = new ArrayList<>();

    public GoalAdapter(List<Goal> goalList) {
        this.goalList = goalList;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preference, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);
        holder.goalText.setText(goal.getGoalName());

        Glide.with(holder.itemView.getContext())
                .load(goal.getImageResId())
                .into(holder.goalImage);

        // Highlight the card if selected
        if (selectedGoals.contains(goal.getGoalName())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#D1C4E9")); // Highlight color
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); // Default color
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectedGoals.contains(goal.getGoalName())) {
                selectedGoals.remove(goal.getGoalName());
            } else {
                selectedGoals.add(goal.getGoalName());
            }
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public List<String> getSelectedGoals() {
        return selectedGoals;
    }

    /**
     * Sets the selected goals and refreshes the UI to reflect the current state.
     *
     * @param selectedGoals List of selected goals to be restored.
     */
    public void setSelectedGoals(List<String> selectedGoals) {
        this.selectedGoals.clear();
        this.selectedGoals.addAll(selectedGoals);
        notifyDataSetChanged(); // Refresh the RecyclerView
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        ImageView goalImage;
        TextView goalText;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalImage = itemView.findViewById(R.id.goal_image);
            goalText = itemView.findViewById(R.id.goal_text);
        }
    }
}
