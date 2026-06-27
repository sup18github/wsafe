package com.android.sheguard.util;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.sheguard.repository.EvidenceRepository;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class UploadManager {

    private static final String TAG = "UploadManager";
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final String WORK_TAG_EVIDENCE_UPLOAD = "evidence_upload";

    private final Context context;
    private final EvidenceRepository evidenceRepository;

    public UploadManager(Context context) {
        this.context = context;
        this.evidenceRepository = new EvidenceRepository();
    }

    public void uploadEvidence(File file, String sosEventId, long timestamp, double lat, double lng, String type) {
        String uniqueWorkName = WORK_TAG_EVIDENCE_UPLOAD + "_" + type + "_" + sosEventId;

        Data inputData = new Data.Builder()
                .putString("file_path", file.getAbsolutePath())
                .putString("sos_event_id", sosEventId)
                .putLong("timestamp", timestamp)
                .putDouble("latitude", lat)
                .putDouble("longitude", lng)
                .putString("type", type)
                .build();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest uploadWork = new OneTimeWorkRequest.Builder(EvidenceUploadWorker.class)
                .setInputData(inputData)
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.SECONDS)
                .addTag(uniqueWorkName)
                .build();

        WorkManager.getInstance(context).enqueue(uploadWork);
        Log.i(TAG, "Evidence upload work queued for SOS: " + sosEventId + " type: " + type);
    }

    public static class EvidenceUploadWorker extends Worker {

        private final EvidenceRepository repository;

        public EvidenceUploadWorker(Context context, WorkerParameters workerParams) {
            super(context, workerParams);
            repository = new EvidenceRepository();
        }

        @NonNull
        @Override
        public Result doWork() {
            String filePath = getInputData().getString("file_path");
            String sosEventId = getInputData().getString("sos_event_id");
            long timestamp = getInputData().getLong("timestamp", 0);
            double lat = getInputData().getDouble("latitude", 0);
            double lng = getInputData().getDouble("longitude", 0);
            String type = getInputData().getString("type");
            if (type == null) type = "audio";

            if (filePath == null || sosEventId == null) {
                Log.e(TAG, "Invalid input data for upload worker");
                return Result.failure();
            }

            File file = new File(filePath);
            if (!file.exists()) {
                Log.e(TAG, "Evidence file not found: " + filePath);
                return Result.failure();
            }

            Log.i(TAG, "Starting upload for: " + sosEventId + ", attempt: " + getRunAttemptCount());

            try {
                repository.uploadEvidenceWithRetry(file, sosEventId, timestamp, lat, lng, type);
                Log.i(TAG, "Upload completed for: " + sosEventId);
                return Result.success();
            } catch (Exception e) {
                Log.e(TAG, "Upload failed for: " + sosEventId, e);
                if (getRunAttemptCount() < MAX_RETRY_ATTEMPTS) {
                    Log.i(TAG, "Scheduling retry, attempt: " + (getRunAttemptCount() + 1));
                    return Result.retry();
                } else {
                    Log.e(TAG, "Max retry attempts reached, marking as failure");
                    return Result.failure();
                }
            }
        }
    }
}