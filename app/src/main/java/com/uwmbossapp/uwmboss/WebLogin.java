package com.uwmbossapp.uwmboss;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebLogin extends AppCompatActivity{

    private String url = "https://google.com";
    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_web_login);
        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        MyClient client = new MyClient();

        webView.loadUrl(url);
        webView.setWebViewClient(client);



    }
    public void successfulAuthentication(){
//        TODO: pass auth info back to main activity
        this.finish();
    }

    private class MyClient extends WebViewClient {


    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
//        TODO: Not sure if this is exactly what the url will look like. Check.
        if(url.trim() == "https://www.uwm-boss.com/saml/acs")
            successfulAuthentication();
    }
}
}
