package com.android.sheguard;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;
import com.google.gson.Gson;

public class SheGuard extends Application {

    public static final Gson GSON = new Gson();
    @SuppressLint("StaticFieldLeak")
    public static Context context;

    public static Context getAppContext() {
        return context;
    }

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        DynamicColors.applyToActivitiesIfAvailable(this);
        
        try {
            com.google.firebase.firestore.FirebaseFirestoreSettings settings = 
                new com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
            com.google.firebase.firestore.FirebaseFirestore.getInstance().setFirestoreSettings(settings);
        } catch (Exception e) {
            // Settings might already be set
        }
    }
}