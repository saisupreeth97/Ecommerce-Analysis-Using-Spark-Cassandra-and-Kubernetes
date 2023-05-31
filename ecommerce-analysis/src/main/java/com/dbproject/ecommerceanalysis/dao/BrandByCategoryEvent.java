package com.dbproject.ecommerceanalysis.dao;

import java.io.Serializable;
import java.math.BigInteger;

//Mapper for class to save brands count to DB with filter event type
public class BrandByCategoryEvent implements Serializable {
    public static final long serialVersionUID = -9145078013564848791L;
    private String brand;
    private BigInteger count;
    private String eventType;

    public BrandByCategoryEvent()
    {

    }
    public BrandByCategoryEvent(String brand, BigInteger count, String eventType) {
        this.brand = brand;
        this.count = count;
        this.eventType = eventType;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigInteger getCount() {
        return count;
    }

    public void setCount(BigInteger count) {
        this.count = count;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
