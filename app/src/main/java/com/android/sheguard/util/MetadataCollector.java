package com.android.sheguard.util;

import android.content.Context;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

public class MetadataCollector {

    private final Context context;
    private final LocationProvider locationProvider;

    public MetadataCollector(Context context) {
        this.context = context;
        this.locationProvider = new LocationProvider(context);
    }

    public Map<String, Object> collectMetadata(String sosId) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("sosId", sosId);
        metadata.put("timestamp", System.currentTimeMillis());
        metadata.put("deviceModel", Build.MODEL);
        metadata.put("androidVersion", Build.VERSION.RELEASE);
        metadata.put("batteryLevel", getBatteryLevel());
        
        // Note connection to location provider is async, so this might be null or cached
        // For distinct location, we rely on callbacks, but for metadata snapshot we take what's available or provided
        return metadata;
    }

    private int getBatteryLevel() {
        BatteryManager bm = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        if (bm != null) {
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        }
        return -1;
    }
    
    public void getCurrentLocation(LocationProvider.OnLocationReceived listener) {
        locationProvider.requestLocation(listener);
    }
}
