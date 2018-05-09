package edu.rosehulman.ciepieab.gatego;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by reesemm on 4/23/2018.
 */

public class Gate {

    private double latitude;
    private double longitude;
    private LatLng coordinate;
    private String gateID;
    private String label;
    private String airportKey;

    public Gate (){

    }

    public Gate(String airportKey, String gateID, String label, double latitude, double longitude) {
        this.airportKey = airportKey;
        this.gateID = gateID;
        this.label = label;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }

    public String getGateID() {
        return gateID;
    }

    public void setGateID(String gateID) {
        this.gateID = gateID;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getAirportKey() {
        return airportKey;
    }

    public void setAirportKey(String airportKey) {
        this.airportKey = airportKey;
    }
}
