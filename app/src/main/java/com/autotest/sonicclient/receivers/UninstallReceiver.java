package com.autotest.sonicclient.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.util.Log;

import com.autotest.sonicclient.utils.LogUtil;

public class UninstallReceiver extends BroadcastReceiver {
    private static final String TAG = "UninstallReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
            String packageName = intent.getData().getSchemeSpecificPart();
            LogUtil.d(TAG, "应用被卸载：" + packageName);
        }
    }

    public static interface UninstallResultListener {
        void onReceive(Boolean result);
    }
}