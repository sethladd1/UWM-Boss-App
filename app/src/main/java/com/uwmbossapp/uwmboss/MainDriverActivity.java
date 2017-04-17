package com.uwmbossapp.uwmboss;

import android.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import layout.DriverDashBoard;
import layout.DriverHomeFragment;
import layout.PassengerQueueTableFragment;
import models.Driver;

public class MainDriverActivity extends AppCompatActivity {


    private String username;
    private FrameLayout container;
    private FragmentManager fragment_manager;
    private SharedPreferences sharedPreferences;
    private Fragment frag_view;
    private Location location;
    private final int GET_LOC_PERMISSION = 25;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.driver_home:
                    frag_view = DriverHomeFragment.newInstance(createLocationParams());
                    break;
                case R.id.driver_dashboard:
                    frag_view = DriverDashBoard.newInstance();
                    break;
                case R.id.driver_queue_table:
                    frag_view = PassengerQueueTableFragment.newInstance(0);
                    break;
            }
            final FragmentTransaction transaction = fragment_manager.beginTransaction();
            transaction.replace(R.id.driver_navigation_container, frag_view).commit();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_driver);

        sharedPreferences = getSharedPreferences("UWMBOSS", MODE_PRIVATE);
        username = getIntent().getStringExtra("username");
        fragment_manager = getSupportFragmentManager();
        location = getDeviceLocation();

        container = (FrameLayout) findViewById(R.id.driver_navigation_container);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }



    private Location getDeviceLocation() {
        Location loc = null;
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[2];
                permissions[0] = android.Manifest.permission.ACCESS_FINE_LOCATION;
                permissions[1] = android.Manifest.permission.ACCESS_COARSE_LOCATION;
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(permissions, GET_LOC_PERMISSION);
                }
                return null;
            }
            loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }else if(manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            loc = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }else{
            Toast.makeText(this, "unable to get your location", Toast.LENGTH_SHORT).show();
        }
        return loc;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int result : grantResults) {
            if(result != PackageManager.PERMISSION_GRANTED)
                return;
        }
        location = getDeviceLocation();
        frag_view = DriverHomeFragment.newInstance(createLocationParams());

    }

    private float[] createLocationParams() {
        return new float[]{(float)location.getLatitude(), (float)location.getLongitude()};
    }
}
