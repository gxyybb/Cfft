package com.example.cfft.enity;



import lombok.Data;

import java.util.List;

@Data
public class MushRoomVO {
    /**
     * ID
     */

    private Integer mushroomId;

    /**
     * 名称
     */

    private String mushroomName;

    /**
     * 分类ID
     */

    private String category;

    /**
     * 图片URL
     */

    private List<String> mushroomImages;

    /**
     * 是否能食用(不可以0，可以1，其他3)
     */

    private Integer isEat;

    /**
     * 分布地点
     */

    private String mushroomLocation;

    /**
     * 描述
     */

    private String mushroomDesc;

    /**
     * 是否有毒(不含毒0，有毒1，未知2)
     */

    private Integer isPoison;

    public Integer getMushroomId() {
        return mushroomId;
    }

    public void setMushroomId(Integer mushroomId) {
        this.mushroomId = mushroomId;
    }

    public String getMushroomName() {
        return mushroomName;
    }

    public void setMushroomName(String mushroomName) {
        this.mushroomName = mushroomName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getMushroomImages() {
        return mushroomImages;
    }

    public void setMushroomImages(List<String> mushroomImages) {
        this.mushroomImages = mushroomImages;
    }

    public Integer getIsEat() {
        return isEat;
    }

    public void setIsEat(Integer isEat) {
        this.isEat = isEat;
    }

    public String getMushroomLocation() {
        return mushroomLocation;
    }

    public void setMushroomLocation(String mushroomLocation) {
        this.mushroomLocation = mushroomLocation;
    }

    public String getMushroomDesc() {
        return mushroomDesc;
    }

    public void setMushroomDesc(String mushroomDesc) {
        this.mushroomDesc = mushroomDesc;
    }

    public Integer getIsPoison() {
        return isPoison;
    }

    public void setIsPoison(Integer isPoison) {
        this.isPoison = isPoison;
    }
}
