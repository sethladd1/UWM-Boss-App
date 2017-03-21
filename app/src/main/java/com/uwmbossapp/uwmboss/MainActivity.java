package com.uwmbossapp.uwmboss;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.uwmbossapp.uwmboss.services.MyService;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final String USER_URL = "https://uwm-boss.com/admin/users/show";
    private final int WEBLOGIN = 0;
    private SharedPreferences sharedPreferences;
    private String token;
    private static final String TAG = "Main";
    private boolean loggedIn;
    private BroadcastReceiver receiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String url = intent.getStringExtra("url");
//            Log.i(TAG, "onReceive: url=" + url);
            switch (url){
                case USER_URL:
                    String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
                    if(message.trim().equals("null")){
                        login();
                    }
//TODO: populate user info with received data
//                    Log.i(TAG, "onReceive: \n" + message);
                    break;
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        loggedIn = false;

        Log.i(TAG, "onCreate: ");
        sharedPreferences = getSharedPreferences("UWMBOSS", MODE_PRIVATE);
        if(sharedPreferences.contains("token")){
            
            token = sharedPreferences.getString("token", null);
//            Log.i(TAG, "onCreate: token in SP="+token);
            if(token != null) {
                loggedIn = true;
                storeCookie("https://uwm-boss.com", "token", token);
            }
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, new IntentFilter(MyService.MY_SERVICE_MESSAGE));
        if(!loggedIn){
//            Log.i(TAG, "onCreate: no token in SP. Logging in.");
            login();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private void login(){
        Toast.makeText(this, "Please log in with your UWM credentials", Toast.LENGTH_SHORT);
        Intent loginIntent = new Intent(this, WebLogin.class);
        startActivityForResult(loginIntent, WEBLOGIN);
    }
    public void callServer(String url){
        if(!loggedIn){
            login();
        }
        else {
            Intent intent = new Intent(this, MyService.class);
            intent.setData(Uri.parse(url));
            startService(intent);
        }
    }
    private void storeCookie(String url, String name, String value){
        HttpCookie cookie = new HttpCookie(name,value);
        cookie.setPath("/");
        cookie.setDomain("uwm-boss.com");
        cookie.setVersion(0);

        CookieManager cookieManager = (CookieManager)CookieHandler.getDefault();
        if(cookieManager==null){
            CookieHandler.setDefault(new CookieManager());
            cookieManager = (CookieManager)CookieHandler.getDefault();
        }
        try {
            cookieManager.getCookieStore().add(new URI(url), cookie);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case WEBLOGIN:
                if(resultCode == Activity.RESULT_OK){
                    token = data.getStringExtra("token");

                    if(token != null) {
                        storeCookie("uwm-boss.com", "token", token);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.apply();
                        callServer(USER_URL);
                    }
                    loggedIn=true;

                }
                else{
                    loggedIn=false;
                }
                break;
            default:
                break;


        }
    }



}
