package com.agh.iot.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class WebSearch extends AppCompatActivity {

    private final String URL = "http://192.168.10.1/";

    private WebView webView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_search);

        webView = findViewById(R.id.web_view);

        initWebView();
    }

    private void initWebView() {
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(URL);
    }


}