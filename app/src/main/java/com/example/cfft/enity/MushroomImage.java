package com.example.cfft.enity;

public class MushroomImage {
    private int imgId;
    private int mushroomId;
    private String imgUrl;

    // 构造函数
    public MushroomImage(int imgId, int mushroomId, String imgUrl) {
        this.imgId = imgId;
        this.mushroomId = mushroomId;
        this.imgUrl = imgUrl;
    }

    // Getter 和 Setter 方法
    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public int getMushroomId() {
        return mushroomId;
    }

    public void setMushroomId(int mushroomId) {
        this.mushroomId = mushroomId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
