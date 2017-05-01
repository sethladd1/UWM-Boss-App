package com.uwmbossapp.uwmboss;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebViewFragment;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.JsonParseException;
import com.uwmbossapp.uwmboss.services.MyFirebaseMessagingService;
import com.uwmbossapp.uwmboss.services.MyService;

import layout.DriverDashBoard;
import layout.DriverHomeFragment;
import models.Driver;
import models.Ride;

public class MainDriverActivity extends AppCompatActivity
        implements DriverHomeFragment.OnDriverPairedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private static final String SHOW_DRIVER_URL = "https://uwm-boss.com/admin/drivers/show";
    private static final String DRIVER_URL = "https://uwm-boss.com/admin/drivers/";
    private static final String RIDE_URL = "https://uwm-boss.com/admin/rides/";
    private FragmentManager fragment_manager;
    private Fragment frag_view;
    private Location location;
    private static final int GET_LOC_PERMISSION = 25;
    private CheckBox availability_button;
    private Button cancel_ride_button;
    private Button delete_ride_button;
    private Ride ride;
    private Driver driver;
    private FrameLayout frag_container;
    private GoogleApiClient api_client;
    private BottomNavigationView bottom_navigation_view;




    private static final float CITY_BLOCK = 274.32f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_driver);
        fragment_manager = getSupportFragmentManager();
        buildGoogleApiClient();
        driver = getIntent().getExtras().getParcelable("driver");

        PATCH_Driver(driver);
        location = getDeviceLocation();
        frag_container = (FrameLayout) findViewById(R.id.driver_navigation_container);
        BottomNavigationView bottom_navigation_view = (BottomNavigationView) findViewById(R.id.navigation);
        bottom_navigation_view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener(){

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.driver_home:
                        if(frag_view instanceof DriverHomeFragment){
                            return false;
                        }
                        frag_view = DriverHomeFragment.newInstance(createLocationParams());
                        break;
                    case R.id.driver_dashboard:
                        frag_view = DriverDashBoard.newInstance();
                        break;
                    case R.id.driver_queue_table:
//                        frag_view = PassengerQueueTableFragment.newInstance(0);
                        return false;
                    case R.id.driver_logout:
                        logoutDriver();
                        startActivity(new Intent(MainDriverActivity.this, MainActivity.class));

                    default:
                        return false;
                }
                final FragmentTransaction transaction = fragment_manager.beginTransaction();
                transaction.replace(frag_container.getId(), frag_view).commit();
                if(frag_view instanceof DriverHomeFragment && ride != null){
                    newRide();
                }
                return true;
            }
        });
        cancel_ride_button = (Button) findViewById(R.id.driver_cancel_ride);
        cancel_ride_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ride == null) {
                    Toast.makeText(MainDriverActivity.this, "No Ride to Cancel", Toast.LENGTH_SHORT).show();
                    return;
                }
                //TODO remove driver from ride object
                canceledRide();
                PATCH_Ride(ride);
            }
        });
        availability_button = (CheckBox) findViewById(R.id.driver_availability);
//        if(driver != null){
//            availability_button.setChecked(driver.isAvailable());
//        }
        availability_button.setChecked(driver.isAvailable());
        availability_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                driver.setAvailability(isChecked);
                PATCH_Driver(driver);
            }
        });
        delete_ride_button = (Button) findViewById(R.id.driver_remove_ride);
        delete_ride_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                deleteRide();
            }
        });

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new DriverBroadcastReceiver(), new IntentFilter(MyService.MY_SERVICE_MESSAGE));
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new RideBroadcastReceiver(), new IntentFilter(MyFirebaseMessagingService.FIREBASE_MESSAGING_SERVICE));
        frag_view = DriverHomeFragment.newInstance();
        final FragmentTransaction init_transaction = fragment_manager.beginTransaction();
        init_transaction.replace(frag_container.getId(), frag_view).commit();
    }

    private void logoutDriver() {
        callServer(DRIVER_URL, null, "DELETE");
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, GET_LOC_PERMISSION);
            }
        }
    }

    private Location getDeviceLocation() {
        Location loc = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                loc = LocationServices.FusedLocationApi.getLastLocation(api_client);
            } else {
                //Toast.makeText(this, "unable to get your location", Toast.LENGTH_SHORT).show();
                checkLocationPermissions();
            }
        } else {
            loc = LocationServices.FusedLocationApi.getLastLocation(api_client);
        }
        return loc;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GET_LOC_PERMISSION: {
                if (!(grantResults.length > 0)) {
                    Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int result : grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                location = getDeviceLocation();
                if (frag_view instanceof DriverHomeFragment) {
                    //frag_view = DriverHomeFragment.newInstance(createLocationParams());
                    ((DriverHomeFragment) frag_view).getMapAsync((DriverHomeFragment) frag_view);
                }
                if(api_client == null){
                    buildGoogleApiClient();
                }
            }

        }

    }

    protected synchronized void buildGoogleApiClient(){
        api_client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        api_client.connect();
    }

    private float[] createLocationParams() {
        if (location == null) {
            location = getDeviceLocation();
        }
        return new float[]{(float) location.getLatitude(), (float) location.getLongitude()};
    }



    public void callServer(String url, String message, String requestType) {
        Intent intent = new Intent(this, MyService.class);
        intent.setData(Uri.parse(url));
        intent.putExtra("message", message);
        intent.putExtra("requestType", requestType);
        startService(intent);
    }


    private void PATCH_Driver(Driver driver) {
        try {
            String content = Driver.toJSON(driver);
            callServer(DRIVER_URL, content, "PATCH");
        }catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    private void GET_Driver() {
        callServer(SHOW_DRIVER_URL, null, "GET");
    }

    private void PATCH_Ride(Ride ride) {
        try{
            String content = Ride.toJson(ride);
            callServer(RIDE_URL, content, "PATCH");
        }catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * calls DriverHomeFragment method to dynamically add views and map stuff for new Ride
     */
    private void newRide() {
        if(frag_view instanceof DriverHomeFragment){
            ((DriverHomeFragment) frag_view).createNewRide(ride);
        }
    }

    private void canceledRide() {
        //TODO: driver can't complete ride, PATCH it and let DriverHomeFragment know that Ride has been canceled
        driver.setAvailability(false);
        ride.setDriver(null);
        ride.driver_id = -1;
        Toast.makeText(this, "You have been set to UNAVAILABLE", Toast.LENGTH_LONG).show();
        PATCH_Ride(ride);
    }

    //TODO: maybe add Dialogs for cancel and delete asking if they are sure they want to do it.

    private void deleteRide() {
        //TODO: ride is bogus delete it from server and let DriverHomeFragment know
        callServer(RIDE_URL, null, "DELETE");
        ride = null;
        if(frag_view instanceof DriverHomeFragment){
            frag_view = DriverHomeFragment.newInstance(driver.getLocation());
            final FragmentTransaction transaction = fragment_manager.beginTransaction();
            transaction.replace(frag_container.getId(), frag_view).commit();
        }
    }

    @Override
    public void finishRide() {
        //TODO: DriverFragment has noticed it has reached the destination, callback for activity to finish ride and things.
        ride = null;
    }



    @Override
    public void onLocationChanged(Location location) {
        driver.setLocation(location);
        PATCH_Driver(driver);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(api_client,
                    new LocationRequest()
                            .setInterval(5000)
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setFastestInterval(1000)
                            .setSmallestDisplacement(CITY_BLOCK),
                    this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO:
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO:
    }

    private class DriverBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
            switch (intent.getStringExtra("url")) {
                case SHOW_DRIVER_URL:
                    if(message == null){
                        Toast.makeText(MainDriverActivity.this, "Unable to connect to UWM BOSS server. We're sorry.", Toast.LENGTH_SHORT).show();
                    }else if(message.trim().equalsIgnoreCase("null")){
                        Toast.makeText(MainDriverActivity.this, "Didn't Receive Driver Info", Toast.LENGTH_SHORT).show();
                    }else{
                        try {
                            Driver updatedDriver = Driver.fromJSON(message);
                            if(updatedDriver != null)
                                driver = Driver.fromJSON(message);
                        }catch (JsonParseException e){
                            e.printStackTrace();
                        }
                    }
                case DRIVER_URL:
                    if(intent.getBooleanExtra("success", false)){
                        Toast.makeText(MainDriverActivity.this, "Driver update successful", Toast.LENGTH_SHORT);
                    }else{
                        Toast.makeText(MainDriverActivity.this, "Error: " + intent.getStringExtra("errorMessage"), Toast.LENGTH_SHORT).show();
                    }

                case RIDE_URL:
                    if(intent.getBooleanExtra("success", false)){
                        Toast.makeText(MainDriverActivity.this, "Ride", Toast.LENGTH_SHORT);
                    }else{
                        Toast.makeText(MainDriverActivity.this, "Error: " + intent.getStringExtra("errorMessage"), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case "https://uwm-boss.com/admin/rides/show":
                    if(intent.getBooleanExtra("success", false)){
                        if(message != null){
                            try{
                                Log.i("Ride Received",message);
                                Toast.makeText(context, "getting ride", Toast.LENGTH_SHORT).show();
                                ride = Ride.fromJSON(message);
                                newRide();
                            }catch (JsonParseException e){
                                e.printStackTrace();
                            }
                        }
                    }
            }
        }

    }
    private class RideBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String content = intent.getStringExtra(MyFirebaseMessagingService.FIREBASE_MESSAGING_SERVICE_PAYLOAD);

            try {
                //ride = Ride.fromJSON(content);
                Toast.makeText(context, "Recieved Push Notification", Toast.LENGTH_SHORT).show();
                callServer("https://uwm-boss.com/admin/rides/show", null, "GET");
//                newRide();
            }catch (JsonParseException e){
                e.printStackTrace();
            }
        }
    }
}
