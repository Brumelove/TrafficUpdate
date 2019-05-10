package com.mauritues.brume.bmtraffic.model;

public class LocationRecording {


    private double sourceLatitude;
    private double sourceLongitude;
    private String sourceLocationAddress;

    private double destinationLatitude;
    private double destinationLongitude;
    private String destinationLocationAddress;

    private String userId;
    private String token;

    public LocationRecording() {

    }
    public LocationRecording(double sourceLatitude, double sourceLongitude,
                             String sourceLocationAddress, double destinationLatitude,
                             double destinationLongitude, String destinationLocationAddress,
                             String userId, String token) {
        this.sourceLatitude = sourceLatitude;
        this.sourceLongitude = sourceLongitude;
        this.sourceLocationAddress = sourceLocationAddress;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.destinationLocationAddress = destinationLocationAddress;
        this.userId = userId;
        this.token = token;
    }

    public double getSourceLatitude() {
        return sourceLatitude;
    }

    public void setSourceLatitude(double sourceLatitude) {
        this.sourceLatitude = sourceLatitude;
    }

    public double getSourceLongitude() {
        return sourceLongitude;
    }

    public void setSourceLongitude(double sourceLongitude) {
        this.sourceLongitude = sourceLongitude;
    }

    public String getSourceLocationAddress() {
        return sourceLocationAddress;
    }

    public void setSourceLocationAddress(String sourceLocationAddress) {
        this.sourceLocationAddress = sourceLocationAddress;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public String getDestinationLocationAddress() {
        return destinationLocationAddress;
    }

    public void setDestinationLocationAddress(String destinationLocationAddress) {
        this.destinationLocationAddress = destinationLocationAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LocationRecording{" +
                "sourceLatitude=" + sourceLatitude +
                ", sourceLongitude=" + sourceLongitude +
                ", sourceLocationAddress='" + sourceLocationAddress + '\'' +
                ", destinationLatitude=" + destinationLatitude +
                ", destinationLongitude=" + destinationLongitude +
                ", destinationLocationAddress='" + destinationLocationAddress + '\'' +
                ", userId='" + userId + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
