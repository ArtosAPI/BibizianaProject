package com.example.biblioteka;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoritesGridAdapter extends RecyclerView.Adapter<FavoritesGridAdapter.ViewHolder> {
    private List<FavoriteBook> favorites;
    private Context context;

    public FavoritesGridAdapter(List<FavoriteBook> favorites, Context context) {
        this.favorites = favorites;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.book_rv_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteBook book = favorites.get(position);

        // Загрузка обложки
        Picasso.get()
                .load(book.getThumbnail())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(holder.ivBookCover);

        // Обработка клика на сердечко (удаление)
        holder.ivFavorite.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) return;

            FirebaseFirestore.getInstance()
                    .collection("favorites")
                    .document(book.getId())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        favorites.remove(adapterPosition);
                        notifyItemRemoved(adapterPosition);
                    });
        });

        // Обработка клика
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetails.class);
            intent.putExtra("title", book.getTitle());
            intent.putExtra("subtitle", book.getSubtitle());
            intent.putExtra("thumbnail", book.getThumbnail());
            // Добавьте остальные поля при необходимости
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public void updateList(List<FavoriteBook> newList) {
        favorites = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivFavorite;
        ImageView ivBookCover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBookCover = itemView.findViewById(R.id.ivBookCover);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}
