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

    private final List<SwipeCard> swipeCardList; // Swipe kartlarının listesi

    // Constructor (Yapıcı metod)
    public SwipeCardAdapter(List<SwipeCard> swipeCardList) {
        // Eğer verilen liste null değilse, onu kullan, aksi takdirde boş bir liste oluştur
        this.swipeCardList = swipeCardList != null ? swipeCardList : new ArrayList<>();
    }

    @NonNull
    @Override
    public SwipeCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // View Binding kullanarak layout'u şişir
        ItemCardBinding binding = ItemCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), // LayoutInflater ile bağlan
                parent, // Parent view'ı belirt
                false // False, çünkü şişirilen view doğrudan eklenmeyecek
        );
        return new SwipeCardViewHolder(binding); // ViewHolder'ı geri döndür
    }

    @Override
    public void onBindViewHolder(@NonNull SwipeCardViewHolder holder, int position) {
        // Verilen pozisyondaki SwipeCard nesnesini al
        SwipeCard swipeCard = swipeCardList.get(position);

        // Glide ile resmin URL'sini ImageView'a yükle
        Glide.with(holder.binding.getRoot().getContext())
                .load(swipeCard.getImageUrl()) // Görselin URL'si
                .into(holder.binding.cardImage); // Görseli ImageView'a yerleştir

        // Tag, görüntülenmeyen ancak mantık veya veri depolama için kullanılabilecek bir özellik
        String tag = swipeCard.getTag();
        // Örnek: Tag'i loglama veya ek mantık için kullanabilirsiniz
        // Log.d("SwipeCardAdapter", "Tag: " + tag);
    }

    @Override
    public int getItemCount() {
        return swipeCardList.size(); // Liste boyutunu döndür (kaç tane kart olduğunu)
    }

    // Kart listesini almak için kullanılan genel metod
    public List<SwipeCard> getSwipeCardList() {
        return swipeCardList; // Kartları geri döndür
    }

    // ViewHolder sınıfı, View Binding kullanarak
    static class SwipeCardViewHolder extends RecyclerView.ViewHolder {
        final ItemCardBinding binding; // View Binding nesnesi

        public SwipeCardViewHolder(@NonNull ItemCardBinding binding) {
            super(binding.getRoot()); // ItemCardBinding'den root view'a geçiş yap
            this.binding = binding; // Binding'i kaydet
        }
    }
}
