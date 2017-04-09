package com.uwmbossapp.uwmboss.services;


import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by seth on 3/28/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static String TAG = "FirebaseMessaging";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.i(TAG, "From: " + remoteMessage.getFrom());
        Log.i(TAG, "Notification Title"+ remoteMessage.getNotification().getTitle());
        Log.i(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
    }
}
