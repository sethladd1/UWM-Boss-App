package models;

import android.content.SharedPreferences;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.util.Map;

/**
 * Created by jrhayman on 4/16/17.
 */

public class Driver {
    private String username;
    private int user_id;
    private int van_id;
    private LatLng location;

    /**
     *
     * @param username
     * @param user_id
     * @param van_id
     */
    public Driver(String un, int uid, int vid){
            username = un;
            user_id = uid;
            van_id = vid;
    }




}
