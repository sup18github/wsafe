package com.android.sheguard.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.android.sheguard.R;
import com.android.sheguard.SheGuard;
import com.android.sheguard.api.NotificationAPI;
import com.android.sheguard.common.Constants;
import com.android.sheguard.config.Prefs;
import com.android.sheguard.model.ContactModel;
import com.android.sheguard.model.SosEventModel;
import com.android.sheguard.service.SosService;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;
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

public class SosUtil {

    private static boolean sentSMS = false;
    private static boolean sentNotification = false;
    private static boolean calledEmergency = false;
    private static AudioManager audioManager = null;
    private static LocationManager locationManager = null;
    private static LocationRequest locationRequest = null;
    private static NotificationAPI notificationApiService = null;
    private static final MediaPlayer mediaPlayer = new MediaPlayer();

    static {
        if (locationRequest == null) {
            locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(2000)
                    .setMaxUpdateDelayMillis(5000)
                    .build();
        }

        if (notificationApiService == null) {
            notificationApiService = NotificationClient.getClient("https://fcm.googleapis.com/").create(NotificationAPI.class);
        }
    }

    public static boolean isGPSEnabled(Context context) {
        if (locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        Log.i("SOS", "isGPSEnabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static void turnOnGPS(Context context) {
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context)
                .checkLocationSettings(new LocationSettingsRequest.Builder()
                        .addLocationRequest(locationRequest)
                        .setAlwaysShow(true)
                        .build()
                );

        result.addOnCompleteListener(task -> {
            try {
                task.getResult(ApiException.class);
            } catch (ApiException apiException) {
                switch (apiException.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) apiException;
                            resolvableApiException.startResolutionForResult((AppCompatActivity) context, 2);
                        } catch (IntentSender.SendIntentException sendIntentException) {
                            Log.i("SOS", "turnOnGPS: " + sendIntentException.getMessage());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    public static void startSosNotificationService(Context context) {
        if (!SosService.isRunning) {
            Intent notificationIntent = new Intent(context, SosService.class);
            notificationIntent.setAction("START");

            context.startForegroundService(notificationIntent);
        }
    }

    public static void stopSosNotificationService(Context context) {
        if (SosService.isRunning) {
            Intent notificationIntent = new Intent(context, SosService.class);
            notificationIntent.setAction("STOP");

            context.startForegroundService(notificationIntent);
        }
    }

    /**
     * Activates instant SOS mode from the UI (manual trigger).
     * Uses LocationProvider, SmsDispatcher, and FirebaseLogger for modular dispatch.
     */
    public static void activateInstantSosMode(Context context) {
        if (mediaPlayer.isPlaying()) {
            stopSiren();
            resetValues();
            Log.i("SOS", "Stopping Siren");
            Log.i("SOS", "Resetting Values");
            return;
        }

        resetValues();

        // Vibrate feedback
        vibrateDevice(context);

        // Broadcast: activating
        broadcastSosStatus(context, SosService.STATE_ACTIVATING, 0);

        ArrayList<ContactModel> contacts = new ArrayList<>();
        Gson gson = SheGuard.GSON;
        String jsonContacts = Prefs.getString(Constants.CONTACTS_LIST, "");

        if (Prefs.getBoolean(Constants.SETTINGS_CALL_EMERGENCY_SERVICE, false) && !calledEmergency) {
            callEmergency(context);
            calledEmergency = true;
        }

        if (!jsonContacts.isEmpty()) {
            Type type = new TypeToken<List<ContactModel>>() {
            }.getType();
            contacts.addAll(gson.fromJson(jsonContacts, type));
        }

        if (Prefs.getBoolean(Constants.SETTINGS_PLAY_SIREN, false) && !mediaPlayer.isPlaying()) {
            playSiren(context);
            Log.i("SOS", "Playing Siren");
        } else {
            stopSiren();
            Log.i("SOS", "Stopping Siren");
        }

        // Get location, then send SMS & log
        if (!contacts.isEmpty()) {
            LocationProvider provider = new LocationProvider(context);
            provider.requestLocation(new LocationProvider.OnLocationReceived() {
                @Override
                public void onLocationReceived(double lat, double lng, float accuracy) {
                    String mapsLink = "https://maps.google.com/maps?q=loc:" + lat + "," + lng;
                    
                    String message = "Hey " + SmsDispatcher.PLACEHOLDER_NAME + ",\n" +
                            "I'm in Danger!\n" +
                            "My location:\n" + mapsLink;

                    int contactsNotified = 0;
                    if (Prefs.getBoolean(Constants.SETTINGS_SEND_SMS, true) && !sentSMS) {
                        Map<String, Boolean> smsResult = SmsDispatcher.sendToAll(context, contacts, message);
                        sentSMS = true;

                        for (Boolean sent : smsResult.values()) {
                            if (sent) contactsNotified++;
                        }

                        // Log to Firebase
                        SosEventModel event = new SosEventModel(lat, lng, accuracy, System.currentTimeMillis(), Constants.TRIGGER_MANUAL);
                        event.setSmsStatus(smsResult);
                        FirebaseLogger.logSosEvent(event);
                    }

                    if (Prefs.getBoolean(Constants.SETTINGS_SEND_NOTIFICATION, true) && !sentNotification) {
                        sendNotification(context, contacts, mapsLink);
                        sentNotification = true;
                    }

                    broadcastSosStatus(context, SosService.STATE_COMPLETED, contactsNotified);
                }

                @Override
                public void onLocationFailed() {
                    Log.e("SOS", "Failed to get location.");
                    SosEventModel event = new SosEventModel(0, 0, 0, System.currentTimeMillis(), Constants.TRIGGER_MANUAL);
                    FirebaseLogger.logSosEvent(event);
                    broadcastSosStatus(context, SosService.STATE_FAILED, 0);
                }
            });
        } else {
            Log.w("SOS", "No trusted contacts configured.");
            broadcastSosStatus(context, SosService.STATE_FAILED, 0);
        }
    }

    private static void broadcastSosStatus(Context context, String state, int contactsNotified) {
        Intent intent = new Intent(SosService.ACTION_SOS_STATUS);
        intent.putExtra(SosService.EXTRA_SOS_STATE, state);
        intent.putExtra(SosService.EXTRA_CONTACTS_NOTIFIED, contactsNotified);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private static void vibrateDevice(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        }
    }

    public static void sendNotification(Context context, ArrayList<ContactModel> contacts, String mapsLink) {
        for (ContactModel contact : contacts) {
            FirebaseFirestore.getInstance()
                    .collection(Constants.FIRESTORE_COLLECTION_PHONE2UID)
                    .document(contact.getPhone())
                    .get()
                    .addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot document1 = task1.getResult();

                            if (document1.exists() && document1.getString("uid") != null) {
                                Log.i("SOS", "sendNotification: uid found");

                                FirebaseFirestore.getInstance()
                                        .collection(Constants.FIRESTORE_COLLECTION_TOKENS)
                                        .document(Objects.requireNonNull(document1.getString("uid")))
                                        .get()
                                        .addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                DocumentSnapshot document2 = task2.getResult();

                                                if (document2.exists() && document2.getString("token") != null) {
                                                    Log.i("SOS", "sendNotification: token found");
                                                    sendNotification(document2.getString("token"), Prefs.getString(Constants.PREFS_USER_NAME, context.getString(R.string.app_name)), context.getString(R.string.sos_notification, mapsLink));
                                                }
                                            }
                                        });
                            }
                        }
                    });
        }
    }

    @SuppressWarnings("deprecation")
    public static void sendNotification(String userToken, String title, String message) {
        new FirebaseUtil.SendNotificationTask(notificationApiService, userToken, title, message).execute();
    }

    private static void callEmergency(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Constants.EMERGENCY_NUMBER));
        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(callIntent);
        Log.i("SOS", "Calling Emergency");
    }

    private static void playSiren(Context context) {
        if (mediaPlayer.isPlaying()) {
            return;
        }

        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        try {
            AssetFileDescriptor afd = context.getAssets().openFd("police-operation-siren.mp3");
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

    private static void resetValues() {
        sentSMS = false;
        sentNotification = false;
        calledEmergency = false;
    }
}
