package com.mfumowako.link2apk;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout offlineLayout;
    private Button btnRetry;
    
    private static final String TARGET_URL = "https://www.google.com"; 
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kuanzisha vishikwambi (Views)
        myWebView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.loading_bar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        offlineLayout = findViewById(R.id.offline_layout);
        btnRetry = findViewById(R.id.btn_retry);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Kuomba Ruhusa zote za Simu zilizotakiwa mara tu App ikifunguka
        checkAndRequestPermissions();

        // Kusanidi mifumo ya kisasa ya WebView
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true); // Ruhusa ya Location ndani ya tovuti
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Kudhibiti Loading bar na Ukurasa wa Offline
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                offlineLayout.setVisibility(View.GONE); // Ficha kosa wakati inaanza upya
                myWebView.setVisibility(View.VISIBLE); // <--- HAPA TUMESAFHISHA SIKU HIZI!
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false); // Zima ule mduara wa swipe refresh
            }

            // Kukamata makosa ya mtandao kwenye simu za zamani
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                OnyeshaUkurasaWaOffline();
            }

            // Kukamata makosa ya mtandao kwenye simu za kisasa (Android 6.0+)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    OnyeshaUkurasaWaOffline();
                }
            }
        });

        // Kudhibiti maombi ya Kamera, Sauti na Location kutoka kwenye Tovuti yenyewe
        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });

        // Kufungua Link kuu
        myWebView.loadUrl(TARGET_URL);

        // Mfumo wa Kuvuta chini ili kusasisha (Swipe to Refresh)
        swipeRefresh.setOnRefreshListener(() -> myWebView.reload());

        // Kitufe cha Jaribu tena cha ukurasa wa offline
        btnRetry.setOnClickListener(v -> {
            offlineLayout.setVisibility(View.GONE);
            myWebView.setVisibility(View.VISIBLE);
            myWebView.reload();
        });

        // Kudhibiti vifungo vya chini
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

    private void OnyeshaUkurasaWaOffline() {
        myWebView.setVisibility(View.GONE);
        offlineLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

    // Mtambo wa Kiotomatiki wa kuomba Ruhusa zote kwa usalama
    private void checkAndRequestPermissions() {
        List<String> listPermissionsNeeded = new ArrayList<>();
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onBackPressed() {
        if (myWebView.canGoBack()) {
            myWebView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
