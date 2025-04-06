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

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class BookAdapter extends RecyclerView.Adapter<BookAdapter.BookViewHolder> {

    private final ArrayList<BookInfo> bookInfoArrayList;
    private final Context context;

    // Constructor to initialize adapter with data
    public BookAdapter(ArrayList<BookInfo> bookInfoArrayList, Context context) {
        this.bookInfoArrayList = bookInfoArrayList;
        this.context = context;
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

        // Set data to UI components
        if (bookInfo.getPublisher() == null) bookInfo.setPublisher("N/A");
        if (bookInfo.getPublishedDate() == null) bookInfo.setPublishedDate("N/A");

        holder.publisherTV.setText(bookInfo.getPublisher());
        holder.dateTV.setText("Published On: " + bookInfo.getPublishedDate());

        holder.nameTV.setText(bookInfo.getTitle());
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
            intent.putExtra("title", bookInfo.getTitle());
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
    }

    @Override
    public int getItemCount() {
        return bookInfoArrayList.size();
    }

    // ViewHolder class to hold UI elements for each item
    public static class BookViewHolder extends RecyclerView.ViewHolder {
        TextView nameTV, publisherTV, pageCountTV, dateTV;
        ImageView bookIV;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            // Initialize UI components
            nameTV = itemView.findViewById(R.id.idTVBookTitle);
            publisherTV = itemView.findViewById(R.id.idTVpublisher);
            pageCountTV = itemView.findViewById(R.id.idTVPageCount);
            dateTV = itemView.findViewById(R.id.idTVPublishDate);
            bookIV = itemView.findViewById(R.id.idIVbook);
        }
    }
}
