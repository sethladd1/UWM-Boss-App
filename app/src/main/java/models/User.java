package models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.GsonBuilder;

/**
 * Created by jrhayman on 4/21/17.
 */

public class User implements Parcelable{
    private static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>(){

        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int user_id;
    public String user_name, token;
    public boolean is_driver, is_admin;

    private User(Parcel in){
        user_id = in.readInt();
        user_name = in.readString();
        is_driver = (boolean) in.readValue(boolean.class.getClassLoader());
        is_admin = (boolean) in.readValue(boolean.class.getClassLoader());
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(user_id);
        dest.writeString(user_name);
        dest.writeString(token);
        dest.writeValue(is_driver);
        dest.writeValue(is_admin);
    }

    public User(String user_name, String token, boolean is_driver, boolean is_admin) {
        this.user_name = user_name;
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
        return user_name;
    }
    public String getToken(){
        return token;
    }
    public void setToken(String t){
        token = t;
    }


}
