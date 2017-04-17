package models;

import android.content.SharedPreferences;
import android.location.Geocoder;

import java.util.Map;

/**
 * Created by jrhayman on 4/16/17.
 */

public class Driver {
    private String username;
    private int user_id;
    private int van_id;
    private String name;
    private Geocoder location;

    public Driver(SharedPreferences sp){
        Map<String, ?> map = sp.getAll();
        username = (String) map.get("username");
        van_id = (int) map.get("van_id");

    }
}
