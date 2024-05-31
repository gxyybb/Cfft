package com.example.cfft.enity;

import java.util.List;

public class UserData {
    private String username; // 修改字段名为与服务器返回数据中的字段名匹配
    private String address; // 修改字段名为与服务器返回数据中的字段名匹配
    private String userImage;
    private List<String> texts;
    private String time;

    // 构造方法

    public UserData() {
    }

    public UserData(String username, String address, String userImage, List<String> texts, String time) {
        this.username = username;
        this.address = address;
        this.userImage = userImage;
        this.texts = texts;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public List<String> getTexts(int i) {
        return texts;
    }

    public void setTexts(List<String> texts) {
        this.texts = texts;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    // Getter 和 Setter 方法


}
