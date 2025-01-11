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

    private final List<Goal> goalList; // Hedef listesi
    private final List<String> selectedGoals = new ArrayList<>(); // Seçilen hedefler listesi

    public GoalAdapter(List<Goal> goalList) {
        this.goalList = goalList; // Hedef listesi adaptöre geçiyor
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Item'ı bağlamak için layout dosyasını şişir
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preference, parent, false);
        return new GoalViewHolder(view); // GoalViewHolder döndür
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position); // Listeden hedefi al
        holder.goalText.setText(goal.getGoalName()); // Hedefin adını yerleştir

        // Görseli Glide ile yükle
        Glide.with(holder.itemView.getContext())
                .load(goal.getImageResId()) // Görsel kaynağını yükle
                .into(holder.goalImage); // Görseli ImageView'a yerleştir

        // Eğer hedef seçildiyse kartı vurgula
        if (selectedGoals.contains(goal.getGoalName())) {
            holder.itemView.setBackgroundColor(Color.parseColor("#D1C4E9")); // Seçili renk
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE); // Varsayılan renk
        }

        // Tıklama olayını ayarla
        holder.itemView.setOnClickListener(v -> {
            // Hedef seçilmişse kaldır, seçilmemişse ekle
            if (selectedGoals.contains(goal.getGoalName())) {
                selectedGoals.remove(goal.getGoalName());
            } else {
                selectedGoals.add(goal.getGoalName());
            }
            notifyItemChanged(position); // Görünümü güncelle
        });
    }

    @Override
    public int getItemCount() {
        return goalList.size(); // Hedeflerin sayısını döndür
    }

    public List<String> getSelectedGoals() {
        return selectedGoals; // Seçilen hedefleri döndür
    }

    /**
     * Seçilen hedefleri ayarlar ve UI'yi günceller.
     *
     * @param selectedGoals Seçilen hedeflerin listesi.
     */
    public void setSelectedGoals(List<String> selectedGoals) {
        this.selectedGoals.clear(); // Önceki seçilenleri temizle
        this.selectedGoals.addAll(selectedGoals); // Yeni seçilenleri ekle
        notifyDataSetChanged(); // RecyclerView'i güncelle
    }

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        ImageView goalImage; // Hedefin görseli
        TextView goalText; // Hedefin adı

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalImage = itemView.findViewById(R.id.goal_image); // Görseli bağla
            goalText = itemView.findViewById(R.id.goal_text); // Metni bağla
        }
    }
}
