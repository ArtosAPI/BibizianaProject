package com.example.biblioteka;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
    private List<FavoriteBook> favorites;

    public FavoritesAdapter(List<FavoriteBook> favorites) {
        this.favorites = favorites;
    }

    // Новый метод для обновления данных
    public void updateList(List<FavoriteBook> newList) {
        Log.d("FavoritesAdapter", "Updating list with " + newList.size() + " items");
        favorites = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.book_rv_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FavoriteBook book = favorites.get(position);

        // Убедитесь, что данные не null
        if (book == null) return;

        // Привязка данных
        holder.titleTV.setText(book.getTitle() != null ? book.getTitle() : "No Title");
        holder.subtitleTV.setText(book.getSubtitle() != null ? book.getSubtitle() : "");

        // Загрузка изображения
        if (book.getThumbnail() != null && !book.getThumbnail().isEmpty()) {
            Picasso.get()
                    .load(book.getThumbnail())
                    .into(holder.thumbnailIV);
        } else {
            holder.thumbnailIV.setImageResource(R.drawable.placeholder);
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTV, subtitleTV;
        ImageView thumbnailIV;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.idTVBookTitle);
            thumbnailIV = itemView.findViewById(R.id.idIVbook);
            subtitleTV = itemView.findViewById(R.id.idTVSubTitle);
        }
    }
}