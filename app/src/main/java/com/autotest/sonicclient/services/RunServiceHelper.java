package com.autotest.sonicclient.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.utils.LogUtil;

public class RunServiceHelper {
    private static final String TAG = "RunServiceHelper";

    static final String ACTION_RUN_SUIT = "com.autotest.sonicclient.services.action.RUN_SUIT";
    static final String EXTRA_SUIT_INFO = "com.autotest.sonicclient.services.extra.SUIT_INFO";

    public static void startActionRunSuit(Context context, JSONObject suitInfo) {
        LogUtil.d(TAG, "startActionRunSuit: suitInfo: " + suitInfo);
        Intent intent = new Intent(context, RunService.class);
        intent.setAction(ACTION_RUN_SUIT);
        intent.putExtra(EXTRA_SUIT_INFO, suitInfo.toJSONString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static boolean isServiceRunning() {
        return RunService.isRunning;
    }

    public static boolean isServiceReady() {
        return InjectorService.getService(TServiceWrapper.class).isReady();
    }

    public static boolean waitServiceReady() {
        for (int i = 0; i < 20 && !isServiceReady(); i++) {
            SystemClock.sleep(500);
        }
        return isServiceReady();
    }
}