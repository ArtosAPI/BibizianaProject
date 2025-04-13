package com.example.biblioteka;

import java.util.ArrayList;

public class FavoriteBook {
    private String id;
    private String title;
    private String subtitle;
    private String thumbnail;
    private String userId; // ID текущего пользователя

    // Пустой конструктор (обязателен для Firestore)
    public FavoriteBook() {}

    // Конструктор с параметрами
    public FavoriteBook(String title, String subtitle, String thumbnail, String userId) {
        this.title = title;
        this.subtitle = subtitle;
        this.thumbnail = thumbnail;
        this.userId = userId;
    }

    // Геттеры
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getUserId() {
        return userId;
    }

    // Сеттеры
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}