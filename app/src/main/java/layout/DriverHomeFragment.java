package layout;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    private LocationRequest loc_request;
    private float[] mParam1;
    private GoogleMap map;
    private OnDriverPairedListener mListener;
    private LocationManager loc_manager;
    private static final int GET_LOC_PERMISSION = 25;
    private GoogleApiClient api_client;


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
        loc_manager = ( LocationManager) this.getActivity().getSystemService(LOCATION_SERVICE);
        if (getArguments() != null) {
            mParam1 = getArguments().getFloatArray(DRIVER_LOC_ARG);
            this.getMapAsync(this);
        }
    }

//    @Override
//    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults){
//        //TODO figure out how to make this function work in this class,
//        //TODO or add specific request code to MainDriverActivity
//    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_driver_home, container, false);
    }


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
                buildGoogleApiClient();
            }else{
                checkLocationPermissions();
            }
        }else{
            map.setMyLocationEnabled(true);
            buildGoogleApiClient();
        }

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
        loc_request = new LocationRequest();
        loc_request.setInterval(5000);
        loc_request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        loc_request.setFastestInterval(1000);
        loc_request.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(api_client, loc_request, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions marker_options = new MarkerOptions();
        marker_options.position(ltlng);
        marker_options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        map.addMarker(marker_options);
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


    public void createNewRide(Ride ride){
        //TODO:
    }

    public void removePassenger(){
        //TODO:
    }

    /**
     * Interface to communicate with activity
     */
    public interface OnDriverPairedListener {
        void newRide();
        void canceledRide();
        void finishRide();
    }
}
