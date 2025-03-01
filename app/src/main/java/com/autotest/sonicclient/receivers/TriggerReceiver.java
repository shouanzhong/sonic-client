package com.autotest.sonicclient.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.PermissionHelper;

public class TriggerReceiver extends BroadcastReceiver {
    private static final String TAG = "TriggerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        String action = intent.getAction();
        Log.i(TAG, "onReceive: " + action);
        if (action == null) {
            return;
        }

        switch (action) {
            case Constant.ACTION_ACC_PERMISSION:
                PermissionHelper.openAccessibilityActivityIfNotGranted(context);
                break;
        }
    }
}