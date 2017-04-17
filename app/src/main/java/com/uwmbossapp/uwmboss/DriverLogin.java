package com.uwmbossapp.uwmboss;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.uwmbossapp.uwmboss.R;
import com.uwmbossapp.uwmboss.utils.HttpHelper;
import com.uwmbossapp.uwmboss.utils.NetworkHelper;

public class DriverLogin extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private Intent intent;
    private EditText edit_text;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);
        edit_text = (EditText) findViewById(R.id.van_id_edit_text);
        ((Button) findViewById(R.id.van_login_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }
}
