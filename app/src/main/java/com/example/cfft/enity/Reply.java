package com.example.cfft.enity;

public class Reply {
    private String username;
    private String content;
    private String publishTime;

    public Reply(String username, String content, String publishTime) {
        this.username = username;
        this.content = content;
        this.publishTime = publishTime;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public String getPublishTime() {
        return publishTime;
    }
}
