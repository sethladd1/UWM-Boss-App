package com.uwmbossapp.uwmboss;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.uwmbossapp.uwmboss.services.MyService;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity {
    private static final String USER_URL = "https://uwm-boss.com/admin/users/show";
    private final int WEBLOGINID = 0;
    private final int ACCOUNTACTIVITYID=1;
    private SharedPreferences sharedPreferences;
    private String token;
    private static final String TAG = "Main";
    private boolean loggedIn;
    private String accountInfo = null;
    HttpCookie tokenCookie;
    private BroadcastReceiver receiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String url = intent.getStringExtra("url");

            String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
            if(message==null){
                Toast.makeText(MainActivity.this, "Unable to connect to UWM BOSS server. We're sorry.", Toast.LENGTH_LONG).show();
            }
            else
                switch (url){
                    case USER_URL:
                        if(message.trim().equals("null")){
                            login();
                        }
                        else {
                            accountInfo = message;
                            loggedIn=true;
                        }
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

        getSupportActionBar().setTitle("UWM BOSS");
        //getSupportActionBar().setIcon(getDrawable(R.drawable.profile));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, new IntentFilter(MyService.MY_SERVICE_MESSAGE));
        loggedIn = false;


        sharedPreferences = getSharedPreferences("UWMBOSS", MODE_PRIVATE);
        if(sharedPreferences.contains("token")){

            token = sharedPreferences.getString("token", null);

            if(token != null) {
                loggedIn = true;
                storeCookie("https://uwm-boss.com", "token", token);
                callServer(USER_URL);
            }
        }
        if(!loggedIn){
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
        switch (id){
            case R.id.action_account:

                if(loggedIn){
                    if(accountInfo != null) {
                        Intent accountIntent = new Intent(this, AccountActivity.class);
                        accountIntent.putExtra("accountInfo", accountInfo);
                        startActivityForResult(accountIntent, ACCOUNTACTIVITYID);
                    }
                    else
                        callServer(USER_URL);
                }
                else{
                    login();
                }
                return true;
            case R.id.action_settings:
//                TODO: launch settings page
                return true;

        }



        return super.onOptionsItemSelected(item);
    }
    private void login(){
        Toast.makeText(this, "Please log in with your UWM credentials", Toast.LENGTH_SHORT).show();
        Intent loginIntent = new Intent(this, WebLogin.class);
        startActivityForResult(loginIntent, WEBLOGINID);
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
        tokenCookie = new HttpCookie(name,value);
        tokenCookie.setPath("/");
        tokenCookie.setDomain("uwm-boss.com");
        tokenCookie.setVersion(0);

        CookieManager cookieManager = (CookieManager)CookieHandler.getDefault();
        if(cookieManager==null){
            CookieHandler.setDefault(new CookieManager());
            cookieManager = (CookieManager)CookieHandler.getDefault();
        }
        try {
            cookieManager.getCookieStore().add(new URI(url), tokenCookie);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case WEBLOGINID:
                if(resultCode == Activity.RESULT_OK){
                    token = data.getStringExtra("token");

                    if(token != null) {
                        storeCookie("uwm-boss.com", "token", token);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.apply();
                        loggedIn=true;
                        callServer(USER_URL);
                    }


                }
                else{
                    loggedIn=false;
                }
                return;
            case ACCOUNTACTIVITYID:
                if(resultCode == Activity.RESULT_OK){
                    boolean logout = data.getBooleanExtra("logout", false);
                    if(logout){
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("token");
                        CookieManager cookieManager = (CookieManager)CookieHandler.getDefault();
                        try {
                            cookieManager.getCookieStore().remove(new URI("https://uwm-boss.com"), tokenCookie);

                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        android.webkit.CookieManager.getInstance().removeAllCookie();
                        loggedIn = false;
                    }
                }
                return;
            default:
                break;


        }
    }



}
