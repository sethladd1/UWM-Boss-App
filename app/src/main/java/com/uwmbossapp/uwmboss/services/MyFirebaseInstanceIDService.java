package com.uwmbossapp.uwmboss.services;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.uwmbossapp.uwmboss.utils.HttpHelper;

import java.io.IOException;

/**
 * Created by seth on 3/28/17.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

private static String TAG="FBInstanceID";



    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        Log.i(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }
    public void sendRegistrationToServer(String token){
        String un=getSharedPreferences("UWMBOSS", MODE_PRIVATE).getString("username", null);
        if(un!=null) {
            String msg = "{\"" + un + "\":{\"token\":\"" + token + "\"}}";
            try {
                HttpHelper.makeHTTPRequest("https://boss-30632.firebaseio.com/tokens.json", msg, "PATCH");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
