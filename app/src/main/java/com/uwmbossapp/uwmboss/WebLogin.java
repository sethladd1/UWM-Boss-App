package com.uwmbossapp.uwmboss;


import android.content.Intent;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.CookieHandler;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;


public class WebLogin extends AppCompatActivity{

        private String url = "https://uwm-boss.com/saml/sso";

    private WebView webView;
    private String TAG = "WebLogin";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_login);


        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);


        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient(){

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.trim().equals("https://uwm-boss.com/saml/acs")){
                    successfulAuthentication();
                }
                Log.i(TAG, "onPageFinished: "+ url);
            }


        });

    }


    private String getCookie(String url, String cookieName){

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(url);
        String cookieValue = null;
        Log.i(TAG, "getCookie: " + cookies);
        if(cookies !=null) {
            String[] temp = cookies.split(";");
            for (String ar1 : temp) {
                if (ar1.contains(cookieName)) {
                    String[] temp1 = ar1.split("=");
                    cookieValue = temp1[1];
                }
            }
        }
        return cookieValue;
    }
    private void successfulAuthentication(){
        Intent resultIntent = new Intent();
        String token = getCookie("uwm-boss.com", "token");
        if (token != null) {
            resultIntent.putExtra("token", token.trim());

            setResult(RESULT_OK, resultIntent);
        }
        else
        {
            setResult(RESULT_CANCELED);
        }
        finish();
    }



}
