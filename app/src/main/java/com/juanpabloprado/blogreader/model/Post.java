package com.juanpabloprado.blogreader.model;

/**
 * Created by Juan on 4/26/2015.
 */
public class Post {

    private final int id;
    private final String url;
    private final String title;
    private final String date;
    private final String author;
    private final String thumbnail;


    public Post(int id, String url, String title, String date, String author, String thumbnail) {
        this.id = id;
        this.url = url;
        this.title = title;
        this.date = date;
        this.author = author;
        this.thumbnail = thumbnail;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Post() {
        url = null;
        title = null;
        author = null;
        id = 0;
        date = null;
        thumbnail = null;
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
