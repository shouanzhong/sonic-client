package com.autotest.sonicclient.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.autotest.sonicclient.activities.SuitActivity;
import com.autotest.sonicclient.services.RunService;

public class StopServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, RunService.class);
        context.stopService(serviceIntent);
    }
}