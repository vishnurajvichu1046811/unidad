package com.utracx.view.activity;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.utracx.R;
import com.utracx.database.datamodel.UserDataEntity;
import com.utracx.viewmodel.DashBoardViewModel;

import java.net.URI;

import static com.utracx.util.ConstantVariables.REPORTS_WEB_URL;
import static com.utracx.util.helper.SharedPreferencesHelper.getUserEmail;
import static java.security.AccessController.getContext;

public class ReportWebview_A extends AppCompatActivity {

    private BroadcastReceiver MyReceiver = null;

    public static WebView webview;
    SwipeRefreshLayout refreshLayout;
    LinearLayout netError;
    Button retry;
    String url;
    Boolean network;
    ProgressBar pbar;
    ActionBar actionBar;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_webview_);

        webview = findViewById(R.id.webview_loader);
        refreshLayout = findViewById(R.id.refresh);
        netError = findViewById(R.id.ll_net_error);
        retry = findViewById(R.id.btn_retry);
        pbar = findViewById(R.id.pbar);
        actionBar = getSupportActionBar();
        refreshLayout.setRefreshing(true);
        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        url = "http://web.utracx.com:8080/utracx/report?username=admin&password=Utrack1%23";

        webload();
        webview.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView wv, String url) {
                if (url.startsWith("upi:")) {

                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }catch (Exception e){

                    }

                    return true;
                }
                return false;
            }
        });


        MyReceiver = new com.utracx.view.webviewservice.MyReceiver();
        broadcastIntent();

        refreshLayout.setOnRefreshListener( new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webload();
            }
        });

        retry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pbar.setVisibility(View.VISIBLE);
                webview.setActivated(true);
                webload();


            }
        });

       browserSettings();
    }

    public void webload()

    {
        Toast.makeText(getApplicationContext(), "Loading", Toast.LENGTH_SHORT).show();
        pbar.setVisibility(View.GONE);
        refreshLayout.setRefreshing(false);
        if (isNetworkConnected()) {
            netError.setVisibility(View.GONE);
            webview.setVisibility(View.VISIBLE);
            webview.loadUrl((url));
        } else {
            webview.setVisibility(View.GONE);
            netError.setVisibility(View.VISIBLE);
        }
    }


    //CLASS CALLBACK

    public class Callback extends WebViewClient {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);

            int errorCode = error.getErrorCode();
            Log.e("","");
            if(errorCode == Callback.ERROR_HOST_LOOKUP){
                webview.destroy();
                netError.setVisibility(View.VISIBLE);
            }
            else{
                netError.setVisibility(View.GONE);
            }

        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            netError.setVisibility(View.GONE);
            pbar.setVisibility(View.GONE);
        }
    }

@SuppressLint("SetJavaScriptEnabled")
private void browserSettings() {
    webview.getSettings().setJavaScriptEnabled(true);
    webview.setDownloadListener(new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
            webview.loadUrl(getBase64StringFromBlobUrl(url));
        }
    });

}

    public static String getBase64StringFromBlobUrl(String blobUrl) {
        if(blobUrl.startsWith("blob")){
            return "javascript: var xhr = new XMLHttpRequest();" +
                    "xhr.open('GET', '"+ blobUrl +"', true);" +
                    "xhr.setRequestHeader('Content-type','application/pdf');" +
                    "xhr.responseType = 'blob';" +
                    "xhr.onload = function(e) {" +
                    "    if (this.status == 200) {" +
                    "        var blobPdf = this.response;" +
                    "        var reader = new FileReader();" +
                    "        reader.readAsDataURL(blobPdf);" +
                    "        reader.onloadend = function() {" +
                    "            base64data = reader.result;" +
                    "            Android.getBase64FromBlobData(base64data);" +
                    "        }" +
                    "    }" +
                    "};" +
                    "xhr.send();";
        }
        return "javascript: console.log('It is not a Blob URL');";
    }



    //END---------------
    public void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();

    }
    public void onBackPressed()
    {
        if (webview.canGoBack()) {
            webview.goBack();
        }
        else{
            super.onBackPressed();
        }
    }

    public static class NetworkUtil {
        public static final int TYPE_WIFI = 1;
        public static final int TYPE_MOBILE = 2;
        public static final int TYPE_NOT_CONNECTED = 0;
        public static final int NETWORK_STATUS_NOT_CONNECTED = 0;
        public static final int NETWORK_STATUS_WIFI = 1;
        public static final int NETWORK_STATUS_MOBILE = 2;

    }

}