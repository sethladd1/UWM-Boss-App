package com.uwmbossapp.uwmboss.services;



import android.app.NotificationManager;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import static com.uwmbossapp.uwmboss.R.mipmap.ic_launcher;

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

        String title = "UWM BOSS";
        if(remoteMessage.getNotification().getTitle()!=null){
            title = remoteMessage.getNotification().getTitle();
        }
        if(remoteMessage.getNotification().getBody()!=null) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(ic_launcher).setContentTitle(title).setContentText(remoteMessage.getNotification().getBody());
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(129, mBuilder.build());
        }
    }
}
