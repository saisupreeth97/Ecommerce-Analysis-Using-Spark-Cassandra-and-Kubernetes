package com.dbproject.ecommerceanalysis.dao;

import java.io.Serializable;
import java.math.BigInteger;

//Mapper for class to save event count to DB
public class EventCount implements Serializable {

    public static final long serialVersionUID = 1046998925038962439L;
    private String eventType;
    private BigInteger count;

    public EventCount()
    {

    }
    public EventCount(String eventType, BigInteger count) {
        this.eventType = eventType;
        this.count = count;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public BigInteger getCount() {
        return count;
    }

    public void setCount(BigInteger count) {
        this.count = count;
    }
}
