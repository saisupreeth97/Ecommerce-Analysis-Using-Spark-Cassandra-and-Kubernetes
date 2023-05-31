package com.dbproject.ecommerceanalysis.dao;

import java.io.Serializable;

//Mapper for class to save average price to DB
public class AvgPrice implements Serializable {
    public static final long serialVersionUID = -5019748972053278075L;

    private String eventType;
    private Float price;

    public AvgPrice()
    {

    }
    public AvgPrice(String eventType, Float price) {
        this.eventType = eventType;
        this.price = price;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Float getPrice() {
        return price;
    }

    public void setPrice(Float price) {
        this.price = price;
    }
}
