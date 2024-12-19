package com.autotest.sonicclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.application.ApplicationImpl;
import com.autotest.sonicclient.services.TService;
import com.autotest.sonicclient.utils.InstrumentImpl;
import com.autotest.sonicclient.utils.PermissionHelper;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.ShellUtil;
import com.autotest.sonicclient.utils.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private WeakReference<AccessibilityService> serviceRef = new WeakReference<>(null);
    private MediaProjectionManager mediaProjectionManager;
    private MediaProjection mediaProjection;
    private VirtualDisplay virtualDisplay;
    private MediaProjectionManager projectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionHelper.openAccessibilityActivityIfNotGranted(this);



//        JSONObject jsonObject = JsonParser.readJsonFromAssets(this, "suitCaseTemp.json");
////        System.out.println(jsonObject);
//        Log.i(TAG, "onCreate: " + jsonObject.toString());
//        Log.i(TAG, "onCreate: " + jsonObject.get("cases"));
//        Log.i(TAG, "onCreate: " + jsonObject.get("cases"));
//        ArrayList<JSONObject> arrayList = (ArrayList) jsonObject.get("cases");
//        for (JSONObject case_ : arrayList) {
//            ArrayList<JSONObject> steps = (ArrayList) case_.get("steps");
//            Log.i(TAG, "onCreate: " + steps);
//            for (JSONObject step : steps) {
//                Log.i(TAG, "onCreate: step = " + step);
//                Log.i(TAG, "onCreate: " + step.get(""));
//            }
//        }
    }



    public void onTextClick(View view) {
//        Toast.makeText(this, "Text Clicked !!!", Toast.LENGTH_SHORT).show();
        ApplicationImpl.getInstance().showToast("Text Clicked !!!");
        Log.i(TAG, "onTextClick: Text Clicked !!!");
    }

    public void onButtonClick(View view) throws Exception {
//        while (!TService.isReady()) {
//            SystemClock.sleep(50);
//        }
        switch (view.getId()) {
            case R.id.bt1:
                @SuppressLint("MissingPermission") String serial = Build.getSerial();
                ToastUtil.showToast(serial);

                ShellUtil.execCmd("screencap -p /sdcard/shot.png");

//                TService.getInstance().clickText("Hello World!");
//                TService.getInstance().clickID("com.autotest.sonicclient:id/bt2");
//                TService.getInstance().clickDesc("Button2");

//                try {
//                    Log.d(TAG, "onButtonClick: -----------------");
//                    TService.getInstance().clickPos(550, 550);
////                    ShellUtil.execCmd(String.format("input tap %s %s", 550, 550), 100);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                ShellUtil.execCmd("settings put system pointer_location 1");
//                ShellUtil.execCmd("input swipe 100 100 200 200 40");
//                public void sendHomeKey() {

//            }
                break;
            case R.id.bt2:
                ApplicationImpl.getInstance().showToast("click on bt2");
                ApplicationImpl.getInstance().scanClasses();
        }
    }
}