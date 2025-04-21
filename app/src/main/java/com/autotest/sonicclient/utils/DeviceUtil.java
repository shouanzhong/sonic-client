package com.autotest.sonicclient.utils;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInstaller;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.autotest.sonicclient.config.GConfig;
import com.autotest.sonicclient.receivers.InstallReceiver;
import com.autotest.sonicclient.receivers.UninstallReceiver;
import com.autotest.sonicclient.services.AdbService;
import com.autotest.sonicclient.services.AdbServiceWrapper;
import com.autotest.sonicclient.services.InjectorService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DeviceUtil {
    public static Point getScreenSize() {
        String wm_size = ShellUtil.execCmd("wm size");
        String wmSize = wm_size.replace("Physical size:", "").trim();
        String[] xes = wmSize.split("x");

        return new Point(Integer.parseInt(xes[0]), Integer.parseInt(xes[1]));
    }

    public static int getScreenOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        int rotation = display.getRotation();
        int orientation = context.getResources().getConfiguration().orientation;

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public static String takeShot(String name) {
        @SuppressLint("SdCardPath") String dir = GConfig.DATA_BASE_DIR + "screenshot";
        FileUtil.createDirs(dir);
        long timeMillis = System.currentTimeMillis();
        String filePath = String.format("%s/%s_%s.png", dir, name, timeMillis);
        String cmd = String.format("screencap -p %s", filePath);
        InjectorService.getService(AdbServiceWrapper.class).execCmd(cmd);
        return filePath;
    }

    public static String startApp(String pkg) {
        return ShellUtil.execCmd(String.format("monkey -p %s -c android.intent.category.LAUNCHER 1", pkg));
    }

    public static String forceStop(String packageName) {
        return ShellUtil.execCmd("am force-stop " + packageName);
    }

    public static String getCurrentActivity() {
        String cmd = ShellUtil.execCmd(String.format("dumpsys window %s", ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? "displays" : "windows")));
        String result = "";
        try {
            String start = cmd.substring(cmd.indexOf("mCurrentFocus="));
            String end = start.substring(start.indexOf("/") + 1);
            result = end.substring(0, end.indexOf("}"));
        } catch (Exception e) {
        }
        if (result.length() == 0) {
            try {
                String start = cmd.substring(cmd.indexOf("mFocusedApp="));
                String end = start.substring(start.indexOf("/") + 1);
                String endCut = end.substring(0, end.indexOf(" "));
                result = endCut;
            } catch (Exception e) {
            }
        }
        return result;
    }

    public static void setAirPlaneMode(Boolean enable) {
        int val = enable ? 1 : 0;
        ShellUtil.execCmd("settings put global airplane_mode_on " + val);
        ShellUtil.execCmd("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
    }

    public static void setWifiState(Boolean enable) {
        String val = enable ? "enable" : "disable";
        ShellUtil.execCmd("svc wifi " + val);
    }

    public static void setLocationState(Boolean enable) {
        ShellUtil.execCmd(String.format("settings put secure location_providers_allowed %sgps", enable ? "+" : "-"));
    }

    public static void uninstallPkg(Context context, String pkg) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        UninstallReceiver receiver = new UninstallReceiver();
        context.registerReceiver(receiver, filter);

        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        packageInstaller.uninstall(pkg, PendingIntent.getActivity(
                context, 0, new Intent(context, UninstallReceiver.class), PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT).getIntentSender());
    }

    public static void installApk(Context context, File apkFile) throws IOException {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);

        int sessionId = packageInstaller.createSession(params);
        PackageInstaller.Session session = packageInstaller.openSession(sessionId);

        try (OutputStream packageInSession = session.openWrite("package", 0, -1);
             FileInputStream apkStream = new FileInputStream(apkFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = apkStream.read(buffer)) != -1) {
                packageInSession.write(buffer, 0, bytesRead);
            }
            session.fsync(packageInSession);
        }

        // 监听安装结果
        Intent intent = new Intent(context, InstallReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, sessionId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        // 提交
        session.commit(pendingIntent.getIntentSender());
        session.close();
    }

    public static class Rotation {
        // 启用屏幕旋转
        public static void enable(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, 1);
        }

        // 禁用屏幕旋转
        public static void disable(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Settings.System.putInt(resolver, Settings.System.ACCELEROMETER_ROTATION, 0);
        }

        // 右旋转
        public static void setRight(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Settings.System.putInt(resolver, Settings.System.USER_ROTATION, 1);
        }

        // 左旋转
        public static void setLeft(Context context) {
            ContentResolver resolver = context.getContentResolver();
            Settings.System.putInt(resolver, Settings.System.USER_ROTATION, 3);
        }
    }

}
