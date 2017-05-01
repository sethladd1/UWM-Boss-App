package models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.GsonBuilder;

/**
 * Created by jrhayman on 4/21/17.
 */

public class User implements Parcelable{
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>(){

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int id;
    public String email, token;
    public boolean is_driver, is_admin;

    private User(Parcel in){
        id = in.readInt();
        email = in.readString();
        token = in.readString();
        boolean[] bool_array = new boolean[2];
        in.readBooleanArray(bool_array);
        is_driver = bool_array[0];
        is_admin = bool_array[1];
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(email);
        dest.writeString(token);
        dest.writeBooleanArray(new boolean[]{is_driver, is_admin});

    }

    public User(String email, String token, boolean is_driver, boolean is_admin) {
        this.email = email;
        this.token = token;
        this.is_driver = is_driver;
        this.is_admin = is_admin;
    }

    public static String toJSON(User user){
        return new GsonBuilder().create().toJson(user, User.class);
    }
    public static User fromJSON(String content){
        return new GsonBuilder().create().fromJson(content, User.class);
    }

    public Boolean isDriver(){
        return is_driver;
    }
    public Boolean isAdmin(){
        return is_admin;
    }
    public String getUserName(){
        return email;
    }
    public String getToken(){
        return token;
    }
    public void setToken(String t){
        token = t;
    }


}
