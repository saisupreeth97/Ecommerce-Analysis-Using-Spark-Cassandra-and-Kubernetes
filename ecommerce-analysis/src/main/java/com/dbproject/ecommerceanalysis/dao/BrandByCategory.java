package com.dbproject.ecommerceanalysis.dao;

import java.io.Serializable;
import java.math.BigInteger;

//Mapper for class to save brands count to DB
public class BrandByCategory implements Serializable {

    public static final long serialVersionUID = 7684054748945107893L;

    private String brand;
    private BigInteger count;

    public BrandByCategory()
    {

    }

    public BrandByCategory(String brand, BigInteger count) {
        this.brand = brand;
        this.count = count;
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
}
