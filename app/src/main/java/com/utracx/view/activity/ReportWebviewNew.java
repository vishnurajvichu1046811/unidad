package com.utracx.view.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.utracx.R;

public class ReportWebviewNew extends AppCompatActivity {
    public static WebView webview;
    String url = "http://web.utracx.com:8080/utracx/report?username=admin&password=Utrack1%23";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_webview_);

        webview = findViewById(R.id.webview_loader);


        WebSettings webSettings = webview.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        webview.setVisibility(View.VISIBLE);

        webview.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                new Handler().postDelayed(() ->{
                    webview.loadUrl(JavaScriptInterface.getBase64StringFromBlobUrl(url));
                } ,0);

            }
        });

        webview.getSettings().setJavaScriptEnabled(true);
        webview.addJavascriptInterface(new JavaScriptInterface(this), "Android");
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


        new Handler().postDelayed(() ->{
            webview.loadUrl((url)); } ,0);
    }


}
