package com.official.ihome.Fragment;

import com.official.ihome.Notification.MyResponse;
import com.official.ihome.Notification.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAdg7NFac:APA91bFvUoWaB9F1wCQrAGp9S2CfNva1hDDfTpDVD3MQKo1H9C99qmMDktm0iBFTjpMHXI3SZ48q1NcPcVToLJW8k0cBBsOmBwPaWKq2qd2LNQCNuTSJg41UfvokceNh5dUcKOGAR_Un"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
