package com.autotest.sonicclient.handler;

import android.content.Context;

import com.autotest.sonicclient.services.RunServiceHelper;

import java.lang.ref.WeakReference;

public class StepHandlerBase {
    protected boolean isRunning = true;

    WeakReference<Context> contextWeakReference;
    public StepHandlerBase(Context context) {
        contextWeakReference = new WeakReference<>(context);
    }

    public boolean isStopped() {
        return !isRunning();
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }
}
