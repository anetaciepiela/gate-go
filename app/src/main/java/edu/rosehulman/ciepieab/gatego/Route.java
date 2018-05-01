package edu.rosehulman.ciepieab.gatego;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by reesemm on 4/23/2018.
 */

public class Route {

    private String routeID;
    private String date;
    private String expirationDate;
    private LatLng startGate;
    private LatLng destGate;
    private String airportKey;

    public Route(String date, LatLng startGate, LatLng destGate, String airportKey) {
        this.date = date;
        this.startGate = startGate;
        this.destGate = destGate;
        this.airportKey = airportKey;
    }

    public String getRouteID() {
        return routeID;
    }

    public void setRouteID(String routeID) {
        this.routeID = routeID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LatLng getStartGate() {
        return startGate;
    }

    public void setStartGate(LatLng startGate) {
        this.startGate = startGate;
    }

    public LatLng getDestGate() {
        return destGate;
    }

    public void setDestGate(LatLng destGate) {
        this.destGate = destGate;
    }

    public String getAirportKey() {
        return airportKey;
    }

    public void setAirportKey(String airportKey) {
        this.airportKey = airportKey;
    }
}
