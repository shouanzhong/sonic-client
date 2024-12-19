package com.autotest.sonicclient.utils;

import android.annotation.SuppressLint;

public class CommonUtil {

    public static String takeShot(String name) {
        @SuppressLint("SdCardPath") String dir = "/sdcard/sonic";
        FileUtil.createDirs(dir);
        long timeMillis = System.currentTimeMillis();
        String filePath = String.format("screencap -p %s/%s_%s.png", dir, name, timeMillis);
        ShellUtil.execCmd(filePath);
        return filePath;
    }
}
