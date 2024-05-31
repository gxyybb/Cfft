package com.example.cfft.enity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class CommunityItem implements Serializable {
    private int userAvatar;
    private String userName;
    private String content;
    private int imageResource;
    private int likeImageResource;
    private int commentImageResource;
    private Integer postId;
    private String title;
    private Date publishTime;
    private List<String> img;
    private Integer userId;
    private String userImg;
    private Integer likeCount;
    private Integer viewCount;
    private Integer commentCount;

    public CommunityItem(int userAvatar, String userName, String content, int imageResource, int likeImageResource, int commentImageResource, Integer postId, String title, Date publishTime, List<String> img, Integer userId, String userImg, Integer likeCount, Integer viewCount, Integer commentCount) {
        this.userAvatar = userAvatar;
        this.userName = userName;
        this.content = content;
        this.imageResource = imageResource;
        this.likeImageResource = likeImageResource;
        this.commentImageResource = commentImageResource;
        this.postId = postId;
        this.title = title;
        this.publishTime = publishTime;
        this.img = img;
        this.userId = userId;
        this.userImg = userImg;
        this.likeCount = likeCount;
        this.viewCount = viewCount;
        this.commentCount = commentCount;
    }

    public int getUserAvatar() {
        return userAvatar;
    }

    public String getUserName() {
        return userName;
    }

    public String getContent() {
        return content;
    }

    public int getImageResource() {
        return imageResource;
    }

    public int getLikeImageResource() {
        return likeImageResource;
    }

    public int getCommentImageResource() {
        return commentImageResource;
    }

    public Integer getPostId() {
        return postId;
    }

    public String getTitle() {
        return title;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public List<String> getImg() {
        return img;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getUserImg() {
        return userImg;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }
}
