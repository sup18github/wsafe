package com.android.sheguard.model;

import com.google.firebase.Timestamp;

@SuppressWarnings("unused")
public class EvidenceModel {

    private String id;
    private String sosEventId;
    private String userId;
    private long timestamp;
    private double latitude;
    private double longitude;
    private String downloadUrl;
    private String storagePath;
    private String type;
    private String fileName;
    private long fileSize;
    private int recordingDuration;
    private boolean isUploaded;

    public EvidenceModel() {
    }

    public EvidenceModel(String sosEventId, String userId, long timestamp, double latitude, double longitude) {
        this.sosEventId = sosEventId;
        this.userId = userId;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = "audio";
        this.isUploaded = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSosEventId() {
        return sosEventId;
    }

    public void setSosEventId(String sosEventId) {
        this.sosEventId = sosEventId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getRecordingDuration() {
        return recordingDuration;
    }

    public void setRecordingDuration(int recordingDuration) {
        this.recordingDuration = recordingDuration;
    }

    public boolean isUploaded() {
        return isUploaded;
    }

    public void setUploaded(boolean uploaded) {
        isUploaded = uploaded;
    }

    public String getFormattedDate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    public String getFormattedDuration() {
        int seconds = recordingDuration / 1000;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public String getLocationString() {
        if (latitude != 0 && longitude != 0) {
            return String.format(java.util.Locale.getDefault(), "%.4f, %.4f", latitude, longitude);
        }
        return "Location unavailable";
    }
}