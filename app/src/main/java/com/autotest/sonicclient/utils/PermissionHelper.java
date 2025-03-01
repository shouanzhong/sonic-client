package com.autotest.sonicclient.utils;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.autotest.sonicclient.dialogs.MDialog;
import com.autotest.sonicclient.services.AdbServiceWrapper;
import com.autotest.sonicclient.services.InjectorService;
import com.autotest.sonicclient.services.TService;

public class PermissionHelper {
    private static final String TAG = "PermissionHelper";


    public static void checkAdbStatus(AppCompatActivity context) {
        AdbServiceWrapper adbService = InjectorService.getService(AdbServiceWrapper.class);
        for (int i = 0; i < 3 && !adbService.isConnected(); i++) {
            SystemClock.sleep(1000);
            adbService = InjectorService.getService(AdbServiceWrapper.class);
        }
        if (!adbService.isConnected()) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new MDialog(context).checkAdbStatus(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (InjectorService.getService(AdbServiceWrapper.class).isConnected()) {
                                dialog.dismiss();
                            } else {
                                SystemClock.sleep(1000);
                                checkAdbStatus(context);
                            }
                        }
                    });
                }
            });
        }
    }

    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        String service = context.getPackageName() + "/" + serviceClass.getName();
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        );
        boolean accessibilityEnabled = Settings.Secure.getInt(
                context.getContentResolver(),
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
        ) == 1;

        if (accessibilityEnabled && !TextUtils.isEmpty(enabledServices)) {
            String[] enabledServiceList = enabledServices.split(":");
            for (String enabledService : enabledServiceList) {
                if (enabledService.equalsIgnoreCase(service)) {
                    return true;
                }
            }
        }
        return false;
    }

    // 引导用户到辅助功能设置页面
    public static void promptEnableAccessibility(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static void openAccessibilityActivityIfNotGranted(Context context) {
        AdbServiceWrapper adbService = InjectorService.getService(AdbServiceWrapper.class);
        adbService.execCmd("settings put secure enabled_accessibility_services com.autotest.sonicclient/com.autotest.sonicclient.services.TService");
        adbService.execCmd("settings put secure accessibility_enabled 1");
        SystemClock.sleep(2000);
        if (!PermissionHelper.isAccessibilityServiceEnabled(context, TService.class)) {
            // 提示用户
            PermissionHelper.promptEnableAccessibility(context);
        } else {
            // 已打开
            LogUtil.i(TAG, "Accessibility Service is enabled");
        }
    }

    public static void grantAllPermissions(Context context, String pkg) throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        PackageInfo pkgInfo = pm.getPackageInfo(pkg, PackageManager.GET_PERMISSIONS);
        String[] permissions = pkgInfo.requestedPermissions;
        if (permissions != null) {
            for (String permission : permissions) {
                if (pm.checkPermission(permission, pkg) == PackageManager.PERMISSION_DENIED) {
                    ShellUtil.execCmd(String.format("pm grant %s %s", pkg, permission));
                }
            }
        }
    }
}
