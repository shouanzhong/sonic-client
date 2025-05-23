package com.autotest.sonicclient.utils;

import android.app.Instrumentation;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class InstrumentImpl extends Instrumentation {
    private static final String TAG = "InstrumentImpl";
    private static final InstrumentImpl _instance = new InstrumentImpl();

    public static InstrumentImpl getInstance() {
        return _instance;
    }

    @Deprecated
    public void clickPos(float x, float y) {
        LogUtil.d(TAG, String.format("clickPos: x: %s, y: %s", x, y));
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis() + 100;

        MotionEvent downEvent = MotionEvent.obtain(downTime, eventTime, MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent upEvent = MotionEvent.obtain(downTime, eventTime + 100, MotionEvent.ACTION_UP, x, y, 0);

        // send event
        sendPointerSync(downEvent);
        sendPointerSync(upEvent);
    }

    public void clickPos(Point point) {
        clickPos(point.x, point.y);
    }

    public void pressKeyCode(int keycode) throws InterruptedException {
        Thread thread = new Thread(() -> sendKeyDownUpSync(keycode));
        thread.start();
        thread.join(1000);
    }
}
