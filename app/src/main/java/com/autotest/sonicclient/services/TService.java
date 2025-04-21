package com.autotest.sonicclient.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.autotest.sonicclient.utils.LogUtil;

public class TService extends AccessibilityService {
    private static final String TAG = "TService";
    private boolean connected = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        connected = true;
        LogUtil.d(TAG, "Service connected");
        AccessibilityServiceInfo info = getServiceInfo();
        try {
            InjectorService.register((AccessibilityService) this, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (info == null) {
            LogUtil.e(TAG, "ServiceInfo为空");
            return;
        }
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
                AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.DEFAULT;
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                LogUtil.d(TAG, "Root window title: " + rootNode.getPackageName());
            }
        }
    }

    @Override
    public void onInterrupt() {
        LogUtil.w(TAG, "Service Interrupt !!");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtil.d(TAG, "Service onUnbind !!");
        connected = false;
        return super.onUnbind(intent);
    }

    public boolean isConnected() {
        return connected;
    }
}
