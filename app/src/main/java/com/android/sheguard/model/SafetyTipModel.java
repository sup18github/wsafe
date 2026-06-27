package com.android.sheguard.model;

public class SafetyTipModel {
    private String title;
    private String description;
    private Integer videoResId; // Null if it's just a tip

    public SafetyTipModel(String title, String description) {
        this.title = title;
        this.description = description;
        this.videoResId = null;
    }

    public SafetyTipModel(String title, String description, Integer videoResId) {
        this.title = title;
        this.description = description;
        this.videoResId = videoResId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Integer getVideoResId() {
        return videoResId;
    }

    public boolean isVideo() {
        return videoResId != null && videoResId != 0;
    }
}
