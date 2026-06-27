package com.android.sheguard.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.sheguard.R;
import com.android.sheguard.SheGuard;
import com.android.sheguard.api.NotificationAPI;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.model.ContactModel;
import com.android.sheguard.model.SosEventModel;
import com.android.sheguard.ui.activity.MainActivity;
import com.android.sheguard.util.FirebaseLogger;
import com.android.sheguard.util.FirebaseUtil;
import com.android.sheguard.util.LocationProvider;
import com.android.sheguard.util.NotificationClient;
import com.android.sheguard.util.SmsDispatcher;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("FieldCanBeLocal")
public class SosService extends Service implements SensorEventListener {

    public static final String ACTION_SOS_STATUS = "com.android.sheguard.SOS_STATUS";
    public static final String ACTION_TRIGGER_VOICE_SOS = "com.android.sheguard.TRIGGER_VOICE_SOS";
    public static final String EXTRA_SOS_STATE = "sos_state";
    public static final String STATE_ACTIVATING = "activating";
    public static final String STATE_COMPLETED = "completed";
    public static final String STATE_FAILED = "failed";
    public static final String EXTRA_CONTACTS_NOTIFIED = "contacts_notified";

    private long lastShakeTime = 0;
    private boolean sentSMS = false;
    public static boolean isRunning = false;
    private boolean calledEmergency = false;
    private boolean sentNotification = false;
    private AudioManager audioManager = null;
    private final Float shakeThreshold = 10.2f;
    private SensorManager sensorManager = null;
    private LocationManager locationManager = null;
    private static final int MIN_TIME_BETWEEN_SHAKES = 1000;
    private static NotificationAPI notificationApiService = null;
    private static final MediaPlayer mediaPlayer = new MediaPlayer();

    private LocationProvider locationProvider = null;

    private static final int SHAKE_COUNT_THRESHOLD = 2;
    private static final long SHAKE_WINDOW_MS = 2500; // 2.5 seconds window for 2 shakes
    private static final long SHAKE_RESET_MS = 1500; // Reset if > 1.5s between shakes
    private static final long COOLDOWN_MS = 30000; // 30 seconds cooldown
    private int shakeCount = 0;
    private long firstShakeTime = 0;
    private long lastTriggerTime = 0;
    
    // removed duplicate sensorManager
    private android.os.PowerManager.WakeLock wakeLock;

    // Power button panic feature
    private BroadcastReceiver powerButtonReceiver;
    private int powerButtonPressCount = 0;
    private long lastPowerButtonPressTime = 0;
    private static final long POWER_BUTTON_WINDOW_MS = 3000; // 3 seconds for 3 presses

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (notificationApiService == null) {
            notificationApiService = NotificationClient.getClient("https://fcm.googleapis.com/").create(NotificationAPI.class);
        }

        // Initialize power button receiver
        powerButtonReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction()) || Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastPowerButtonPressTime > POWER_BUTTON_WINDOW_MS) {
                        // Reset if too much time has passed
                        powerButtonPressCount = 1;
                    } else {
                        powerButtonPressCount++;
                        if (powerButtonPressCount >= 3) {
                            // Cooldown check
                            if (System.currentTimeMillis() - lastTriggerTime > COOLDOWN_MS) {
                                lastTriggerTime = System.currentTimeMillis();
                                Log.i("SosService", "Power Button Panic Triggered! (3 presses)");
                                activateSosMode("power_button");
                            }
                            powerButtonPressCount = 0; // reset
                        }
                    }
                    lastPowerButtonPressTime = currentTime;
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(powerButtonReceiver, filter);

        locationProvider = new LocationProvider(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        android.os.PowerManager powerManager = (android.os.PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "SheGuard:SosServiceWakeLock");
        }

        if (sensorManager != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equalsIgnoreCase("STOP")) {
                if (isRunning) {
                    this.stopForeground(true);
                    this.stopSelf();

                    stopSiren();
                    resetValues();
                    if (wakeLock != null && wakeLock.isHeld()) {
                        wakeLock.release();
                    }
                    Log.i("SosService", "Service Stopped");
                }
            } else if (intent.getAction().equalsIgnoreCase(ACTION_TRIGGER_VOICE_SOS)) {
                if (!isRunning) {
                    startForegroundNotification();
                    isRunning = true;
                    Log.i("SosService", "Service Started via Voice Trigger");
                }
                activateSosMode("voice_watch");
                return START_STICKY;
            } else {
                startForegroundNotification();
                
                if (wakeLock != null && !wakeLock.isHeld()) {
                    wakeLock.acquire();
                }

                isRunning = true;
                Log.i("SosService", "Service Started");
                return START_STICKY;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }
    
    private void startForegroundNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        NotificationChannel channel = new NotificationChannel(getString(R.string.notification_channel_emergency), getString(R.string.notification_channel_emergency), NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription(getString(R.string.notification_channel_emergency_desc));
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, getString(R.string.notification_channel_emergency))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_emergency_mode, getString(R.string.app_name)))
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();

        this.startForeground(1, notification);
        notificationManager.notify(1, notification);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            
            // COOLDOWN CHECK
            if (System.currentTimeMillis() - lastTriggerTime < COOLDOWN_MS) {
                return;
            }

            float x = event.values[0];
            float y = event.values[1]; // Vertical axis when phone is upright
            float z = event.values[2];

            // Vertical Shake Logic (Strict):
            // 1. Strong movement in Y (Up) > 18m/s^2 (approx 1.8G)
            // 2. Weak movement in X and Z (< 8m/s^2) to reject horizontal shakes/walking
            
            boolean isVerticalShake = (y > 18.0f) && (Math.abs(x) < 8.0f) && (Math.abs(z) < 8.0f);

            if (isVerticalShake) {
                long curTime = System.currentTimeMillis();
                
                // If it's been too long since last shake, reset count
                if (curTime - lastShakeTime > SHAKE_RESET_MS) {
                    shakeCount = 0;
                    firstShakeTime = curTime;
                }
                
                // Debounce: ignore peaks too close together (500ms to ensure distinct movements)
                if (curTime - lastShakeTime > 500) { 
                    lastShakeTime = curTime;
                    shakeCount++;
                    
                    Log.d("SosService", "Vertical Shake detected: " + shakeCount);
                    
                    // Tactile feedback for each shake
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (v != null && v.hasVibrator()) {
                         v.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                    }

                    if (shakeCount >= SHAKE_COUNT_THRESHOLD) {
                        // Check if all shakes happened within the window
                        if (curTime - firstShakeTime < SHAKE_WINDOW_MS) {
                             lastTriggerTime = System.currentTimeMillis(); // Set cooldown
                             deviceShaken();
                             Log.i("SosService", "Device Shaken Triggered (Vertical Count: " + shakeCount + ")");
                        }
                        shakeCount = 0; 
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // do nothing
    }

    private void deviceShaken() {
        if (!Prefs.getBoolean(Constants.SETTINGS_SHAKE_DETECTION, false)) {
            stopSiren();
            Log.i("SosService", "Stopped Siren");
            return;
        }

        activateSosMode(Constants.TRIGGER_SHAKE);
    }

    /**
     * Main SOS orchestration method. Uses modular helpers for each step.
     *
     * @param triggerType "manual" or "shake"
     */
    public void activateSosMode(String triggerType) {
        long sosTimestamp = System.currentTimeMillis();
        String sosId = String.valueOf(sosTimestamp);

        // Broadcast: SOS activating
        broadcastSosStatus(STATE_ACTIVATING, 0);

        // Vibrate feedback
        vibrateDevice();

        // Start Evidence Collection Immediately (if enabled in settings)
        if (Prefs.getBoolean(Constants.SETTINGS_RECORD_EVIDENCE, true)) {
            Intent evidenceIntent = new Intent(this, EvidenceService.class);
            evidenceIntent.setAction(EvidenceService.ACTION_START);
            evidenceIntent.putExtra(EvidenceService.EXTRA_SOS_ID, sosId);
            evidenceIntent.putExtra(EvidenceService.EXTRA_LAT, 0.0);
            evidenceIntent.putExtra(EvidenceService.EXTRA_LNG, 0.0);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(evidenceIntent);
            } else {
                startService(evidenceIntent);
            }
        } else {
            Log.i("SosService", "Evidence recording disabled by user settings");
        }

        ArrayList<ContactModel> contacts = new ArrayList<>();
        Gson gson = SheGuard.GSON;
        String jsonContacts = Prefs.getString(Constants.CONTACTS_LIST, "");

        if (Prefs.getBoolean(Constants.SETTINGS_CALL_EMERGENCY_SERVICE, false) && !calledEmergency) {
            callEmergency();
            calledEmergency = true;
        }

        if (!jsonContacts.isEmpty()) {
            Type type = new TypeToken<List<ContactModel>>() {
            }.getType();
            contacts.addAll(gson.fromJson(jsonContacts, type));
        }

        if (Prefs.getBoolean(Constants.SETTINGS_PLAY_SIREN, false)) {
            playSiren();
            Log.i("SosService", "Playing Siren");
        } else {
            stopSiren();
            Log.i("SosService", "Stopped Siren");
        }

        if (!contacts.isEmpty()) {
            locationProvider.requestLocation(new LocationProvider.OnLocationReceived() {
                @Override
                public void onLocationReceived(double lat, double lng, float accuracy) {
                    sendSosMessage(contacts, lat, lng, accuracy, triggerType, sosTimestamp);
                }

                @Override
                public void onLocationFailed() {
                    Log.e("SosService", "Failed to get location for SOS. Sending without location.");
                    sendSosMessage(contacts, 0, 0, 0, triggerType, sosTimestamp);
                }
            });
        } else {
            Log.w("SosService", "No trusted contacts configured.");
            broadcastSosStatus(STATE_FAILED, 0);
        }
    }

    private void sendSosMessage(ArrayList<ContactModel> contacts, double lat, double lng, float accuracy, String triggerType, long timestamp) {
        String mapsLink = (lat != 0 && lng != 0) ? "https://maps.google.com/maps?q=loc:" + lat + "," + lng : "Location unavailable";
        
        // Build emergency message with personalization placeholder
        String message = "Hey " + SmsDispatcher.PLACEHOLDER_NAME + ",\n" +
                "I'm in Danger!\n" +
                "My location:\n" + mapsLink;

        // Send SMS via SmsDispatcher
        int contactsNotified = 0;
        if (Prefs.getBoolean(Constants.SETTINGS_SEND_SMS, true) && !sentSMS) {
            Map<String, Boolean> smsResult = SmsDispatcher.sendToAll(SosService.this, contacts, message);
            sentSMS = true;

            for (Boolean sent : smsResult.values()) {
                if (sent) contactsNotified++;
            }

            // Log to Firebase
            SosEventModel event = new SosEventModel(lat, lng, accuracy, timestamp, triggerType);
            event.setSmsStatus(smsResult);
            FirebaseLogger.logSosEvent(event);
        }

        // Send push notifications to contacts who are also users
        if (Prefs.getBoolean(Constants.SETTINGS_SEND_NOTIFICATION, true) && !sentNotification) {
            sendNotification(contacts, mapsLink);
            sentNotification = true;
        }

        // Broadcast: SOS completed
        broadcastSosStatus(STATE_COMPLETED, contactsNotified);
    }

    private void broadcastSosStatus(String state, int contactsNotified) {
        Intent intent = new Intent(ACTION_SOS_STATUS);
        intent.putExtra(EXTRA_SOS_STATE, state);
        intent.putExtra(EXTRA_CONTACTS_NOTIFIED, contactsNotified);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void vibrateDevice() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    private void sendNotification(ArrayList<ContactModel> contacts, String mapsLink) {
        for (ContactModel contact : contacts) {
            FirebaseFirestore.getInstance()
                    .collection(Constants.FIRESTORE_COLLECTION_PHONE2UID)
                    .document(contact.getPhone())
                    .get()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document1 = task1.getResult();

                            if (document1.exists() && document1.getString("uid") != null) {
                                Log.i("SosService", "Notification: uid found");

                                FirebaseFirestore.getInstance()
                                        .collection(Constants.FIRESTORE_COLLECTION_TOKENS)
                                        .document(Objects.requireNonNull(document1.getString("uid")))
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                DocumentSnapshot document2 = task2.getResult();

                                                if (document2.exists() && document2.getString("token") != null) {
                                                    Log.i("SosService", "Notification: token found");
                                                    sendPushNotification(document2.getString("token"), Prefs.getString(Constants.PREFS_USER_NAME, getString(R.string.app_name)), getString(R.string.sos_notification, mapsLink));
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    @SuppressWarnings("deprecation")
    private static void sendPushNotification(String userToken, String title, String message) {
        new FirebaseUtil.SendNotificationTask(notificationApiService, userToken, title, message).execute();
    }

    private void callEmergency() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Constants.EMERGENCY_NUMBER));
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(callIntent);
        Log.i("SosService", "Call: called emergency");
    }

    private void playSiren() {
        if (mediaPlayer.isPlaying()) {
            return;
        }

        try {
            AssetFileDescriptor afd = getAssets().openFd("police-operation-siren.mp3");
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e("SOS", "playSiren error: " + e.getMessage(), e);
        }
    }

    public static void stopSiren() {
        try {
            mediaPlayer.stop();
            mediaPlayer.reset();
        } catch (Exception ignored) {
        }
    }

    private void resetValues() {
        isRunning = false;
        sentSMS = false;
        sentNotification = false;
        calledEmergency = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (powerButtonReceiver != null) {
            try {
                unregisterReceiver(powerButtonReceiver);
            } catch (Exception ignored) {}
        }
        resetValues();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopForeground(true);
        stopSelf();
        stopSiren();
        resetValues();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
        if (powerButtonReceiver != null) {
            try {
                unregisterReceiver(powerButtonReceiver);
            } catch (Exception ignored) {}
        }
        Log.i("SosService", "Service Stopped via task removed");
    }
}