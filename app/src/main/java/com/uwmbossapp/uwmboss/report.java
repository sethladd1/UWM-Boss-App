package com.uwmbossapp.uwmboss;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.uwmbossapp.uwmboss.services.MyFirebaseMessagingService;
import com.uwmbossapp.uwmboss.services.MyService;

public class report extends AppCompatActivity {

    private Chronometer simpleChronometer;
    final private static String TAG = "ReportPage";
    final private static String AVGTIMEURL= "https://uwm-boss.com/admin/rides/average_pickup";
    boolean waitingForRide = false;
    private TextView avgTimeText;
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("success", false)) {
                String url = intent.getStringExtra("url");
                String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
                if (url.equals(AVGTIMEURL)) {
                    avgTimeText.setText("Average wait time for a ride: " + message);
                }
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mToolbar);
        setContentView(R.layout.activity_report);
        avgTimeText = (TextView) findViewById(R.id.avgTimeLabel);
        callServer(AVGTIMEURL, null, "GET");
        Chronometer simpleChronometer = (Chronometer) findViewById(R.id.simpleChronometer); // initiate a chronometer
        Intent intent = getIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new IntentFilter(MyService.MY_SERVICE_MESSAGE));
        waitingForRide = intent.getBooleanExtra("waitingForRide", false);

        if(waitingForRide)
            simpleChronometer.start(); // start a chronometer
        simpleChronometer.setFormat("%s"); // set the format for a chronometer
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    public void cancelRide(View v){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("rideCancelled", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    private void closeActivity(){
        finish();
    }
    public void callServer(String url, String message, String requestType) {

        Intent intent = new Intent(this, MyService.class);
        intent.setData(Uri.parse(url));
        intent.putExtra("message", message);
        intent.putExtra("requestType", requestType);
        startService(intent);

    }


}
