package com.uwmbossapp.uwmboss;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class AccountActivity extends AppCompatActivity {
    private TextView user;
    private TextView accountType;
    private static final String TAG = "AccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        Toolbar toolbar = (Toolbar) findViewById(R.id.mToolbar);

        setSupportActionBar(toolbar);
        ActionBar ab = getSupportActionBar();
        if(ab!=null){
            ab.setTitle("USER ACCOUNT");
            ab.setDisplayHomeAsUpEnabled(true);
        }
        user = (TextView) findViewById(R.id.userText);
        accountType = (TextView) findViewById(R.id.accountTypeText);
        String accountInfo = getIntent().getStringExtra("accountInfo");
        try {
            JSONObject jsonObject = new JSONObject(accountInfo);
            String email = jsonObject.getString("email");
            boolean driver = jsonObject.getBoolean("is_driver");
            if (email != null) {
                String[] arr = email.split("@");
                if (arr.length > 0) {
                    user.setText("Logged in as " + arr[0]);

                }
                if (driver) {
                    accountType.setText("Driver Account");
                } else {
                    accountType.setText("Passenger Account");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void logOut(View v) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("logout", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
}
