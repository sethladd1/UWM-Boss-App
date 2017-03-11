package com.uwmbossapp.uwmboss;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebLogin extends AppCompatActivity{

        private String url = "https://uwm-boss.com/saml/sso";

    private WebView webView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_login);
        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.clearCache(true);

        webView.loadUrl(url);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if(url.trim().equals("https://uwm-boss.com/saml/acs")){
                    successfulAuthentication();
                }
            }
        });

    }


    private String getCookie(String url, String cookieName){

        CookieManager cookieManager = CookieManager.getInstance();
        String cookies = cookieManager.getCookie(url);
        String cookieValue = null;
        String[] temp=cookies.split(";");
        for (String ar1 : temp ){
            if(ar1.contains(cookieName)){
                String[] temp1=ar1.split("=");
                cookieValue = temp1[1];
            }
        }
        return cookieValue;
    }
    public void successfulAuthentication(){
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
