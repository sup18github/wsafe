package com.android.sheguard.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootReceiver", "Boot completed. Checking Shake Detection settings.");
            
            // Ensure Prefs are initialized (might be needed if App context isn't fully ready, though usually it is)
            // Ideally Prefs should be initialized in Application.onCreate, checking here just in case.
            
            boolean isShakeEnabled = Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false);
            if (isShakeEnabled) {
                Log.d("BootReceiver", "Shake Detection enabled, but waiting for app to open to start service.");
            }
        }
    }
}
