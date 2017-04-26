package com.uwmbossapp.uwmboss;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.uwmbossapp.uwmboss.R;
import com.uwmbossapp.uwmboss.utils.HttpHelper;
import com.uwmbossapp.uwmboss.utils.NetworkHelper;

import models.Driver;
import models.User;

public class DriverLogin extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Intent intent;
    private EditText edit_text;
    private User user;
    private Location location;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        user = getIntent().getExtras().getParcelable("user");
        location = getIntent().getExtras().getParcelable("location");
        edit_text = (EditText) findViewById(R.id.van_id_edit_text);
        intent = new Intent(this, MainDriverActivity.class);
        ((Button) findViewById(R.id.van_login_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = edit_text.getText().toString();
                if(!input.isEmpty()) {
                    intent.putExtra("driver", new Driver(user.user_name, user.user_id, Integer.getInteger(input), location));
                    startActivity(intent);
                }else{
                    Toast.makeText(DriverLogin.this, "please input vanID", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
