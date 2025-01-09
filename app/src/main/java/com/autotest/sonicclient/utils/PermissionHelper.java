package com.autotest.sonicclient.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.autotest.sonicclient.services.TService;

public class PermissionHelper {
    private static final String TAG = "AccessibilityHelper";

    // 检查辅助功能服务是否开启
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
        Settings.Secure.putString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                "com.autotest.sonicclient/com.autotest.sonicclient.services.TService"
        );
        Settings.Secure.putInt(context.getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
        for (int i = 0; i < 20 && TService.getInstance() != null; i++) {
            SystemClock.sleep(100);
        }
        if (!PermissionHelper.isAccessibilityServiceEnabled(context, TService.class)) {
            // 辅助功能未开启，提示用户
            PermissionHelper.promptEnableAccessibility(context);
        } else {
            // 辅助功能已开启，可以继续使用
            Log.d(TAG, "Accessibility Service is enabled");
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
