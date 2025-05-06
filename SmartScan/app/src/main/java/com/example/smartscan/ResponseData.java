package com.example.smartscan;

public class ResponseData {
    private String title;
    private String link;
    private String snippet;

    public ResponseData(String title, String link, String snippet) {
        this.title = title;
        this.link = link;
        this.snippet = snippet;
    }

    public String getTitle() {
        return title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public String getLink() {
        return link;
    }
}

