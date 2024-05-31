package com.example.cfft.enity;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

@Data
public class CommentVO implements Serializable {
    private String userImage;
    private Integer commentId;
    private String content;
    private Date publishTime;
    private String username;
    private Integer postId;
    private Integer parentCommentId;
    private Integer likeCount;
    private Integer replyCount;

    public CommentVO(String userImage, Integer commentId, String content, Date publishTime, String username, Integer postId, Integer parentCommentId, Integer likeCount, Integer replyCount) {
        this.userImage = userImage;
        this.commentId = commentId;
        this.content = content;
        this.publishTime = publishTime;
        this.username = username;
        this.postId = postId;
        this.parentCommentId = parentCommentId;
        this.likeCount = likeCount;
        this.replyCount = replyCount;
    }

    public CommentVO() {

    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(Integer parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getReplyCount() {
        return replyCount;
    }

    public void setReplyCount(Integer replyCount) {
        this.replyCount = replyCount;
    }
}