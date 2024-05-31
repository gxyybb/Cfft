package com.example.cfft.enity;

import java.io.Serializable;

public class UserProfile implements Serializable {

    private int userId;
    private String username;
    private String email;
    private String registrationTime;
    private String avatar;
    private String gender;
    private String birthdate;
    private String address;
    private String bio;
    private int level;
    private String userImage;
    private String nickName;
    private String backImg;
    private int likeCount;
    private int postZanCount;
    private int commentZanCount;
    private int commentCommentCount;

    // Getters and Setters

    public static class Builder {
        private int userId;
        private String username;
        private String email;
        private String registrationTime;
        private String avatar;
        private String gender;
        private String birthdate;
        private String address;
        private String bio;
        private int level;
        private String userImage;
        private String nickName;
        private String backImg;
        private int likeCount;
        private int postZanCount;
        private int commentZanCount;
        private int commentCommentCount;

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder registrationTime(String registrationTime) {
            this.registrationTime = registrationTime;
            return this;
        }

        public Builder avatar(String avatar) {
            this.avatar = avatar;
            return this;
        }

        public Builder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public Builder birthdate(String birthdate) {
            this.birthdate = birthdate;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder bio(String bio) {
            this.bio = bio;
            return this;
        }

        public Builder level(int level) {
            this.level = level;
            return this;
        }

        public Builder userImage(String userImage) {
            this.userImage = userImage;
            return this;
        }

        public Builder nickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public Builder backImg(String backImg) {
            this.backImg = backImg;
            return this;
        }

        public Builder likeCount(int likeCount) {
            this.likeCount = likeCount;
            return this;
        }

        public Builder postZanCount(int postZanCount) {
            this.postZanCount = postZanCount;
            return this;
        }

        public Builder commentZanCount(int commentZanCount) {
            this.commentZanCount = commentZanCount;
            return this;
        }

        public Builder commentCommentCount(int commentCommentCount) {
            this.commentCommentCount = commentCommentCount;
            return this;
        }

        public UserProfile build() {
            UserProfile userProfile = new UserProfile();
            userProfile.userId = this.userId;
            userProfile.username = this.username;
            userProfile.email = this.email;
            userProfile.registrationTime = this.registrationTime;
            userProfile.avatar = this.avatar;
            userProfile.gender = this.gender;
            userProfile.birthdate = this.birthdate;
            userProfile.address = this.address;
            userProfile.bio = this.bio;
            userProfile.level = this.level;
            userProfile.userImage = this.userImage;
            userProfile.nickName = this.nickName;
            userProfile.backImg = this.backImg;
            userProfile.likeCount = this.likeCount;
            userProfile.postZanCount = this.postZanCount;
            userProfile.commentZanCount = this.commentZanCount;
            userProfile.commentCommentCount = this.commentCommentCount;
            return userProfile;
        }
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRegistrationTime() {
        return registrationTime;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getAddress() {
        return address;
    }

    public String getBio() {
        return bio;
    }

    public int getLevel() {
        return level;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getNickName() {
        return nickName;
    }

    public String getBackImg() {
        return backImg;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public int getPostZanCount() {
        return postZanCount;
    }

    public int getCommentZanCount() {
        return commentZanCount;
    }

    public int getCommentCommentCount() {
        return commentCommentCount;
    }
}
