package com.example.biblioteka;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {
    private final Map<String, Boolean> favoriteCache = new HashMap<>();
    private ArrayList<BookInfo> bookInfoArrayList;
    private final Context context;
    private final Map<String, ListenerRegistration> listeners = new HashMap<>();

    private final FirebaseUser currentUser;
    private final FirebaseFirestore db;

    // Constructor to initialize adapter with data
    public BookAdapter(ArrayList<BookInfo> bookInfoArrayList, Context context) {
        this.bookInfoArrayList = bookInfoArrayList;
        this.context = context;
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onViewRecycled(@NonNull BookViewHolder holder) {
        super.onViewRecycled(holder);
        // Отписываемся от слушателей при переиспользовании ViewHolder
        if (listeners.containsKey(holder.getAdapterPosition())) {
            listeners.get(holder.getAdapterPosition()).remove();
        }
    }
    private void setupRealTimeUpdates(BookViewHolder holder, String bookId) {
        ListenerRegistration reg = db.collection("favorites")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("bookId", bookId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    boolean isFavorite = !value.isEmpty();
                    holder.ivFavorite.setImageResource(
                            isFavorite ?
                                    R.drawable.ic_favorite_filled :
                                    R.drawable.ic_favorite_border
                    );
                });

        listeners.put(String.valueOf(holder.getAdapterPosition()), reg);
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate layout for each RecyclerView item
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.book_rv_item, parent, false);

        return new BookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder holder, int position) {
        // Get current book info
        BookInfo bookInfo = bookInfoArrayList.get(position);

        // Загрузка данных книги
        holder.nameTV.setText(bookInfo.getUniqueId());

        // Проверка статуса избранного
        updateFavoriteIcon(holder.ivFavorite, bookInfo.getUniqueId());

        // Обработчик клика на иконку
        holder.ivFavorite.setOnClickListener(v -> {
            if (currentUser == null) {
                context.startActivity(new Intent(context, AuthActivity.class));
                return;
            }
            toggleFavorite(holder.ivFavorite, bookInfo);
        });

        // Set data to UI components
        if (bookInfo.getPublisher() == null) bookInfo.setPublisher("N/A");
        if (bookInfo.getPublishedDate() == null) bookInfo.setPublishedDate("N/A");

        holder.publisherTV.setText(bookInfo.getPublisher());
        holder.dateTV.setText("Published On: " + bookInfo.getPublishedDate());

        holder.nameTV.setText(bookInfo.getUniqueId());
        holder.pageCountTV.setText("Pages : " + bookInfo.getPageCount());

        // Load image from URL using Glide
        if (bookInfo.getThumbnail() != null && !bookInfo.getThumbnail().isEmpty()) {
            Glide.with(context)
                    .load(bookInfo.getThumbnail())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder) // Добавьте отдельную иконку для ошибок
                    .override(200, 300) // Фиксированный размер для кэширования
                    .centerCrop()
                    .into(holder.bookIV);
        } else {
            holder.bookIV.setImageResource(R.drawable.placeholder);
        }

        // Set click listener to open BookDetails activity with data
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookDetails.class);
            intent.putExtra("title", bookInfo.getUniqueId());
            intent.putExtra("subtitle", bookInfo.getSubtitle());
            intent.putExtra("authors", bookInfo.getAuthors());
            intent.putExtra("publisher", bookInfo.getPublisher());
            intent.putExtra("publishedDate", bookInfo.getPublishedDate());
            intent.putExtra("description", bookInfo.getDescription());
            intent.putExtra("pageCount", bookInfo.getPageCount());
            intent.putExtra("thumbnail", bookInfo.getThumbnail());
            intent.putExtra("previewLink", bookInfo.getPreviewLink());
            intent.putExtra("infoLink", bookInfo.getInfoLink());
            intent.putExtra("buyLink", bookInfo.getBuyLink());

            // Start new activity with intent
            context.startActivity(intent);
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

// 1. Используем массив для хранения состояния (обход final-переменных в лямбдах)
        final boolean[] isFavorite = {false};
        final String[] favoriteId = {null};

// Проверка, добавлена ли книга в избранное
        db.collection("favorites")
                .whereEqualTo("bookId", bookInfo.getUniqueId())
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    isFavorite[0] = !queryDocumentSnapshots.isEmpty();

                    // Сохраняем ID документа, если книга уже в избранном
                    if (!queryDocumentSnapshots.isEmpty()) {
                        favoriteId[0] = queryDocumentSnapshots.getDocuments().get(0).getId();
                    }

                    holder.ivFavorite.setImageResource(isFavorite[0]
                            ? R.drawable.ic_favorite_filled
                            : R.drawable.ic_favorite_border);
                });

// 2. Обработка клика с использованием сохраненных значений
        holder.ivFavorite.setOnClickListener(v -> {
            if (isFavorite[0]) {
                // Удаление из избранного
                db.collection("favorites").document(favoriteId[0]).delete()
                        .addOnSuccessListener(aVoid -> {
                            // Обновляем UI после удаления
                            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_border);
                            isFavorite[0] = false;
                        });
            } else {
                // Добавление в избранное
                FavoriteBook favorite = new FavoriteBook(
                        bookInfo.getUniqueId(),
                        bookInfo.getSubtitle(),
                        bookInfo.getThumbnail(),
                        user.getUid()
                );

                db.collection("favorites").add(favorite)
                        .addOnSuccessListener(documentReference -> {
                            // Обновляем UI после добавления
                            holder.ivFavorite.setImageResource(R.drawable.ic_favorite_filled);
                            isFavorite[0] = true;
                            favoriteId[0] = documentReference.getId(); // Сохраняем новый ID
                        });
            }
        });

    }

    @Override
    public int getItemCount() {
        return bookInfoArrayList.size();
    }

    // Добавим методы для обновления данных
    public void updateBooks(List<BookInfo> newBooks) {
        bookInfoArrayList = new ArrayList<>(newBooks);
    }

    public List<BookInfo> getBooks() {
        return bookInfoArrayList;
    }

    // ViewHolder class to hold UI elements for each item
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, publisherTV, pageCountTV, dateTV;
        ImageView bookIV;
        ImageView ivFavorite;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize UI components
            nameTV = itemView.findViewById(R.id.idTVBookTitle);
            publisherTV = itemView.findViewById(R.id.idTVpublisher);
            pageCountTV = itemView.findViewById(R.id.idTVPageCount);
            dateTV = itemView.findViewById(R.id.idTVPublishDate);
            bookIV = itemView.findViewById(R.id.idIVbook);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }

    private void updateFavoriteIcon(ImageView ivFavorite, String bookId) {
        if (currentUser == null) return;

        if (favoriteCache.containsKey(bookId)) {
            ivFavorite.setImageResource(
                    favoriteCache.get(bookId) ?
                            R.drawable.ic_favorite_filled :
                            R.drawable.ic_favorite_border
            );
            return;
        }

        db.collection("favorites")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnSuccessListener(query -> {
                    boolean isFavorite = !query.isEmpty();

                    favoriteCache.put(bookId, isFavorite);
                    ivFavorite.setImageResource(
                            isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border
                    );
                });
    }

    private void toggleFavorite(ImageView ivFavorite, BookInfo bookInfo) {
        String bookId = bookInfo.getUniqueId(); // Используйте уникальный ID вместо title

        db.collection("favorites")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("bookId", bookId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // Удаление из избранного
                            for (QueryDocumentSnapshot doc : task.getResult()) {
                                db.collection("favorites").document(doc.getId()).delete()
                                        .addOnSuccessListener(aVoid ->
                                                ivFavorite.setImageResource(R.drawable.ic_favorite_border));
                            }
                        } else {
                            // Добавление в избранное
                            Map<String, Object> favBook = new HashMap<>();
                            favBook.put("userId", currentUser.getUid());
                            favBook.put("bookId", bookId);
                            favBook.put("timestamp", FieldValue.serverTimestamp());

                            db.collection("favorites").add(favBook)
                                    .addOnSuccessListener(ref ->
                                            ivFavorite.setImageResource(R.drawable.ic_favorite_filled));
                        }
                    }
                });
    }
}