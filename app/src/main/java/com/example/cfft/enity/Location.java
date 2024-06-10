package com.example.cfft.enity;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Data;

@Data
public class Location implements Serializable {

    private Integer id;

    /**
     * 
     */
    public String province;

    /**
     * 
     */
    public String city;

    /**
     * 
     */
    public BigDecimal latitude;

    /**
     * 
     */
    public BigDecimal longitude;
    public String description;


    private static final long serialVersionUID = 1L;

}