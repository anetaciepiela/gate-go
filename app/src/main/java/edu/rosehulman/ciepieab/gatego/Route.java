package edu.rosehulman.ciepieab.gatego;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by reesemm on 4/23/2018.
 */

public class Route {

    private String routeID;
    private String date;
    private String startGateID;
    private String destGateID;
    private List<Double> latPolyPoints;
    private List<Double> longPolyPoints;

    public Route(String date, String startGateID, String destGateID, List<Double> latPolyPoints, List<Double> longPolyPoint) {
        //this.date = date;
        this.startGateID = startGateID;
        this.destGateID = destGateID;
        this.latPolyPoints = latPolyPoints;
        this.longPolyPoints = longPolyPoint;
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

    public String getStartGateID() {
        return startGateID;
    }

    public void setStartGateID(String startGateID) {
        this.startGateID = startGateID;
    }

    public String getDestGateID() {
        return destGateID;
    }

    public void setDestGateID(String destGateID) {
        this.destGateID = destGateID;
    }

    public List<Double> getLatPolyPoints() {
        return latPolyPoints;
    }

    public void setLatPolyPoints(List<Double> latPolyPoints) {
        this.latPolyPoints = latPolyPoints;
    }

    public List<Double> getLongPolyPoints() {
        return longPolyPoints;
    }

    public void setLongPolyPoints(List<Double> longPolyPoints) {
        this.longPolyPoints = longPolyPoints;
    }
}
