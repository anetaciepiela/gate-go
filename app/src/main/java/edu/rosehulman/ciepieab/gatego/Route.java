package edu.rosehulman.ciepieab.gatego;

import java.util.List;

/**
 * Created by reesemm on 4/23/2018.
 */

public class Route {

    private String routeID;
    private String routeName;
    private String startGateID;
    private String destGateID;
    private List<Double> latPolyPoint;
    private List<Double> longPolyPoint;
    private String key;

    public Route(String routeName, String routeID, String startGateID, String destGateID, List<Double> latPolyPoint, List<Double> longPolyPoint) {
        this.routeName = routeName;
        this.routeID = routeID;
        this.startGateID = startGateID;
        this.destGateID = destGateID;
        this.latPolyPoint = latPolyPoint;
        this.longPolyPoint = longPolyPoint;
    }

    public Route() {

    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteID() {
        return routeID;
    }

    public void setRouteID(String routeID) {
        this.routeID = routeID;
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

    @Override
    public String toString() {
        return routeName;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
