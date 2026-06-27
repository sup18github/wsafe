package com.android.sheguard.util;

import android.util.Log;

import com.android.sheguard.common.Constants;
import com.android.sheguard.model.SosEventModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Utility class that writes SOS events to Firebase Realtime Database.
 * Path: sos_alerts/{userId}/{eventId}
 */
public class FirebaseLogger {

    private static final String TAG = "FirebaseLogger";

    /**
     * Logs an SOS event to Firebase Realtime Database.
     *
     * @param event The SOS event model containing location, trigger type, and SMS status.
     */
    public static void logSosEvent(SosEventModel event) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.w(TAG, "logSosEvent: No authenticated user, skipping Firebase log.");
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference(Constants.FIREBASE_SOS_ALERTS)
                .child(user.getUid());

        String eventId = ref.push().getKey();
        if (eventId == null) {
            Log.e(TAG, "logSosEvent: Failed to generate event ID.");
            return;
        }

        ref.child(eventId).setValue(event)
                .addOnSuccessListener(aVoid -> Log.i(TAG, "logSosEvent: Event logged successfully: " + eventId))
                .addOnFailureListener(e -> Log.e(TAG, "logSosEvent: Failed to log event: " + e.getMessage(), e));
    }
}
