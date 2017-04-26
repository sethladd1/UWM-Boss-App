package models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by jrhayman on 4/22/17.
 */

public class Ride {

    int user_id, driver_id;
    float picklat, picklong, destlat, destlong;

    public Ride(int user_id, int driver_id, float plat, float plong, float dlat, float dlong){
        this.user_id = user_id;
        this.driver_id = driver_id;
        this.picklat = plat;
        this.picklong = plong;
        this.destlat = dlat;
        this.destlong = dlong;
    }

    public static Ride fromJSON(String response){
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(response, Ride.class);
    }

    public static String toJson(Ride ride){
        return new GsonBuilder().create().toJson(ride, Ride.class);
    }

    public void setDriver(int driver_id) {
        this.driver_id = driver_id;
    }


}
