package com.uwmbossapp.uwmboss;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import com.uwmbossapp.uwmboss.services.MyService;

public class RateDriverActivity extends AppCompatActivity {
    RatingBar ratingBar;
    final static String RATINGSURL = "https://uwm-boss.com/admin/users/rate?rating=";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate_driver);
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setNumStars(5);
        ratingBar.setRating(3);
        ratingBar.setStepSize(1);
    }

    public void onCancel(View v){
        this.finish();
    }
    public void onAccept(View v){
        Toast.makeText(this, "Thanks for the Feedback", Toast.LENGTH_SHORT).show();
        callServer(RATINGSURL+ ratingBar.getRating(), null, "GET");
        this.finish();
    }
    public void callServer(String url, String message, String requestType) {


        Intent intent = new Intent(this, MyService.class);
        intent.setData(Uri.parse(url));
        intent.putExtra("message", message);
        intent.putExtra("requestType", requestType);
        startService(intent);

    }
}
