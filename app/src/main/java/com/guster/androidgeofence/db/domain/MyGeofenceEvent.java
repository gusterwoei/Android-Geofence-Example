package com.guster.androidgeofence.db.domain;

import com.guster.skydb.annotation.Column;
import com.guster.skydb.annotation.Table;

/**
 * Created by Gusterwoei on 9/17/15.
 *
 */
@Table(name = "my_geo_event")
public class MyGeofenceEvent {
    @Column(name = "id")
    private String id;
    @Column(name = "lat")
    private double latitude;
    @Column(name = "lng")
    private double longitude;
    @Column(name = "radius")
    private float radius;
    @Column(name = "event")
    private String event;
    @Column(name = "date")
    private long date;

    private String placeName;

    public MyGeofenceEvent() {}

    public MyGeofenceEvent(MyGeofence myGeofence) {
        setId(myGeofence.getId());
        setLatitude(myGeofence.getLatitude());
        setLongitude(myGeofence.getLongitude());
        setRadius(myGeofence.getRadius());
        setDate(System.currentTimeMillis());
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

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }
}
