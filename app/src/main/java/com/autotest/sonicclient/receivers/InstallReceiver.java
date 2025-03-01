package com.autotest.sonicclient.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageInstaller;

import com.autotest.sonicclient.utils.LogUtil;

public class InstallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String packageName = null;
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
        String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);

        if (status == PackageInstaller.STATUS_SUCCESS) {
            LogUtil.d("InstallResultReceiver", String.format("安装成功！", packageName));
        } else {
            LogUtil.e("InstallResultReceiver", String.format("安装失败：%s", message));
        }
    }
}
