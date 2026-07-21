package com.example.storehub.model;

import com.google.gson.annotations.SerializedName;

public class Post {
    @SerializedName("_id")
    private String id;
    private String title;
    private String content;
    private String image;
    private String author;
    private String createdAt;

    public Post() {}

    public Post(String title, String content, String image) {
        this.title = title;
        this.content = content;
        this.image = image;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
