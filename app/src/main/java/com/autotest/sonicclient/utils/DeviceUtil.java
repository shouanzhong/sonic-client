package com.autotest.sonicclient.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DeviceUtil {
    public static Point getScreenSize() {
        String wm_size = ShellUtil.execCmd("wm size");
        String wmSize = wm_size.replace("Physical size:", "").trim();
        String[] xes = wmSize.split("x");

        return new Point(Integer.parseInt(xes[0]), Integer.parseInt(xes[1]));
    }

    public static String takeShot(String name) {
        @SuppressLint("SdCardPath") String dir = "/sdcard/sonic";
        FileUtil.createDirs(dir);
        long timeMillis = System.currentTimeMillis();
        String filePath = String.format("screencap -p %s/%s_%s.png", dir, name, timeMillis);
        ShellUtil.execCmd(filePath);
        return filePath;
    }
}
