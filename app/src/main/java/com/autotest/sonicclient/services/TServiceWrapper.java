package com.autotest.sonicclient.services;

import android.accessibilityservice.AccessibilityService;
import android.os.SystemClock;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.autotest.sonicclient.interfaces.Assemble;

import java.lang.ref.WeakReference;

public class TServiceWrapper {
    private final int TIMEOUT = 5000;

    @Assemble
    private WeakReference<AccessibilityService> accessibilityService;


    @NonNull
    AccessibilityNodeInfo getAccessibilityNodeInfo() throws Exception {
        AccessibilityNodeInfo rootNode = accessibilityService.get().getRootInActiveWindow();
        long timeStart = System.currentTimeMillis();
        while (timeStart + TIMEOUT > System.currentTimeMillis() && rootNode == null) {
            rootNode = accessibilityService.get().getRootInActiveWindow();
            SystemClock.sleep(50);
        }
        if (rootNode == null) {
            throw new Exception("Get node info fail !");
        }
        return rootNode;
    }

    public AccessibilityNodeInfo findNodeById(String id) throws Exception {
        AccessibilityNodeInfo accessibilityNodeInfo = getAccessibilityNodeInfo();
        return accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id).get(0);
    }
}
