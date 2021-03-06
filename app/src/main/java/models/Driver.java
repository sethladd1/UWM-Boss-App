package models;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jrhayman on 4/16/17.
 */

public class Driver implements Parcelable {
    public int user_id;
//    public int van_id;
    @SerializedName("lat") public float loclat;
    @SerializedName("long") public float loclong;
    public boolean available;
    public static final Parcelable.Creator<Driver> CREATOR = new Parcelable.Creator<Driver>(){

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
        user_id = in.readInt();
        loclat = in.readFloat();
        loclong = in.readFloat();
        available = (boolean)in.readValue(boolean.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(user_id);
        dest.writeFloat(loclat);
        dest.writeFloat(loclong);
        dest.writeValue(available);
    }


    /**
     * @param uid
     * @param location
     */
    public Driver(int uid, Location location){
        user_id = uid;
        loclat = (float)location.getLatitude();
        loclong = (float)location.getLongitude();
        available = true;
    }

    public boolean isAvailable(){
        return available;
    }

//    public int getVanID(){
//        return van_id;
//    }

    public int getUserID(){
        return user_id;
    }
//    public String getUserName(){
//        return username;
//    }
    public void setAvailability(boolean avail){
        available = avail;
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
    public float[] getLocation(){
        return new float[]{loclat, loclong};
    }
}
