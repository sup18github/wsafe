package com.android.sheguard.repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.sheguard.common.Constants;
import com.android.sheguard.model.EvidenceModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EvidenceRepository {

    private static final String TAG = "EvidenceRepository";
    private static final String STORAGE_PATH = "evidence/";
    private static final String FIRESTORE_COLLECTION = "Evidence";

    private final FirebaseStorage storage;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    public EvidenceRepository() {
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void uploadEvidence(File file, String sosEventId, long timestamp, double lat, double lng, String type) {
        if (auth.getCurrentUser() == null) {
            Log.e(TAG, "User not logged in. Cannot upload evidence.");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        String prefix = "video".equals(type) ? "video_" : "evidence_";
        String fileName = prefix + timestamp + ".mp4";
        String path = STORAGE_PATH + userId + "/" + fileName;

        StorageReference ref = storage.getReference().child(path);
        Uri fileUri = Uri.fromFile(file);

        UploadTask uploadTask = ref.putFile(fileUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "Upload success: " + path);
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        saveMetadata(sosEventId, userId, timestamp, lat, lng, uri.toString(), path, type);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Upload failed", e);
            }
        });
    }

    private void saveMetadata(String sosEventId, String userId, long timestamp, double lat, double lng, String downloadUrl, String storagePath, String type) {
        Map<String, Object> evidenceData = new HashMap<>();
        evidenceData.put("sosEventId", sosEventId);
        evidenceData.put("userId", userId);
        evidenceData.put("timestamp", timestamp);
        evidenceData.put("latitude", lat);
        evidenceData.put("longitude", lng);
        evidenceData.put("downloadUrl", downloadUrl);
        evidenceData.put("storagePath", storagePath);
        evidenceData.put("type", type != null ? type : "audio");

        firestore.collection(FIRESTORE_COLLECTION)
                .add(evidenceData)
                .addOnSuccessListener(documentReference -> Log.i(TAG, "Metadata saved with ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e(TAG, "Error adding document", e));
    }

    public void uploadEvidenceWithRetry(File file, String sosEventId, long timestamp, double lat, double lng, String type) throws Exception {
        if (auth.getCurrentUser() == null) {
            throw new Exception("User not logged in");
        }

        String userId = auth.getCurrentUser().getUid();
        String prefix = "video".equals(type) ? "video_" : "evidence_";
        String fileName = prefix + timestamp + ".mp4";
        String path = STORAGE_PATH + userId + "/" + fileName;

        StorageReference ref = storage.getReference().child(path);
        Uri fileUri = Uri.fromFile(file);

        UploadTask uploadTask = ref.putFile(fileUri);
        
        com.google.android.gms.tasks.Tasks.await(uploadTask);
        
        Log.i(TAG, "Upload success with retry: " + path);
        Uri downloadUri = com.google.android.gms.tasks.Tasks.await(ref.getDownloadUrl());
        saveMetadata(sosEventId, userId, timestamp, lat, lng, downloadUri.toString(), path, type);
    }

    public void getEvidenceForUser(OnEvidenceLoadedListener listener) {
        if (auth.getCurrentUser() == null) {
            listener.onError("User not logged in");
            return;
        }

        String userId = auth.getCurrentUser().getUid();
        
        firestore.collection(FIRESTORE_COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    java.util.List<EvidenceModel> evidenceList = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        EvidenceModel evidence = doc.toObject(EvidenceModel.class);
                        if (evidence != null) {
                            evidence.setId(doc.getId());
                            evidence.setUploaded(true);
                            evidenceList.add(evidence);
                        }
                    }
                    listener.onSuccess(evidenceList);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching evidence", e);
                    listener.onError(e.getMessage());
                });
    }

    public interface OnEvidenceLoadedListener {
        void onSuccess(java.util.List<EvidenceModel> evidenceList);
        void onError(String error);
    }
}
