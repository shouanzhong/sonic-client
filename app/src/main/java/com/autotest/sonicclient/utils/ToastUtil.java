package com.autotest.sonicclient.utils;

import android.content.Context;
import android.widget.Toast;

import com.autotest.sonicclient.application.ApplicationImpl;

public class ToastUtil {
    private static final String TAG = "ToastUtil";
    private static Toast sToast;

    public static void showToast(Context context, String msg) {
        if (sToast != null) {
            sToast.cancel();
        }
        sToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        sToast.show();
        LogUtil.d(context.getClass().toString(), msg);
    }

    public static void showToast(String msg) {
        showToast(ApplicationImpl.getInstance(), msg);
    }
}
