package models;

import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.util.Map;

/**
 * Created by jrhayman on 4/16/17.
 */

public class Driver implements Parcelable {
    String username;
    int user_id;
    int van_id;
    @SerializedName("lat")  float loclat;
    @SerializedName("long")  float loclong;
    boolean isAvailable;
    static final Parcelable.Creator<Driver> CREATOR = new Parcelable.Creator<Driver>(){

        @Override
        public Driver createFromParcel(Parcel source) {
            //return Driver.fromJSON(source.readString());
            return new Driver(source);
        }

        @Override
        public Driver[] newArray(int size) {
            return new Driver[size];
        }
    };

    private Driver(Parcel in){
        username = in.readString();
        user_id = in.readInt();
        van_id = in.readInt();
        loclat = in.readFloat();
        loclong = in.readFloat();
        isAvailable = (boolean)in.readValue(boolean.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeInt(user_id);
        dest.writeInt(van_id);
        dest.writeFloat(loclat);
        dest.writeFloat(loclong);
        dest.writeValue(isAvailable);
    }


    /**
     * @param un
     * @param uid
     * @param vid
     * @param location
     */
    public Driver(String un, int uid, int vid, Location location){
        username = un;
        user_id = uid;
        van_id = vid;
        loclat = (float)location.getLatitude();
        loclong = (float)location.getLongitude();
        isAvailable = true;
    }

    public boolean isAvailable(){
        return isAvailable;
    }
    public int getVanID(){
        return van_id;
    }
    public int getUserID(){
        return user_id;
    }
    public String getUserName(){
        return username;
    }
    public void setAvailability(boolean avail){
        isAvailable = avail;
    }
    public void setLocation(Location location){
        loclat = (float)location.getLatitude();
        loclong = (float)location.getLongitude();
    }

    public static String toJSON(Driver driver){
        return new GsonBuilder().create().toJson(driver, Driver.class);
    }

    public static Driver fromJSON(String content){
        return new GsonBuilder().create().fromJson(content, Driver.class);
    }
}
