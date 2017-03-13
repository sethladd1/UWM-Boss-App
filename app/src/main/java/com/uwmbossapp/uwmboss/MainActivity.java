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

import com.uwmbossapp.uwmboss.services.MyService;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String USER_URL = "https://uwm-boss.com/admin/users/show";
    private final int WEBLOGIN = 0;
    private SharedPreferences sharedPreferences;
    private String token;
    private static final String TAG = "Main";
    private int signinAttempts = 0;
    private BroadcastReceiver receiver =  new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
//            if(message.trim().equals("null")){
//                login();
//            }

            Log.i(TAG, "onReceive: \n" + message);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        boolean loggedIn = false;


        sharedPreferences = getSharedPreferences("UWMBOSS", MODE_PRIVATE);
//        if(sharedPreferences.contains("token")){
//            token = sharedPreferences.getString("token", null);
//            if(token != null) {
//                loggedIn = true;
//                storeCookie("https://uwm-boss.com", "token", token);
//
//                Intent intent = new Intent(this, MyService.class);
//                intent.setData(Uri.parse(USER_URL));
//                startService(intent);
//            }
//        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, new IntentFilter(MyService.MY_SERVICE_MESSAGE));
        if(!loggedIn){
            login();
        }
    }
    private void login(){
        Intent loginIntent = new Intent(this, WebLogin.class);
        startActivityForResult(loginIntent, WEBLOGIN);
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

                        Intent intent = new Intent(this, MyService.class);
                        intent.setData(Uri.parse(USER_URL));
                        startService(intent);

                    }

                }
                break;
            default:
                break;


        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
