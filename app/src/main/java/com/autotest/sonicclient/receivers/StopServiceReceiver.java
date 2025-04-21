package com.autotest.sonicclient.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.autotest.sonicclient.services.RunService;
import com.autotest.sonicclient.utils.LogUtil;

public class StopServiceReceiver extends BroadcastReceiver {
    private static final String TAG = "StopServiceReceiver";
    private RunService runService;

    public StopServiceReceiver(RunService runService) {
        this.runService = runService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, RunService.class);
        context.stopService(serviceIntent);

        LogUtil.d(TAG, "Service force stop !");
        RunService.isRunning = false;
        runService.updateFinishNotification("手动停止测试");
    }
}