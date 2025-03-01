package com.autotest.sonicclient.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class ServiceBase extends Service {
    private static final String TAG = "ServiceBase";
    public static boolean isRunning = false;

    public ServiceBase() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }
}