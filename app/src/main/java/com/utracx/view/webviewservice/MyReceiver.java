package com.utracx.view.webviewservice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.utracx.view.activity.ReportWebview_A;


public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String status = com.utracx.view.webviewservice.NetworkUtil.getConnectivityStatusString(context);

        if(status.isEmpty()) {
            status="No Internet Connection";
            ReportWebview_A.webview.setEnabled(false);
            ReportWebview_A.webview.setClickable(false);


        }
        else
            if(status.equalsIgnoreCase("No internet is available")){
                ReportWebview_A.webview.setEnabled(false);
                ReportWebview_A.webview.setClickable(false);
            }
            else
            {
                ReportWebview_A.webview.setEnabled(true);
                ReportWebview_A.webview.setClickable(true);
            }
    }
}