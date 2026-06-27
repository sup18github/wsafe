package com.android.sheguard.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Data model for an SOS alert event, stored in Firebase.
 * Path: sos_alerts/{userId}/{eventId}
 */
@SuppressWarnings("unused")
public class SosEventModel {

    private double lat;
    private double lng;
    private float accuracy;
    private long time;
    private String triggerType; // "manual" or "shake"
    private Map<String, Boolean> smsStatus; // phone -> sent(true)/failed(false)

    public SosEventModel() {
        // Required empty constructor for Firebase
    }

    public SosEventModel(double lat, double lng, float accuracy, long time, String triggerType) {
        this.lat = lat;
        this.lng = lng;
        this.accuracy = accuracy;
        this.time = time;
        this.triggerType = triggerType;
        this.smsStatus = new HashMap<>();
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public Map<String, Boolean> getSmsStatus() {
        return smsStatus;
    }

    public void setSmsStatus(Map<String, Boolean> smsStatus) {
        this.smsStatus = smsStatus;
    }

    public void addSmsStatus(String phone, boolean sent) {
        if (this.smsStatus == null) {
            this.smsStatus = new HashMap<>();
        }
        this.smsStatus.put(phone, sent);
    }
}
