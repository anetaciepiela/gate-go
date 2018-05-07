package edu.rosehulman.ciepieab.gatego;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by reesemm on 4/22/2018.
 */

public class Airport {

    private LatLng coordinate;
    private String city;
    private String abbr;
    private String name;

    public Airport() {

    }


    public LatLng getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(LatLng coordinate) {
        this.coordinate = coordinate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAbbr() {
        return abbr;
    }

    public void setAbbr(String abbr) {
        this.abbr = abbr;
    }
}
