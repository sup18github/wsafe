package com.android.sheguard.service;

import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

public class WatchMessageListenerService extends WearableListenerService {
    private static final String TAG = "WatchListenerService";
    private static final String SOS_ALERT_PATH = "/sheguard/sos";

    @Override
    public void onMessageReceived(@NonNull MessageEvent messageEvent) {
        if (messageEvent.getPath().equalsIgnoreCase(SOS_ALERT_PATH)) {
            Log.i(TAG, "Watch SOS Trigger received via Wear API!");
            
            Intent internalIntent = new Intent(this, SosService.class);
            internalIntent.setAction(SosService.ACTION_TRIGGER_VOICE_SOS);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                startForegroundService(internalIntent);
            } else {
                startService(internalIntent);
            }
        } else {
            super.onMessageReceived(messageEvent);
        }
    }
}
