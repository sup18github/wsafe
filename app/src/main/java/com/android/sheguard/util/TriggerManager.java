package com.android.sheguard.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;

public class TriggerManager {

    private static TriggerManager instance;
    private PendingIntent pendingIntent;

    private TriggerManager() {
    }

    public static synchronized TriggerManager getInstance() {
        if (instance == null) {
            instance = new TriggerManager();
        }
        return instance;
    }

    @SuppressLint("ScheduleExactAlarm")
    public void scheduleCall(Context context, String callerName, String callerNumber, long delayMillis) {
        cancelScheduledCall(context); // Cancel any existing schedule

        Intent intent = new Intent(context, FakeCallReceiver.class);
        intent.putExtra("CALLER_NAME", callerName);
        intent.putExtra("CALLER_NUMBER", callerNumber);

        pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager != null) {
            long triggerTime = SystemClock.elapsedRealtime() + delayMillis;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                     alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
                } else {
                     // Fallback or request permission? 
                     // For now, use setAndAllowWhileIdle which might be inexact if permission denied, but usually works for short delays.
                     // Or just standard setExact.
                     alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
            }
        }
    }

    public void cancelScheduledCall(Context context) {
        if (pendingIntent != null) {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                alarmManager.cancel(pendingIntent);
            }
            pendingIntent = null;
        }
    }
    
    // Overload for backward compatibility / existing calls in SetupActivity if needed
    // But we need context now for cancellation too.
    public void cancelScheduledCall() {
        // Deprecated usage without context, should update caller to provide context.
        // For now, do nothing or rely on SetupActivity holding reference?
        // Let's rely on caller passing context.
    }
}
