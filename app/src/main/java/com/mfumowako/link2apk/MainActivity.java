package com.mfumowako.link2apk;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private WebView myWebView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout offlineLayout;
    private Button btnRetry;
    
    private static final String TARGET_URL = "https://www.google.com"; 
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int FILECHOOSER_RESULTCODE = 1;
    private ValueCallback<Uri[]> mUploadMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Kuanzisha vishikwambi
        myWebView = findViewById(R.id.webview);
        progressBar = findViewById(R.id.loading_bar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        offlineLayout = findViewById(R.id.offline_layout);
        btnRetry = findViewById(R.id.btn_retry);
        FloatingActionButton btnCamera = findViewById(R.id.btn_camera);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Kuomba Ruhusa za kawaida
        checkAndRequestPermissions();

        // Kusanidi WebView
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setGeolocationEnabled(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);

        // Mfumo wa Kudownload Mafaili
        myWebView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimetype);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.addRequestHeader("User-Agent", userAgent);
                request.setDescription("Inapakua faili...");
                request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimetype));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimetype));
                
                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                if (dm != null) {
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), "Upakuaji umeanza... Kagua juu kwenye bango!", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Imeshindwa kupakua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Kudhibiti Loading na Offline
        myWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                progressBar.setVisibility(View.VISIBLE);
                offlineLayout.setVisibility(View.GONE);
                myWebView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                OnyeshaUkurasaWaOffline();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    OnyeshaUkurasaWaOffline();
                }
            }
        });

        // Kudhibiti maombi ya Kamera, Location na Kupandisha Mafaili (Upload)
        myWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(final PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (mUploadMessage != null) {
                    mUploadMessage.onReceiveValue(null);
                }
                mUploadMessage = filePathCallback;
                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, FILECHOOSER_RESULTCODE);
                } catch (Exception e) {
                    mUploadMessage = null;
                    return false;
                }
                return true;
            }
        });

        // TUPO HAPA: Kitufe sasa hivi kinafungua Kamera kamili ya mfumo (Picha + Video)
        btnCamera.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // Hii amri inafungua App nzima ya Kamera ili mtumiaji achague kupiga picha au kurekodi video
                Intent cameraIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
                startActivity(cameraIntent);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            }
        });

        // Swipe to Refresh
        swipeRefresh.setOnRefreshListener(() -> myWebView.reload());

        // Retry Button
        btnRetry.setOnClickListener(v -> {
            offlineLayout.setVisibility(View.GONE);
            myWebView.setVisibility(View.VISIBLE);
            myWebView.reload();
        });

        // Bottom Navigation
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

        // KUWASHA MFUMO WA ALAMA YA KIDOLE / PIN
        KaguaUlinziWaBiometric();
    }

    private void KaguaUlinziWaBiometric() {
        BiometricManager biometricManager = BiometricManager.from(this);
        int authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL;

        if (biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                    finish();
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    runOnUiThread(() -> myWebView.loadUrl(TARGET_URL));
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Thibitisha Utambulisho")
                    .setSubtitle("Weka alama ya kidole au PIN kufungua App")
                    .setAllowedAuthenticators(authenticators)
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } else {
            myWebView.loadUrl(TARGET_URL);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (mUploadMessage == null) return;
            mUploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            mUploadMessage = null;
        }
    }

    private void OnyeshaUkurasaWaOffline() {
        myWebView.setVisibility(View.GONE);
        offlineLayout.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
    }

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
