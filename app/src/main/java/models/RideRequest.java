package models;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by jrhayman on 4/22/17.
 */

public class RideRequest {
    float picklat, picklong, destlat, destlong;

    public RideRequest(@NonNull float plat, @NonNull float plong, @NonNull float dlat, @NonNull float dlong){
        picklat = plat;
        picklong = plong;
        destlat = dlat;
        destlong = dlong;
    }

    public static String toJSON(RideRequest rq){
        return new GsonBuilder().create().toJson(rq, RideRequest.class);
    }
    public static RideRequest fromJSON(String content){
        return new GsonBuilder().create().fromJson(content, RideRequest.class);
    }
}
