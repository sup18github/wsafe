package com.android.sheguard.api;

import com.android.sheguard.model.NotificationSenderModel;
import com.android.sheguard.util.NotificationResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface NotificationAPI {

    @Headers("Content-Type:application/json")
    @POST("v1/projects/YOUR_PROJECT_ID/messages:send") //
    Call<NotificationResponse> sendNotification(@Header("Authorization") String authHeader, @Body NotificationSenderModel body);
}
