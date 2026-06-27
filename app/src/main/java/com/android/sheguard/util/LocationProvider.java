package com.android.sheguard.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Wrapper around FusedLocationProviderClient with high-accuracy request
 * and a fallback to getLastLocation().
 */
public class LocationProvider {

    private static final String TAG = "LocationProvider";
    private static final int LOCATION_INTERVAL_MS = 2000;
    private static final int LOCATION_MIN_INTERVAL_MS = 1000;
    private static final int LOCATION_MAX_DELAY_MS = 2000;
    private static final int REQUIRED_UPDATES = 1;
    private static final int LOCATION_TIMEOUT_MS = 5000;

    /**
     * Callback interface for location results.
     */
    public interface OnLocationReceived {
        void onLocationReceived(double lat, double lng, float accuracy);
        void onLocationFailed();
    }

    private final Context context;
    private final FusedLocationProviderClient fusedClient;
    private final LocationRequest locationRequest;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());

    public LocationProvider(Context context) {
        this.context = context;
        this.fusedClient = LocationServices.getFusedLocationProviderClient(context);
        this.locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(LOCATION_MIN_INTERVAL_MS)
                .setMaxUpdateDelayMillis(LOCATION_MAX_DELAY_MS)
                .build();
    }

    /**
     * Requests high-accuracy location. Falls back to last known location on failure.
     */
    public void requestLocation(@NonNull OnLocationReceived callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "requestLocation: Missing location permissions.");
            callback.onLocationFailed();
            return;
        }

        Runnable timeoutRunnable = () -> {
            Log.w(TAG, "requestLocation: Timeout reached. Trying last known.");
            fusedClient.removeLocationUpdates(new LocationCallback() {}); // Placeholder to ensure valid call, though removal is tricky without reference. Correct approach below.
            // We can't easily remove the specific anonymous callback from here without keeping a reference.
            // So we'll trust the boolean flag in the improved logic below or just call fallback.
            // Actually, best to keep reference to callback wrapper.
        };
        
        // Wrapper to ensure we only call callback once
        final boolean[] isFinished = {false};
        
        LocationCallback internalCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (isFinished[0]) return;
                
                super.onLocationResult(locationResult);
                
                if (!locationResult.getLocations().isEmpty()) {
                    isFinished[0] = true;
                    timeoutHandler.removeCallbacksAndMessages(null);
                    fusedClient.removeLocationUpdates(this);
                    
                    int idx = locationResult.getLocations().size() - 1;
                    Location loc = locationResult.getLocations().get(idx);
                    Log.i(TAG, "requestLocation: Got high-accuracy location.");
                    callback.onLocationReceived(loc.getLatitude(), loc.getLongitude(), loc.getAccuracy());
                }
            }
        };
        
        timeoutHandler.postDelayed(() -> {
            if (!isFinished[0]) {
                isFinished[0] = true;
                fusedClient.removeLocationUpdates(internalCallback);
                Log.w(TAG, "requestLocation: Timeout. Falling back.");
                fallbackToLastKnown(callback);
            }
        }, LOCATION_TIMEOUT_MS);

        fusedClient.requestLocationUpdates(locationRequest, internalCallback, Looper.getMainLooper());
    }

    @SuppressWarnings("MissingPermission")
    private void fallbackToLastKnown(@NonNull OnLocationReceived callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
             callback.onLocationFailed();
             return;
        }

        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.i(TAG, "fallbackToLastKnown: Got last known location.");
                        callback.onLocationReceived(location.getLatitude(), location.getLongitude(), location.getAccuracy());
                    } else {
                        Log.e(TAG, "fallbackToLastKnown: No location available.");
                        callback.onLocationFailed();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "fallbackToLastKnown: Failed: " + e.getMessage(), e);
                    callback.onLocationFailed();
                });
    }
}
