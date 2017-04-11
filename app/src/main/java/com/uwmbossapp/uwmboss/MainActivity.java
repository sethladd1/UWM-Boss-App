package com.uwmbossapp.uwmboss;


import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.uwmbossapp.uwmboss.services.MyFirebaseInstanceIDService;
import com.uwmbossapp.uwmboss.services.MyFirebaseMessagingService;
import com.uwmbossapp.uwmboss.services.MyService;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String USER_URL = "https://uwm-boss.com/admin/users/show",
    FIREBASE_TOKENS_URL = "https://boss-30632.firebaseio.com/tokens.json",
    REQUEST_RIDE_URL = "https://uwm-boss.com/admin/ride";
    //    append username+".json" to this url when calling
    private static final String FIREBASE_USER_URL = "https://boss-30632.firebaseio.com/tokens/";
    private final int WEBLOGINID = 0, ACCOUNTACTIVITYID = 1, SETCURLOCPERMISSION = 55, SETDESTTOCURLOC=56;
    private SharedPreferences sharedPreferences;
    private String token;
    private static final String TAG = "Main";
    private boolean loggedIn = false;
    private String accountInfo = null, username = null;
    private HttpCookie tokenCookie;
    private GoogleApiClient mLocationClient;
    private Marker destMarker, pickupMarker;
    private GoogleMap mMap;
    private LatLng dest, pickup;
    private static final int ERROR_DIALOG_REQUEST = 78898;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String url = intent.getStringExtra("url");
            String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
            switch (url) {
                case USER_URL:
                    Log.i(TAG, "onReceive: " + message);
                    if (message == null) {
                        Toast.makeText(MainActivity.this, "Unable to connect to UWM BOSS server. We're sorry.", Toast.LENGTH_LONG).show();
                        loggedIn = false;
                    } else {
                        if (message.trim().equals("null")) {
                            loggedIn=false;
                            login();
                        } else {
                            accountInfo = message;
                            loggedIn = true;
                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(accountInfo);
                                String email = jsonObject.getString("email");
                                if (email != null) {
                                    String[] arr = email.split("@");
                                    if (arr.length > 0) {
                                        username = arr[0];
                                        Toast.makeText(MainActivity.this, "Logged in as " + username, Toast.LENGTH_LONG).show();
                                        SharedPreferences.Editor editor = sharedPreferences.edit().putString("username", username);
                                        editor.apply();
                                    }
                                    startFirebaseServices();
                                    String fbToken = FirebaseInstanceId.getInstance().getToken();
                                    callServer(FIREBASE_TOKENS_URL, "{\"" + username + "\":{\"token\":\"" + fbToken + "\"}}", "PATCH");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
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
        PlaceAutocompleteFragment autocompleteFragmentDest = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_dest);
        autocompleteFragmentDest.setHint("Where to?");
        autocompleteFragmentDest.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                dest = place.getLatLng();
                if(mMap != null){
                    if(destMarker != null){
                        destMarker.remove();
                    }
                    MarkerOptions options = new MarkerOptions().title("Destination").position(dest);
                    destMarker = mMap.addMarker(options);
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(dest, 15);
                    mMap.animateCamera(update);
                }
                Log.i(TAG, "Place: " + place.getLatLng());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        PlaceAutocompleteFragment autocompleteFragmentPickup = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_pick_up);
        autocompleteFragmentPickup.setHint("Pick Up (default: your location)");
        autocompleteFragmentPickup.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                pickup = place.getLatLng();
                if(mMap != null){
                    if(pickupMarker!=null){
                        pickupMarker.remove();
                    }
                    MarkerOptions options = new MarkerOptions().title("Pick Up").position(pickup);
                    pickupMarker =  mMap.addMarker(options);
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(pickup, 15);
                    mMap.animateCamera(update);
                }
                Log.i(TAG, "Place: " + place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, new IntentFilter(MyService.MY_SERVICE_MESSAGE));
        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        mLocationClient.connect();
        initMap();
        if(!loggedIn) {
            sharedPreferences = getSharedPreferences("UWMBOSS", MODE_PRIVATE);
            if (sharedPreferences.contains("token")) {
                token = sharedPreferences.getString("token", null);
                username = sharedPreferences.getString("username", null);
                if (token != null && username != null) {
                    loggedIn = true;
                    storeCookie("https://uwm-boss.com", "token", token);
                    callServer(USER_URL, null, "GET");
                }
            }
            if (!loggedIn) {
                login();
            }
        }
    }

    public void startFirebaseServices() {
        Intent intent = new Intent(this, MyFirebaseMessagingService.class);
        startService(intent);
        Intent intent2 = new Intent(this, MyFirebaseInstanceIDService.class);
        startService(intent2);
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
        switch (id) {
            case R.id.action_account:
                if (loggedIn) {
                    if (accountInfo != null) {
                        Intent accountIntent = new Intent(this, AccountActivity.class);
                        accountIntent.putExtra("accountInfo", accountInfo);
                        startActivityForResult(accountIntent, ACCOUNTACTIVITYID);
                    } else
                        callServer(USER_URL, null, "GET");
                } else {
                    login();
                }
                return true;
            case R.id.action_profile:
                startActivity(new Intent(this, profile.class));


            case R.id.action_report:
                startActivity(new Intent(this, report.class));

        }


        return super.onOptionsItemSelected(item);
    }

    private void login() {
        Toast.makeText(this, "Please log in with your UWM credentials", Toast.LENGTH_SHORT).show();
        Intent loginIntent = new Intent(this, WebLogin.class);
        startActivityForResult(loginIntent, WEBLOGINID);
    }

    public void callServer(String url, String message, String requestType) {
        if (!loggedIn) {
            login();
        } else {
            Intent intent = new Intent(this, MyService.class);
            intent.setData(Uri.parse(url));
            intent.putExtra("message", message);
            intent.putExtra("requestType", requestType);
            startService(intent);
        }
    }

    private void storeCookie(String url, String name, String value) {
        tokenCookie = new HttpCookie(name, value);
        tokenCookie.setPath("/");
        tokenCookie.setDomain("uwm-boss.com");
        tokenCookie.setVersion(0);

        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        if (cookieManager == null) {
            CookieHandler.setDefault(new CookieManager());
            cookieManager = (CookieManager) CookieHandler.getDefault();
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
        switch (requestCode) {
            case WEBLOGINID:
                if (resultCode == Activity.RESULT_OK) {
                    token = data.getStringExtra("token");

                    if (token != null) {
                        storeCookie("uwm-boss.com", "token", token);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.apply();
                        loggedIn = true;
                        callServer(USER_URL, null, "GET");
                    }
                } else {
                    loggedIn = false;
                }
                return;
            case ACCOUNTACTIVITYID:
                if (resultCode == Activity.RESULT_OK) {
                    boolean logout = data.getBooleanExtra("logout", false);
                    if (logout) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.remove("token");
                        editor.remove("username");
                        editor.apply();
                        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
                        try {
                            cookieManager.getCookieStore().remove(new URI("https://uwm-boss.com"), tokenCookie);

                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        }
                        if (username != null)
                            callServer(FIREBASE_USER_URL + username + ".json", null, "DELETE");
                        username = "";
                        token = "";
                        android.webkit.CookieManager.getInstance().removeAllCookie();
                        loggedIn = false;
                    }
                }
                return;
            default:
                break;


        }
    }

    public boolean servicesOK() {
        int isAvail = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (isAvail == ConnectionResult.SUCCESS) {
            return true;
        } else if (GooglePlayServicesUtil.isUserRecoverableError(isAvail)) {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(isAvail, this, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(this, "Unable to connect to map service", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private void initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    Log.i(TAG, "onMapReady: mMap set");
//                    in case location services isn't available, show UWM
                    LatLng uwmLatLng = new LatLng(43.078252, -87.881995);
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(uwmLatLng, 15);
                    mMap.moveCamera(update);
//                    if location client is connected first, set location, otherwise onConnection will handle it
                    if(mLocationClient.isConnected())
                        setCurrentLocation();
                }
            });
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted=true;
        for(int i=0;i<grantResults.length;++i){
            if(grantResults[i] != android.content.pm.PackageManager.PERMISSION_GRANTED){
                granted = false;
                break;
            }
        }
        if(granted) {
            if (requestCode == SETCURLOCPERMISSION) {
                setCurrentLocation();
            }
            else if(requestCode==SETDESTTOCURLOC){
                getRide(null);
            }
        }

    }

    private void setCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[2];
            permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
            permissions[1] = Manifest.permission.ACCESS_COARSE_LOCATION;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, SETCURLOCPERMISSION);
            }
            return;
        }

        mMap.setMyLocationEnabled(true);
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
        if(currentLocation == null){
            Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "setCurrentLocation failed: " + new Boolean(mLocationClient.isConnected()));
        }
        else{
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(update);
        }
    }
    public void getRide(View v){
        if(dest != null){

            if(pickup ==null){
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    String[] permissions = new String[2];
                    permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
                    permissions[1] = Manifest.permission.ACCESS_COARSE_LOCATION;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(permissions, SETCURLOCPERMISSION);
                    }
                    return;
                }
                Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
                if(currentLocation==null){
                    Toast.makeText(this, "Unable to get location. Please enter pick up location manually", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            String json = "{\"picklat\":\""+new Double(pickup.latitude)
                    +"\",\"picklong\":\""+new Double(pickup.longitude)
                    +"\",\"destlat\":\""+new Double(dest.latitude)
                    +"\",\"destlong\":\""+new Double(dest.longitude)+"\"}";
            callServer(REQUEST_RIDE_URL, json, "PUT");

        }
        else{
            Toast.makeText(this, "Destination is required for ride request", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected: ");
//        if the map is initialized before the connection: set locaction, otherwise initMap callback will handle it
        if(mMap != null)
            setCurrentLocation();
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed: ");
    }
}
