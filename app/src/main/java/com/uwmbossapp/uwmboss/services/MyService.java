package com.uwmbossapp.uwmboss.services;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.uwmbossapp.uwmboss.utils.HttpHelper;

import java.io.IOException;

/**
 * Created by seth on 3/13/17.
 */

public class MyService extends IntentService {
    public static final String TAG = "MyService";
    public static final String MY_SERVICE_MESSAGE = "myServiceMessage";
    public static final String MY_SERVICE_PAYLOAD = "myServicePayload";


    public MyService() {
        super("MyService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Uri uri = intent.getData();

        String type = intent.getStringExtra("requestType");
        String response=null;
        String msg = intent.getStringExtra("message");
        String errorMessage ="";
        boolean success = true;
        try {
            response = HttpHelper.makeHTTPRequest(uri.toString(), msg, type);
        } catch (IOException e) {
            success=false;
            errorMessage = e.getMessage();
            e.printStackTrace();
        }

        Intent messageIntent = new Intent(MY_SERVICE_MESSAGE);
        messageIntent.putExtra(MY_SERVICE_PAYLOAD, response);
        messageIntent.putExtra("url", uri.toString());
        messageIntent.putExtra("success", success);
        messageIntent.putExtra("errorMessage", errorMessage);


        LocalBroadcastManager manager =
                LocalBroadcastManager.getInstance(getApplicationContext());
        manager.sendBroadcast(messageIntent);
    }
}

