package com.example.cfft.enity;

import java.util.List;

public class Post {
    private int postId;
    private String title;
    private String content;
    private String publishTime;
    private List<String> img;
    private int userId;
    private String userName;
    private String userImg;
    private int likeCount;
    private int viewCount;
    private int commentCount;

    // 私有构造函数，使用 Builder 来创建实例
    private Post(Builder builder) {
        this.postId = builder.postId;
        this.title = builder.title;
        this.content = builder.content;
        this.publishTime = builder.publishTime;
        this.img = builder.img;
        this.userId = builder.userId;
        this.userName = builder.userName;
        this.userImg = builder.userImg;
        this.likeCount = builder.likeCount;
        this.viewCount = builder.viewCount;
        this.commentCount = builder.commentCount;
    }

    // Getters
    public int getPostId() { return postId; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getPublishTime() { return publishTime; }
    public List<String> getImg() { return img; }
    public int getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getUserImg() { return userImg; }
    public int getLikeCount() { return likeCount; }
    public int getViewCount() { return viewCount; }
    public int getCommentCount() { return commentCount; }

    // Builder 静态内部类
    public static class Builder {
        private int postId;
        private String title;
        private String content;
        private String publishTime;
        private List<String> img;
        private int userId;
        private String userName;
        private String userImg;
        private int likeCount;
        private int viewCount;
        private int commentCount;

        public Builder postId(int postId) {
            this.postId = postId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder publishTime(String publishTime) {
            this.publishTime = publishTime;
            return this;
        }

        public Builder img(List<String> img) {
            this.img = img;
            return this;
        }

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder userImg(String userImg) {
            this.userImg = userImg;
            return this;
        }

        public Builder likeCount(int likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public Builder viewCount(int viewCount) {
            this.viewCount = viewCount;
            return this;
        }

        public Builder commentCount(int commentCount) {
            this.commentCount = commentCount;
            return this;
        }

        public Post build() {
            return new Post(this);
        }
    }
}
