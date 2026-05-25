package com.mfumowako.link2apk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;
    private ProgressBar progressBar;
    // HAPA NDIPO LINK INAPOKAA. GitHub Actions itakuwa inabadilisha hii link kiotomatiki
    private static final String TARGET_URL = "https://www.google.com"; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myWebView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.loading_bar);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Kusanidi mifumo ya kisasa ya WebView (Android 14+ Safe)
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Kudhibiti loading bar na kufungua link ndani ya App
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE); // Onyesha loading ikianza
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE); // Ficha loading ikimaliza
            }
        });

        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressBar.setProgress(newProgress);
            }
        });

        // Kufungua hiyo Link kuu
        myWebView.loadUrl(TARGET_URL);

        // Kufanya vifungo vya chini (Bottom Navigation) vifanye kazi
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                myWebView.loadUrl(TARGET_URL);
                return true;
            } else if (id == R.id.nav_refresh) {
                myWebView.reload();
                return true;
            } else if (id == R.id.nav_share) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Angalia app hii nzuri: " + myWebView.getUrl());
                startActivity(Intent.createChooser(shareIntent, "Share kupitia"));
                return true;
            }
            return false;
        });
    }

    // Mtumiaji akibonyeza 'Back' ya simu, irudi nyuma kwenye tovuti badala ya kufunga app
    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
  }
