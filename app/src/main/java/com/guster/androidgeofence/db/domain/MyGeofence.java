package com.guster.androidgeofence.db.domain;

import com.guster.skydb.annotation.Column;
import com.guster.skydb.annotation.Table;

/**
 * Created by Gusterwoei on 9/17/15.
 *
 */
@Table(name = "my_geo")
public class MyGeofence {
    @Column(name = "id")
    private String id;
    @Column(name = "lat", unique = true)
    private double latitude;
    @Column(name = "lng", unique = true)
    private double longitude;
    @Column(name = "radius")
    private float radius;
    @Column(name = "title")
    private String title;

    public MyGeofence() {
    }

    public MyGeofence(String id, double latitude, double longitude, float radius) {
        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
