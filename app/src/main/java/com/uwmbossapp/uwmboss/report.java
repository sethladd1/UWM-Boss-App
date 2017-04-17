package com.uwmbossapp.uwmboss;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;

public class report extends AppCompatActivity {

    private Chronometer simpleChronometer;
boolean waitingForRide = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Chronometer simpleChronometer = (Chronometer) findViewById(R.id.simpleChronometer); // initiate a chronometer
        Intent intent = getIntent();
        waitingForRide = intent.getBooleanExtra("waitingForRide", false);
        if(waitingForRide)
            simpleChronometer.start(); // start a chronometer
        simpleChronometer.setFormat("Time Running - %s"); // set the format for a chronometer
    }
    public void cancelRide(View v){
        Intent resultIntent = new Intent();
        resultIntent.putExtra("rideCancelled", true);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

}
