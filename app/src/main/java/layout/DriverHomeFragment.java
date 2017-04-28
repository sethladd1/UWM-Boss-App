package layout;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.uwmbossapp.uwmboss.R;

import models.Ride;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DriverHomeFragment.OnDriverPairedListener} interface
 * to handle interaction events.
 * Use the {@link DriverHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DriverHomeFragment extends SupportMapFragment
        implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private static final String DRIVER_LOC_ARG = "param1";
    public static final float SMALLEST_DISPLACEMENT = 137f;
    //    private LocationRequest loc_request;
    private float[] mParam1;
    private GoogleMap map;
    private OnDriverPairedListener mListener;
    private static final int GET_LOC_PERMISSION = 25;
    private GoogleApiClient api_client;
    private RelativeLayout ride_buttons_layout;
    private RelativeLayout navigation_button_layout;
    private Button ride_pickup_location_button;
    private Button ride_destination_location_button;
    private Button map_navigation_button;
    private Marker pickup_marker;
    private Marker dest_marker;

    public DriverHomeFragment() {/** Required empty public constructor*/}


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * realizing I dont need Params for this fragment...
     *
     * @param param1 float[2] {Lat, Long}.
     * @return A new instance of fragment DriverHomeFragment.
     */
    public static DriverHomeFragment newInstance(float[] param1) {
        DriverHomeFragment fragment = new DriverHomeFragment();
        Bundle args = new Bundle();
        args.putFloatArray(DRIVER_LOC_ARG, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getFloatArray(DRIVER_LOC_ARG);
        }
        this.getMapAsync(this);
    }

//    @Override
//    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
//        //TODO figure out how to make this function work in this class,
//        //TODO or add specific request code to MainDriverActivity
//    }
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_driver_home, container, false);
//    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDriverPairedListener) {
            mListener = (OnDriverPairedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDriverPairedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void checkLocationPermissions(){
        if (ActivityCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, GET_LOC_PERMISSION);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng location = new LatLng(mParam1[0], mParam1[1]);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location,12);
        map.animateCamera(update);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ){
                map.setMyLocationEnabled(true);
                buildGoogleApiClient();
            }else{
                checkLocationPermissions();
            }
        }else{
            map.setMyLocationEnabled(true);
            buildGoogleApiClient();
        }
//        MarkerOptions marker_options = new MarkerOptions();
//        marker_options.position(location);
//        marker_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//        map.addMarker(marker_options);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12));


    }

    protected synchronized void buildGoogleApiClient(){
        api_client = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        api_client.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(api_client, new LocationRequest()
                            .setInterval(5000)
                            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            .setFastestInterval(1000)
                            .setSmallestDisplacement(SMALLEST_DISPLACEMENT),
                    this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
//        MarkerOptions marker_options = new MarkerOptions();
//        marker_options.position(ltlng);
//        marker_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
//        map.addMarker(marker_options);
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(ltlng, 14));
    }

    @Override
    public void onConnectionSuspended(int i){
        //TODO
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO:
    }

    private void generateLocationButtons(float picklat, float picklong, float destlat, float destlong){
        ride_buttons_layout = new RelativeLayout(this.getContext());
        navigation_button_layout = new RelativeLayout(this.getContext());

        RelativeLayout.LayoutParams ride_buttons_layout_params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams navigation_button_layout_params = new RelativeLayout.LayoutParams
                (RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        ride_buttons_layout_params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        navigation_button_layout_params.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        ride_buttons_layout.setLayoutParams(ride_buttons_layout_params);
        navigation_button_layout.setLayoutParams(navigation_button_layout_params);

        ride_pickup_location_button = new Button(this.getContext());
        ride_destination_location_button = new Button(this.getContext());
        map_navigation_button = new Button(this.getContext());

        RelativeLayout.LayoutParams pickup_layout_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams dest_layout_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams navigation_layout_params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        pickup_layout_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        dest_layout_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        navigation_layout_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

        ride_pickup_location_button.setLayoutParams(pickup_layout_params);
        ride_destination_location_button.setLayoutParams(dest_layout_params);
        map_navigation_button.setLayoutParams(navigation_layout_params);

        ride_pickup_location_button.setBackgroundColor(Color.GREEN);
        ride_pickup_location_button.setText("P");
        ride_pickup_location_button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ride_pickup_location_button.setTextColor(Color.DKGRAY);
        ride_destination_location_button.setBackgroundColor(Color.RED);
        ride_destination_location_button.setText("D");
        ride_destination_location_button.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        ride_destination_location_button.setTextColor(Color.DKGRAY);
        map_navigation_button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_navigation, null));

        ride_pickup_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(pickup_marker.getPosition(), 12f));
            }
        });
        ride_destination_location_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO:
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(dest_marker.getPosition(), 12f));
            }
        });
        map_navigation_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    String saddr = "saddr=";
                    String daddr = "daddr=";
                    String googleMapsNavigationURL = "https://maps.google.com/maps?";
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(api_client);
                        if (pickup_marker != null) {
                            googleMapsNavigationURL += saddr + ""+currentLocation.getLatitude()+ ","+currentLocation.getLongitude()+ "&"
                                    + daddr + "" + pickup_marker.getPosition().latitude + "," + pickup_marker.getPosition().longitude + "";
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsNavigationURL)));

                        } else if (dest_marker != null) {
                            googleMapsNavigationURL += saddr + "" + currentLocation.getLatitude()+ ","+currentLocation.getLongitude()+ "&"
                                    + daddr + "" + dest_marker.getPosition().latitude + "," + dest_marker.getPosition().latitude + "";
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsNavigationURL)));
                        } else {
                            Toast.makeText(DriverHomeFragment.this.getContext(), "Can't Navigate without a PickUp or Destination", Toast.LENGTH_SHORT).show();
                        }
                    }
            }

        });

    }


    public void createNewRide(Ride ride){
        pickup_marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(ride.picklat, ride.picklong))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("PickUp"));
        dest_marker = map.addMarker(new MarkerOptions()
                .position(new LatLng(ride.destlat, ride.destlong))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("Destination"));
        generateLocationButtons(ride.picklat, ride.picklong, ride.destlat, ride.destlong);
    }

    public void removePassenger(){
        //TODO: remove location buttons
    }
    public void finishRide(){
        //TODO:
    }

    /**
     * Interface to communicate with activity
     */
    public interface OnDriverPairedListener {
        void finishRide();
    }
}
