package com.android.sheguard.util;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioRecorder {

    private static final String TAG = "AudioRecorder";
    private MediaRecorder mediaRecorder;
    private boolean isRecording = false;

    public AudioRecorder() {
    }

    public boolean startRecording(String filePath) {
        if (isRecording) {
            Log.w(TAG, "Recording already in progress");
            return false;
        }

        mediaRecorder = new MediaRecorder();
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(filePath);
            
            // Set higher quality if possible, but keep standard for reliability
            mediaRecorder.setAudioEncodingBitRate(128000); 
            mediaRecorder.setAudioSamplingRate(44100);

            mediaRecorder.prepare();
            mediaRecorder.start();
            isRecording = true;
            Log.d(TAG, "Recording started: " + filePath);
            return true;
        } catch (IOException | IllegalStateException e) {
            Log.e(TAG, "startRecording failed: " + e.getMessage());
            e.printStackTrace();
            releaseRecorder();
            return false;
        }
    }

    public void stopRecording() {
        if (!isRecording) {
            return;
        }

        try {
            mediaRecorder.stop();
        } catch (RuntimeException e) {
            // RuntimeException is thrown if stop() is called immediately after start().
            // In this case the output file is not written is valid.
            Log.e(TAG, "stopRecording failed (too short?): " + e.getMessage());
        } finally {
            releaseRecorder();
        }
    }

    private void releaseRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
        }
        isRecording = false;
        Log.d(TAG, "Recorder released");
    }

    public boolean isRecording() {
        return isRecording;
    }
}
