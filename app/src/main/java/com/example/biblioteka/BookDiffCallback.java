package com.example.biblioteka;

import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

class BookDiffCallback extends DiffUtil.Callback {
    private final List<BookInfo> oldList;
    private final List<BookInfo> newList;

    BookDiffCallback(List<BookInfo> oldList, List<BookInfo> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() { return oldList.size(); }

    @Override
    public int getNewListSize() { return newList.size(); }

    @Override
    public boolean areItemsTheSame(int oldPos, int newPos) {
        return oldList.get(oldPos).getTitle().equals(newList.get(newPos).getTitle());
    }

    @Override
    public boolean areContentsTheSame(int oldPos, int newPos) {
        return oldList.get(oldPos).equals(newList.get(newPos));
    }
}
