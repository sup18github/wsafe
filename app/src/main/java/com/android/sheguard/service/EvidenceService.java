package com.android.sheguard.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.android.sheguard.R;
import com.android.sheguard.repository.EvidenceRepository;
import com.android.sheguard.util.AudioRecorder;
import com.android.sheguard.util.LocalStorageManager;
import com.android.sheguard.util.LocationProvider;
import com.android.sheguard.util.MetadataCollector;
import com.android.sheguard.util.UploadManager;
import com.android.sheguard.util.VideoRecorder;

import java.io.File;

public class EvidenceService extends Service {

    private static final String TAG = "EvidenceService";
    private static final String CHANNEL_ID = "EvidenceChannel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long RECORDING_DURATION_MS = 120000; // 2 minutes

    public static final String ACTION_START = "ACTION_START";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String EXTRA_SOS_ID = "EXTRA_SOS_ID";
    public static final String EXTRA_LAT = "EXTRA_LAT";
    public static final String EXTRA_LNG = "EXTRA_LNG";

    private AudioRecorder audioRecorder;
    private VideoRecorder videoRecorder;
    private EvidenceRepository evidenceRepository;
    private LocalStorageManager localStorageManager;
    private MetadataCollector metadataCollector;
    private UploadManager uploadManager;
    private Handler stopHandler;
    
    private String currentSosId;
    private File currentAudioFile;
    private File currentVideoFile;
    private double initialLat;
    private double initialLng;

    @Override
    public void onCreate() {
        super.onCreate();
        audioRecorder = new AudioRecorder();
        videoRecorder = new VideoRecorder();
        evidenceRepository = new EvidenceRepository();
        localStorageManager = new LocalStorageManager(this);
        metadataCollector = new MetadataCollector(this);
        uploadManager = new UploadManager(this);
        stopHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equals(action)) {
                currentSosId = intent.getStringExtra(EXTRA_SOS_ID);
                initialLat = intent.getDoubleExtra(EXTRA_LAT, 0);
                initialLng = intent.getDoubleExtra(EXTRA_LNG, 0);
                startRecording();
            } else if (ACTION_STOP.equals(action)) {
                stopRecording();
            }
        }
        return START_STICKY;
    }

    private void startRecording() {
        if (audioRecorder.isRecording() || videoRecorder.isRecording()) {
            return;
        }

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Recording evidence for your safety...")
                .setSmallIcon(R.drawable.ic_launcher_notification)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ requires explicit foreground service type flags
            int foregroundType = ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE;
            if (hasCameraPermission()) {
                foregroundType |= ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA;
            }
            startForeground(NOTIFICATION_ID, notification, foregroundType);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        // Start audio recording
        currentAudioFile = localStorageManager.createEvidenceFile(currentSosId);
        if (currentAudioFile != null) {
            boolean audioStarted = audioRecorder.startRecording(currentAudioFile.getAbsolutePath());
            if (audioStarted) {
                Log.i(TAG, "Audio recording started: " + currentAudioFile.getName());
            } else {
                Log.e(TAG, "Failed to start audio recorder");
            }
        }

        // Start video recording (only if camera permission is granted)
        if (hasCameraPermission()) {
            currentVideoFile = localStorageManager.createVideoEvidenceFile(currentSosId);
            if (currentVideoFile != null) {
                boolean videoStarted = videoRecorder.startRecording(this, currentVideoFile.getAbsolutePath());
                if (videoStarted) {
                    Log.i(TAG, "Video recording started: " + currentVideoFile.getName());
                } else {
                    Log.e(TAG, "Failed to start video recorder");
                    currentVideoFile = null; // Don't try to upload a non-existent file
                }
            }
        } else {
            Log.w(TAG, "Camera permission not granted, skipping video recording");
        }

        // At least one recorder should have started
        if (!audioRecorder.isRecording() && !videoRecorder.isRecording()) {
            Log.e(TAG, "Both recorders failed to start");
            stopSelf();
            return;
        }

        // Schedule auto-stop after duration
        stopHandler.postDelayed(this::stopRecording, RECORDING_DURATION_MS);
    }

    private void stopRecording() {
        boolean hadAudio = audioRecorder.isRecording();
        boolean hadVideo = videoRecorder.isRecording();

        // Stop audio
        if (hadAudio) {
            audioRecorder.stopRecording();
            Log.i(TAG, "Audio recording stopped");
        }

        // Stop video
        if (hadVideo) {
            videoRecorder.stopRecording();
            Log.i(TAG, "Video recording stopped");
        }

        if (!hadAudio && !hadVideo) {
            // Nothing was recording
            stopForeground(true);
            stopSelf();
            return;
        }

        // Upload evidence with location
        metadataCollector.getCurrentLocation(new LocationProvider.OnLocationReceived() {
            @Override
            public void onLocationReceived(double lat, double lng, float accuracy) {
                uploadAllEvidence(lat, lng);
            }

            @Override
            public void onLocationFailed() {
                Log.w(TAG, "Failed to get fresh location, using initial.");
                uploadAllEvidence(initialLat, initialLng);
            }
        });
    }

    private void uploadAllEvidence(double lat, double lng) {
        long timestamp = System.currentTimeMillis();

        // Upload audio evidence
        if (currentAudioFile != null && currentAudioFile.exists() && currentAudioFile.length() > 0) {
            uploadManager.uploadEvidence(currentAudioFile, currentSosId, timestamp, lat, lng, "audio");
            Log.i(TAG, "Audio evidence queued for upload: " + currentAudioFile.getName());
        }

        // Upload video evidence
        if (currentVideoFile != null && currentVideoFile.exists() && currentVideoFile.length() > 0) {
            uploadManager.uploadEvidence(currentVideoFile, currentSosId, timestamp, lat, lng, "video");
            Log.i(TAG, "Video evidence queued for upload: " + currentVideoFile.getName());
        }

        stopForeground(true);
        stopSelf();
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Evidence Recording Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Ensure recorders are stopped if service is killed
        if (audioRecorder.isRecording()) {
            audioRecorder.stopRecording();
        }
        if (videoRecorder.isRecording()) {
            videoRecorder.stopRecording();
        }
        stopHandler.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
