package com.uwmbossapp.uwmboss;

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
import android.view.MenuItem;
import android.widget.TextView;

import com.uwmbossapp.uwmboss.services.MyService;

public class InfoActivity extends AppCompatActivity {

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("success", false)) {
                String url = intent.getStringExtra("url");
                String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
                if (url.equals(AVGTIMEURL)) {
                    try {
                        int time = Integer.parseInt(message);
                        if(time<1){
                            avgTimeText.setText("Current average wait time before pickup: unknown");
                        }
                        else{
                            avgTimeText.setText("Current average wait time before pickup: " + message + "m");
                        }

                    }
                    catch(NumberFormatException e){
                        avgTimeText.setText("Current average wait time before pickup: unknown");
                    }

                }
            }
        }
    };
    final private static String AVGTIMEURL= "https://uwm-boss.com/admin/rides/average_pickup";
    TextView avgTimeText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mToolbar);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab!=null){
            ab.setTitle("INFO");
            ab.setDisplayHomeAsUpEnabled(true);
        }
        avgTimeText = (TextView) findViewById(R.id.waitTimeText);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(broadcastReceiver, new IntentFilter(MyService.MY_SERVICE_MESSAGE));
        callServer(AVGTIMEURL, null, "GET");

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public void callServer(String url, String message, String requestType) {
        Intent intent = new Intent(this, MyService.class);
        intent.setData(Uri.parse(url));
        intent.putExtra("message", message);
        intent.putExtra("requestType", requestType);
        startService(intent);

    }
}
