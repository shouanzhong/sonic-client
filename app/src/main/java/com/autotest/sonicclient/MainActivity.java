package com.autotest.sonicclient;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.autotest.sonicclient.activities.BaseActivity;
import com.autotest.sonicclient.application.ApplicationImpl;
import com.autotest.sonicclient.dialogs.MDialog;
import com.autotest.sonicclient.services.AdbService;
import com.autotest.sonicclient.services.AdbServiceWrapper;
import com.autotest.sonicclient.services.InjectorService;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.HttpUtil;
import com.autotest.sonicclient.utils.PermissionHelper;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends BaseActivity {
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


//        Intent intent = new Intent();
//        intent.setClass(this, AdbService.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startService(intent);
//        SystemClock.sleep(3000);


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

    void test_command() {

    }

//    void test_automation() {
//        // 在测试代码中直接使用 UiAutomation
//        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
//
//// 执行 UI 操作（例如点击）
//        automation.executeShellCommand("input tap 500 500"); // 模拟点击坐标 (500,500)
//    }

    void test_adb() {
        AdbServiceWrapper adbService = InjectorService.getService(AdbServiceWrapper.class);
//
//        if (!adbService.isConnected()) {
//            checkAdbStatus(this);
//            return;
//        }
//        PermissionHelper.openAccessibilityActivityIfNotGranted(this);
//        try {
//            PermissionHelper.grantAllPermissions(this, this.getPackageName());
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//
        for (int i = 0; i < 3 && !adbService.isConnected(); i++) {
            SystemClock.sleep(1000);
        }

        adbService.execCmd(String.format("input tap %s %s", 613, 649), 0);

        String pm_list_pakcage = adbService.execCmd("reboot", 5000);
//        Log.i(TAG, "test_adb: " + pm_list_pakcage);

    }

    private void checkAdbStatus(AppCompatActivity context) {
        new MDialog(context).checkAdbStatus(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.i(TAG, "onClick: ------------------------");
                //
//                try {
//                    ApplicationImpl.getInstance().inject();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                //
                if (InjectorService.getService(AdbServiceWrapper.class).isConnected()) {
                    dialog.dismiss();
                } else {
                    SystemClock.sleep(500);
                    checkAdbStatus(context);
                }
            }
        });
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.i(TAG, "dispatchTouchEvent: " + event.getAction());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();
            Log.d("TouchEvent", "Activity 点击坐标: (" + x + ", " + y + ")");
        }
        return super.dispatchTouchEvent(event); // 确保触摸事件继续传递
    }

    public void onTextClick(View view) {
//        Toast.makeText(this, "Text Clicked !!!", Toast.LENGTH_SHORT).show();
        ApplicationImpl.getInstance().showToast("Text Clicked !!!");
        Log.i("TouchEvent", "onTextClick: Text Clicked !!!");
    }

    public void onButtonClick(View view) throws Exception {
        switch (view.getId()) {
            case R.id.bt1:
//                TService.getInstance().performZoomIn();
//                String s = ShellUtil.execCmd("pm uninstall world.letsgo.booster.android.pro");
//                ToastUtil.showToast(s);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(Constant.ACTION_ACC_PERMISSION);
                broadcastIntent.setPackage(getPackageName());
                sendBroadcast(broadcastIntent);




                break;
            case R.id.bt2:
                ApplicationImpl.getInstance().showToast("click on bt2");
        }
    }

    public void test_okHttp() {
        HttpUtil.get("http://172.22.22.120/server/api/controller/testSuites/custom-list?model=lamu_g&board=lamu", new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                System.out.println("请求失败");
                Log.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.i(TAG, "onResponse: 返回值为：");
                Log.i(TAG, "onResponse: " + response.body().string());
            }
        });
    }
}