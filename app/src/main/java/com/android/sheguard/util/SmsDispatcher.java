package com.android.sheguard.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.android.sheguard.model.ContactModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles multi-contact SMS sending with per-contact success/failure tracking.
 * Supports personalized messages via the {NAME} placeholder and multipart SMS
 * for messages that exceed 160 characters.
 */
public class SmsDispatcher {

    private static final String TAG = "SmsDispatcher";
    public static final String PLACEHOLDER_NAME = "{NAME}";

    /**
     * Sends a personalized SMS message to all provided contacts.
     * The message template may contain {NAME} which is replaced with each contact's name.
     * Uses sendMultipartTextMessage to handle long messages reliably.
     *
     * @param context         Application context.
     * @param contacts        List of trusted contacts to send to.
     * @param messageTemplate The emergency message template (may contain {NAME}).
     * @return Map of phone number → success(true)/failure(false).
     */
    public static Map<String, Boolean> sendToAll(Context context, ArrayList<ContactModel> contacts, String messageTemplate) {
        Map<String, Boolean> result = new HashMap<>();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "sendToAll: SEND_SMS permission not granted.");
            for (ContactModel contact : contacts) {
                result.put(contact.getPhone(), false);
            }
            return result;
        }

        SmsManager smsManager;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            smsManager = context.getSystemService(SmsManager.class);
        } else {
            smsManager = SmsManager.getDefault();
        }

        for (ContactModel contact : contacts) {
            try {
                // Personalize the message for this contact
                String personalizedMessage = messageTemplate.replace(PLACEHOLDER_NAME, contact.getName());

                // Use multipart to handle long messages (map links, emojis, etc.)
                ArrayList<String> parts = smsManager.divideMessage(personalizedMessage);
                if (parts.size() > 1) {
                    smsManager.sendMultipartTextMessage(contact.getPhone(), null, parts, null, null);
                } else {
                    smsManager.sendTextMessage(contact.getPhone(), null, personalizedMessage, null, null);
                }

                result.put(contact.getPhone(), true);
                Log.i(TAG, "sendToAll: SMS sent to " + contact.getName() + " (" + parts.size() + " part(s))");
            } catch (Exception e) {
                result.put(contact.getPhone(), false);
                Log.e(TAG, "sendToAll: Failed to send SMS to " + contact.getName() + ": " + e.getMessage(), e);
            }
        }

        return result;
    }
}

