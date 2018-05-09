package edu.rosehulman.ciepieab.gatego;

import java.util.List;

/**
 * Created by reesemm on 4/23/2018.
 */

public class Route {

    private String routeID;
    private String date;
    private String startGateID;
    private String destGateID;
    private List<Double> latPolyPoint;
    private List<Double> longPolyPoint;

    public Route(String routeID, String date, String startGateID, String destGateID, List<Double> latPolyPoint, List<Double> longPolyPoint) {
        this.routeID = routeID;
        this.date = date;
        this.startGateID = startGateID;
        this.destGateID = destGateID;
        this.latPolyPoint = latPolyPoint;
        this.longPolyPoint = longPolyPoint;
    }

    public Route() {

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

    public List<Double> getLatPolyPoint() {
        return latPolyPoint;
    }

    public void setLatPolyPoint(List<Double> latPolyPoint) {
        this.latPolyPoint = latPolyPoint;
    }

    public List<Double> getLongPolyPoint() {
        return longPolyPoint;
    }

    public void setLongPolyPoint(List<Double> longPolyPoint) {
        this.longPolyPoint = longPolyPoint;
    }
}
