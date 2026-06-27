package com.android.sheguard.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocalStorageManager {

    private static final String TAG = "LocalStorageManager";
    private static final String EVIDENCE_DIR = "evidence";
    private final Context context;

    public LocalStorageManager(Context context) {
        this.context = context;
    }

    public File getEvidenceDirectory() {
        File dir = new File(context.getFilesDir(), EVIDENCE_DIR);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                Log.e(TAG, "Failed to create evidence directory");
                return null;
            }
        }
        return dir;
    }

    public File createEvidenceFile(String sosId) {
        File dir = getEvidenceDirectory();
        if (dir == null) {
            return null;
        }
        
        // Use sosId in filename for linkage, or timestamp if sosId is missing
        String filename = "evidence_" + (sosId != null ? sosId : System.currentTimeMillis()) + ".mp4";
        return new File(dir, filename);
    }

    public File createVideoEvidenceFile(String sosId) {
        File dir = getEvidenceDirectory();
        if (dir == null) {
            return null;
        }
        String filename = "video_" + (sosId != null ? sosId : System.currentTimeMillis()) + ".mp4";
        return new File(dir, filename);
    }

    public File getEvidenceFile(String sosId) {
        File dir = getEvidenceDirectory();
        if (dir == null) {
            return null;
        }
        String filename = "evidence_" + sosId + ".mp4";
        File file = new File(dir, filename);
        return file.exists() ? file : null;
    }
}
