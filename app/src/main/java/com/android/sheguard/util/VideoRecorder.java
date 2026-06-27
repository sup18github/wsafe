package com.android.sheguard.util;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Arrays;

/**
 * Records video from the rear camera without any preview surface.
 * Used during SOS events to capture evidence silently in the background.
 */
public class VideoRecorder {

    private static final String TAG = "VideoRecorder";

    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private MediaRecorder mediaRecorder;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private boolean isRecording = false;
    private String outputFilePath;

    public VideoRecorder() {
    }

    /**
     * Start video recording from the rear camera.
     *
     * @param context  Application context
     * @param filePath Full path to the output .mp4 file
     * @return true if recording started successfully, false otherwise
     */
    public boolean startRecording(Context context, String filePath) {
        if (isRecording) {
            Log.w(TAG, "Recording already in progress");
            return false;
        }

        outputFilePath = filePath;

        try {
            startBackgroundThread();
            setupMediaRecorder(filePath);

            CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = getRearCameraId(manager);
            if (cameraId == null) {
                Log.e(TAG, "No rear camera found");
                releaseRecorder();
                return false;
            }

            try {
                manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                    @Override
                    public void onOpened(@NonNull CameraDevice camera) {
                        cameraDevice = camera;
                        startCaptureSession();
                    }

                    @Override
                    public void onDisconnected(@NonNull CameraDevice camera) {
                        Log.w(TAG, "Camera disconnected");
                        camera.close();
                        cameraDevice = null;
                    }

                    @Override
                    public void onError(@NonNull CameraDevice camera, int error) {
                        Log.e(TAG, "Camera error: " + error);
                        camera.close();
                        cameraDevice = null;
                    }
                }, backgroundHandler);
            } catch (SecurityException e) {
                Log.e(TAG, "Camera permission not granted", e);
                releaseRecorder();
                return false;
            }

            isRecording = true;
            Log.d(TAG, "Video recording initiated: " + filePath);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "startRecording failed", e);
            releaseRecorder();
            return false;
        }
    }

    private void setupMediaRecorder(String filePath) throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setOutputFile(filePath);

        // Low quality to save storage and bandwidth
        mediaRecorder.setVideoEncodingBitRate(1000000); // 1 Mbps
        mediaRecorder.setVideoFrameRate(15);
        mediaRecorder.setVideoSize(640, 480);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setAudioEncodingBitRate(64000);
        mediaRecorder.setAudioSamplingRate(22050);

        mediaRecorder.prepare();
    }

    private void startCaptureSession() {
        if (cameraDevice == null || mediaRecorder == null) {
            Log.e(TAG, "Camera or MediaRecorder not ready");
            return;
        }

        try {
            Surface recorderSurface = mediaRecorder.getSurface();

            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(recorderSurface);

            cameraDevice.createCaptureSession(
                    Arrays.asList(recorderSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                                session.setRepeatingRequest(builder.build(), null, backgroundHandler);
                                mediaRecorder.start();
                                Log.i(TAG, "Video recording started");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to start capture session", e);
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e(TAG, "Capture session configuration failed");
                        }
                    },
                    backgroundHandler
            );

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera access exception during session creation", e);
        }
    }

    /**
     * Stop video recording and release all resources.
     */
    public void stopRecording() {
        if (!isRecording) {
            return;
        }

        try {
            if (captureSession != null) {
                captureSession.stopRepeating();
                captureSession.abortCaptures();
                captureSession.close();
                captureSession = null;
            }
        } catch (Exception e) {
            Log.w(TAG, "Error stopping capture session", e);
        }

        try {
            if (mediaRecorder != null) {
                mediaRecorder.stop();
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "stopRecording failed (too short?): " + e.getMessage());
        }

        releaseRecorder();
        stopBackgroundThread();

        Log.d(TAG, "Video recording stopped");
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            try {
                mediaRecorder.reset();
                mediaRecorder.release();
            } catch (Exception ignored) {
            }
            mediaRecorder = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        isRecording = false;
        Log.d(TAG, "Recorder released");
    }

    private String getRearCameraId(CameraManager manager) throws CameraAccessException {
        for (String id : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("VideoRecorderThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (backgroundThread != null) {
            backgroundThread.quitSafely();
            try {
                backgroundThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, "Background thread interrupted", e);
            }
            backgroundThread = null;
            backgroundHandler = null;
        }
    }

    public boolean isRecording() {
        return isRecording;
    }
}
