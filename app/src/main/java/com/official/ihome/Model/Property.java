package com.official.ihome.Model;

public class Property {
    private String propertyID, name, address, state, detail, imageUrl, OwnerID, latitude, longitude , validity, price,type,sale;

    public Property() {
    }

    public Property(String propertyID, String name, String address, String state, String detail, String imageUrl, String ownerID, String latitude, String longitude, String validity, String price, String type, String sale) {
        this.propertyID = propertyID;
        this.name = name;
        this.address = address;
        this.state = state;
        this.detail = detail;
        this.imageUrl = imageUrl;
        OwnerID = ownerID;
        this.latitude = latitude;
        this.longitude = longitude;
        this.validity = validity;
        this.price = price;
        this.type = type;
        this.sale = sale;
    }

    public String getPropertyID() {
        return propertyID;
    }

    public void setPropertyID(String propertyID) {
        this.propertyID = propertyID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOwnerID() {
        return OwnerID;
    }

    public void setOwnerID(String ownerID) {
        OwnerID = ownerID;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSale() {
        return sale;
    }

    public void setSale(String sale) {
        this.sale = sale;
    }
}