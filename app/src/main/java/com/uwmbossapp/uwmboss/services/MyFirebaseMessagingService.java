package com.uwmbossapp.uwmboss.services;



import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import static com.uwmbossapp.uwmboss.R.mipmap.ic_launcher;

/**
 * Created by seth on 3/28/17.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static String TAG = "FirebaseMessaging";
    public static final String FIREBASE_MESSAGING_SERVICE = "FirebaseMesssagingService";
    public static final String FIREBASE_MESSAGING_SERVICE_PAYLOAD = "FirebaseMesssagingServicePayload";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        String title = "UWM BOSS";
        if(remoteMessage.getNotification().getTitle()!=null){
            title = remoteMessage.getNotification().getTitle();
        }
        if(remoteMessage.getNotification().getBody()!=null) {
            if(remoteMessage.getNotification().getBody().trim().startsWith("You have been paired with a passenger:"))
            {
                LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getApplicationContext());
                                Intent messageIntent = new Intent(FIREBASE_MESSAGING_SERVICE);
                messageIntent.putExtra(FIREBASE_MESSAGING_SERVICE_PAYLOAD, remoteMessage.getNotification().getBody());
                manager.sendBroadcast(messageIntent);
            }
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext()).setSmallIcon(ic_launcher).setContentTitle(title).setContentText(remoteMessage.getNotification().getBody());
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(129, mBuilder.build());
        }

    }
}
