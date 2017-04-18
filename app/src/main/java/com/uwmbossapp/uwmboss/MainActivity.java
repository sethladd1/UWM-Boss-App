package com.uwmbossapp.uwmboss;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
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
import java.util.List;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String USER_URL = "https://uwm-boss.com/admin/users/show",
            FIREBASE_TOKENS_URL = "https://boss-30632.firebaseio.com/tokens.json",
            REQUEST_RIDE_URL = "https://uwm-boss.com/admin/rides", COOKIES="https://uwm-boss.com/cookies";
    //    append username+".json" to this url when calling
    private static final String FIREBASE_USER_URL = "https://boss-30632.firebaseio.com/tokens/";
    private final int WEBLOGINID = 0, ACCOUNTACTIVITYID = 1, REPORTACTIVITYID=3, SETCURLOCPERMISSION = 55, SETDESTTOCURLOC=56;
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
    PlaceAutocompleteFragment autocompleteFragmentDest, autocompleteFragmentPickup;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String url = intent.getStringExtra("url");

            String message = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
            switch (url) {
                case USER_URL:
                    if (message == null) {
                        Toast.makeText(MainActivity.this, "Unable to connect to UWM BOSS server. We're sorry.", Toast.LENGTH_LONG).show();
                        loggedIn = false;
                    } else {
                        if (message.trim().equals("null") || message.trim().equals("You must login first")) {
                            loggedIn=false;
                            login();
                        } else {
                            accountInfo = message;
                            try {
                                JSONObject jsonObject;
                                jsonObject = new JSONObject(accountInfo);
                                username = jsonObject.getString("email").split("@")[0].trim();
                                boolean isDriver = jsonObject.getBoolean("is_driver");
                                Toast.makeText(MainActivity.this, "Logged in as " + username, Toast.LENGTH_LONG).show();
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("username", username);
                                editor.apply();
                                startFirebaseServices();
                                String fbToken = FirebaseInstanceId.getInstance().getToken();
                                callServer(FIREBASE_TOKENS_URL, "{\"" + username + "\":{\"token\":\"" + fbToken + "\"}}", "PATCH");
                                loggedIn=true;
                                if(isDriver){
//                                    TODO: start driver UI
                                }
                            } catch (JSONException e) {
                                loggedIn=false;
                                e.printStackTrace();
                            }

                        }
                    }
                    break;
                case REQUEST_RIDE_URL:
                    if(intent.getBooleanExtra("success", false)){
                        if(!intent.getStringExtra("requestType").equalsIgnoreCase("delete")) {
                            Toast.makeText(MainActivity.this, "Ride request sent and received.", Toast.LENGTH_SHORT).show();
                            dest = null;
                            pickup = null;
                            autocompleteFragmentDest.setText("");
                            autocompleteFragmentPickup.setText("");
                            Intent reportIntent = new Intent(MainActivity.this, report.class);
                            reportIntent.putExtra("waitingForRide", true);
                            startActivityForResult(reportIntent, REPORTACTIVITYID);
                        }
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Error: " + intent.getStringExtra("errorMessage"), Toast.LENGTH_SHORT).show();
                    }
                case COOKIES:
                    Log.i(TAG, "Cookies server saw:" +message);
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        autocompleteFragmentDest = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_dest);
        autocompleteFragmentDest.setHint("Where to?");
        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(43.052134, -87.915197), new LatLng(43.089497, -87.868272));
        autocompleteFragmentDest.setBoundsBias(latLngBounds);
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
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        autocompleteFragmentPickup = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment_pick_up);
        autocompleteFragmentPickup.setHint("Pick Up (default: your location)");
        autocompleteFragmentPickup.setBoundsBias(latLngBounds);
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
                    storeTokenCookie(token);

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
            case R.id.action_report:
                startActivity(new Intent(this, report.class));

        }


        return super.onOptionsItemSelected(item);
    }

    private void login() {
        username = "";
        token = "";
        android.webkit.CookieManager.getInstance().removeAllCookie();
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

    private void storeTokenCookie(String value) {



        CookieManager cookieManager = (CookieManager) CookieHandler.getDefault();
        if (cookieManager == null) {
            CookieHandler.setDefault(new CookieManager());
            cookieManager = (CookieManager) CookieHandler.getDefault();
        }
        try {
            if(tokenCookie != null){
                boolean b = cookieManager.getCookieStore().remove(new URI("uwm-boss.com"), tokenCookie);
            }
            tokenCookie = new HttpCookie("token", value);
            tokenCookie.setPath("/");
            tokenCookie.setDomain("uwm-boss.com");
            tokenCookie.setVersion(0);
            cookieManager.getCookieStore().add(new URI("uwm-boss.com"), tokenCookie);
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
                        storeTokenCookie(token);
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
            case REPORTACTIVITYID:
                if(resultCode == Activity.RESULT_OK){
                    if(data.getBooleanExtra("rideCancelled", false)){
                        Toast.makeText(this, "Ride cancelled", Toast.LENGTH_SHORT).show();
                        callServer(REQUEST_RIDE_URL, null, "DELETE");
                    }
                }
            default:
                break;


        }
    }



    private void initMap() {
        if (mMap == null) {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
//                    coordinates of UWM
                    LatLng uwmLatLng = new LatLng(43.078252, -87.881995);
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(uwmLatLng, 12);
                    mMap.moveCamera(update);
                    drawBoundaries();
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        String[] permissions = new String[2];
                        permissions[0] = Manifest.permission.ACCESS_FINE_LOCATION;
                        permissions[1] = Manifest.permission.ACCESS_COARSE_LOCATION;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(permissions, SETCURLOCPERMISSION);
                        }
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                }
            });
        }
    }

    private void drawBoundaries(){
        PolygonOptions boundaries = new PolygonOptions().fillColor(0x33FF0000).strokeWidth(3).strokeColor(Color.RED);
        LatLng[] boudaryCoordinates = new LatLng[55];
        boudaryCoordinates[0]= new LatLng(43.088263,-87.873593);
        boudaryCoordinates[1]= new LatLng(43.074698,-87.871937);
        boudaryCoordinates[2]= new LatLng(43.074705,-87.869020);
        boudaryCoordinates[3]= new LatLng(43.075332,-87.868946);
        boudaryCoordinates[4]= new LatLng(43.075286,-87.868671);
        boudaryCoordinates[5]= new LatLng(43.074651,-87.868844);
        boudaryCoordinates[6]= new LatLng(43.074637,-87.869612);
        boudaryCoordinates[7]= new LatLng(43.074386,-87.869631);
        boudaryCoordinates[8]= new LatLng(43.074066,-87.868265);
        boudaryCoordinates[9]= new LatLng(43.073422,-87.867828);
        boudaryCoordinates[10]= new LatLng(43.071232,-87.867039);
        boudaryCoordinates[11]= new LatLng(43.070425,-87.867044);
        boudaryCoordinates[12]= new LatLng(43.068633,-87.867741);
        boudaryCoordinates[13]= new LatLng(43.066314,-87.868182);
        boudaryCoordinates[14]= new LatLng(43.065334,-87.868998);
        boudaryCoordinates[15]= new LatLng(43.064330,-87.870903);
        boudaryCoordinates[16]= new LatLng(43.061760,-87.874310);
        boudaryCoordinates[17]= new LatLng(43.058519,-87.876437);
        boudaryCoordinates[18]= new LatLng(43.054434,-87.881381);
        boudaryCoordinates[19]= new LatLng(43.053636,-87.883302);
        boudaryCoordinates[20]= new LatLng(43.053389,-87.885997);
        boudaryCoordinates[21]= new LatLng(43.053416,-87.885964);
        boudaryCoordinates[22]= new LatLng(43.054165,-87.886229);
        boudaryCoordinates[23]= new LatLng(43.054521,-87.886243);
        boudaryCoordinates[24]= new LatLng(43.054954,-87.886051);
        boudaryCoordinates[25]= new LatLng(43.055190,-87.885831);
        boudaryCoordinates[26]= new LatLng(43.055482,-87.885429);
        boudaryCoordinates[27]= new LatLng(43.055640,-87.885392);
        boudaryCoordinates[28]= new LatLng(43.055690,-87.886464);
        boudaryCoordinates[29]= new LatLng(43.054085,-87.888060);
        boudaryCoordinates[30]= new LatLng(43.054567,-87.889006);
        boudaryCoordinates[31]= new LatLng(43.052278,-87.891162);
        boudaryCoordinates[32]= new LatLng(43.052872,-87.892402);
        boudaryCoordinates[33]= new LatLng(43.052976,-87.903339);
        boudaryCoordinates[34]= new LatLng(43.053674,-87.903588);
        boudaryCoordinates[35]= new LatLng(43.055129,-87.905110);
        boudaryCoordinates[36]= new LatLng(43.054548,-87.906710);
        boudaryCoordinates[37]= new LatLng(43.053577,-87.907846);
        boudaryCoordinates[38]= new LatLng(43.052304,-87.909265);
        boudaryCoordinates[39]= new LatLng(43.052431,-87.909756);
        boudaryCoordinates[40]= new LatLng(43.052411,-87.914345);
        boudaryCoordinates[41]= new LatLng(43.074754,-87.914012);
        boudaryCoordinates[42]= new LatLng(43.076294,-87.914732);
        boudaryCoordinates[43]= new LatLng(43.076438,-87.913894);
        boudaryCoordinates[44]= new LatLng(43.076375,-87.913976);
        boudaryCoordinates[45]= new LatLng(43.082105,-87.913761);
        boudaryCoordinates[46]= new LatLng(43.082095,-87.907503);
        boudaryCoordinates[47]= new LatLng(43.085711,-87.907417);
        boudaryCoordinates[48]= new LatLng(43.085754,-87.908544);
        boudaryCoordinates[49]= new LatLng(43.086995,-87.908563);
        boudaryCoordinates[50]= new LatLng(43.087018,-87.910866);
        boudaryCoordinates[51]= new LatLng(43.088165,-87.910858);
        boudaryCoordinates[52]= new LatLng(43.088215,-87.912023);
        boudaryCoordinates[53]= new LatLng(43.089377,-87.912003);
        boudaryCoordinates[54]= new LatLng(43.089272,-87.873910);


        for(int i=0;i<boudaryCoordinates.length;++i){
            boundaries.add(boudaryCoordinates[i]);
        }
        mMap.addPolygon(boundaries);

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
               return;
            }
            else if(requestCode==SETDESTTOCURLOC){
                getRide(null);
            }
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

                pickup = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                if(currentLocation==null){
                    Toast.makeText(this, "Unable to get your location. Please enter pick up location manually", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            String json = "{\"picklat\":"+new Double(pickup.latitude)
                    +",\"picklong\":"+new Double(pickup.longitude)
                    +",\"destlat\":"+new Double(dest.latitude)
                    +",\"destlong\":"+new Double(dest.longitude)+"}";

            callServer(REQUEST_RIDE_URL, json, "POST");

        }
        else{
            Toast.makeText(this, "Destination is required for ride request", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected: ");
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
