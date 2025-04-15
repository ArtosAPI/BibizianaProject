package com.example.biblioteka;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// FavoritesActivity.java
public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView rvFavorites;
    private FavoritesGridAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Инициализация элементов
        progressBar = findViewById(R.id.progressBar);
        rvFavorites = findViewById(R.id.rvFavorites);

        // Настройка сетки (3 колонки)
        int spanCount = 3;
        GridLayoutManager layoutManager = new GridLayoutManager(this, spanCount);
        rvFavorites.setLayoutManager(layoutManager);

        adapter = new FavoritesGridAdapter(new ArrayList<>(), this);
        rvFavorites.setAdapter(adapter);

        loadFavorites();
    }

    private void loadFavorites() {
        progressBar.setVisibility(View.VISIBLE);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Требуется авторизация", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("favorites")
                .whereEqualTo("userId", user.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        List<FavoriteBook> books = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            FavoriteBook book = document.toObject(FavoriteBook.class);
                            book.setId(document.getId()); // Устанавливаем ID документа
                            books.add(book);
                        }
                        adapter.updateList(books);
                    } else {
                        Toast.makeText(this, "Ошибка загрузки", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}