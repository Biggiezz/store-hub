package com.example.storehub.model;

import java.io.Serializable;


public class News implements Serializable {
    private String _id;
    private String title;
    private String content;
    private String image;
    private String status;
    private String author;
    private String createdAt;

    public News() {
    }

    // Constructor đầy đủ tham số
    public News(String _id, String title, String content, String image, String status, String author, String createdAt) {
        this._id = _id;
        this.title = title;
        this.content = content;
        this.image = image;
        this.status = status;
        this.author = author;
        this.createdAt = createdAt;
    }


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return com.example.storehub.utils.ImageUtils.getCorrectedImageUrl(image, com.example.storehub.services.HttpResquest.BASE_URL);
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
