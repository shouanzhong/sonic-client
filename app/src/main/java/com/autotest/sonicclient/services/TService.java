package com.autotest.sonicclient.services;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class TService extends AccessibilityService {
    private static final String TAG = "TService";

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "Service connected");
        try {
            InjectorService.register((AccessibilityService) this, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
