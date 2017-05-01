package com.uwmbossapp.uwmboss;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.uwmbossapp.uwmboss.R;
import com.uwmbossapp.uwmboss.services.MyService;
import com.uwmbossapp.uwmboss.utils.HttpHelper;
import com.uwmbossapp.uwmboss.utils.NetworkHelper;

import models.Driver;
import models.User;

public class DriverLogin extends AppCompatActivity {

    private final static String SHOW_DRIVER_URL = "https://uwm-boss.com/admin/drivers/show";
    private EditText edit_text;
    private User user;
    private Location location;
    private String input;
    private Button login_button;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        user = getIntent().getExtras().getParcelable("user");
        location = getIntent().getExtras().getParcelable("location");
        edit_text = (EditText) findViewById(R.id.van_id_edit_text);
//        input = findViewById(R.id.van_id_edit_text).;
        login_button = (Button) findViewById(R.id.van_login_button);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DriverLogin.this.input = DriverLogin.this.edit_text.getText().toString();
                //driverLogin();
                startActivity(new Intent(DriverLogin.this, MainDriverActivity.class).putExtra("driver", new Driver(user.id, location)));


            }
        });
//        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(new LoginBroadcastReceiver(), new IntentFilter(MyService.MY_SERVICE_MESSAGE));
    }
    private void driverLogin(){
        callServer(SHOW_DRIVER_URL, null, "GET");
    }

    public void callServer(String url, String message, String requestType) {
        Intent intent = new Intent(this, MyService.class);
        intent.setData(Uri.parse(url));
        intent.putExtra("message", message);
        intent.putExtra("requestType", requestType);
        startService(intent);
    }

//    private class  LoginBroadcastReceiver extends BroadcastReceiver{
//
//        private Driver driver;
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String content = intent.getStringExtra(MyService.MY_SERVICE_PAYLOAD);
//            if(content == null){
//                //create driver
//                try{
//                    driver = new Driver(user.id, location);
//                    new Intent(DriverLogin.this, MainDriverActivity.class).putExtra("driver", driver);
//                    intent.putExtra("driver", driver);
//                    startActivity(intent);
//                }catch (JsonParseException e){
//                    Toast.makeText(DriverLogin.this, "cannot make object JSON", Toast.LENGTH_SHORT).show();
//                }
//            }
//            if(content.trim().equals("null")){
//                //create driver
//                try{
//                    driver = new Driver(user.id, location);
//                    callServer("https://uwm-boss.com/admin/drivers/", Driver.toJSON(driver), "POST");
//                    startActivity(new Intent(DriverLogin.this, MainDriverActivity.class).putExtra("driver", driver));
//                }catch (JsonParseException e){
//                    Toast.makeText(DriverLogin.this, "cannot make object JSON", Toast.LENGTH_SHORT).show();
//                }
//            }else{
//                //driver is already in table
//                if(content.equals("true")){
//                        Log.i("LoginBroadcastManager", "JSON: " +content);
//                        startActivity(new Intent(DriverLogin.this, MainDriverActivity.class).putExtra("driver", driver));
//                }
//                try{
//                    Log.i("LoginBroadcastManager", "JSON: " +content);
//                    startActivity(new Intent(DriverLogin.this, MainDriverActivity.class).putExtra("driver", Driver.fromJSON(content)));
//                }catch (JsonParseException e){
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    }
}

