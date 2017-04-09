package com.uwmbossapp.uwmboss;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Chronometer;

public class report extends AppCompatActivity {

    private Chronometer simpleChronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        Chronometer simpleChronometer = (Chronometer) findViewById(R.id.simpleChronometer); // initiate a chronometer

        simpleChronometer.start(); // start a chronometer
        simpleChronometer.setFormat("Time Running - %s"); // set the format for a chronometer
    }
}
