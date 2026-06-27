package com.android.sheguard.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.android.sheguard.R;
import com.android.sheguard.ui.activity.FakeCallActivity;

public class FakeCallReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "fake_call_channel";
    private static final int NOTIFICATION_ID = 999;

    @Override
    public void onReceive(Context context, Intent intent) {
        String name = intent.getStringExtra("CALLER_NAME");
        String number = intent.getStringExtra("CALLER_NUMBER");

        Intent fullScreenIntent = new Intent(context, FakeCallActivity.class);
        fullScreenIntent.putExtra("CALLER_NAME", name);
        fullScreenIntent.putExtra("CALLER_NUMBER", number);
        fullScreenIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                fullScreenIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, 
                    "Incoming Fake Call", 
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            // We want it to be silent/vibrate or use our custom ringtone in activity?
            // Usually fullScreenIntent just launches the activity if conditions are met.
            // If not met, it shows headsup.
            channel.setSound(null, null); 
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher) // Or a phone icon
                .setContentTitle("Incoming Call")
                .setContentText(name)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
        
        // For older androids or sometimes as fallback, we might start activity directly if mostly allowed?
        // But Android 10+ generally blocks background starts.
        // The Notification with fullScreenIntent is the correct way.
    }
}
