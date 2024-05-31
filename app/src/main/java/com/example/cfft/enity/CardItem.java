package com.example.cfft.enity;

public class CardItem {
    private int id;
    private String categoryName;
    private String image;
    private int number;
    private String tips;

    public CardItem(int id, String categoryName, String image, int number, String tips) {
        this.id = id;
        this.categoryName = categoryName;
        this.image = image;
        this.number = number;
        this.tips = tips;
    }

    public int getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getImage() {
        return image;
    }

    public int getNumber() {
        return number;
    }

    public String getTips() {
        return tips;
    }
}
