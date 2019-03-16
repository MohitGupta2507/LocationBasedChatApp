package com.example.mohit.chatapp;

public class OnlineModel {

    private Double latitude,longitude;
    private String Uid;


    public OnlineModel() {
    }

    public OnlineModel(Double latitude, Double longitude, String uid) {
        this.latitude = latitude;
        this.longitude = longitude;
        Uid = uid;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }
}
