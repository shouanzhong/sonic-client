package com.autotest.sonicclient;

import androidx.annotation.NonNull;

import android.accessibilityservice.AccessibilityService;
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
import com.autotest.sonicclient.model.By;
import com.autotest.sonicclient.services.AdbService;
import com.autotest.sonicclient.services.AdbServiceWrapper;
import com.autotest.sonicclient.services.DeviceService;
import com.autotest.sonicclient.services.InjectorService;
import com.autotest.sonicclient.services.TServiceWrapper;
import com.autotest.sonicclient.utils.Assert;
//import com.autotest.sonicclient.utils.ImageMatcher;
import com.autotest.sonicclient.utils.http.HttpUtil;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ToastUtil;


import org.opencv.core.Point;
import org.opencv.utils.ImageMatcher;
import org.opencv.utils.SimilarityChecker;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

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

        Intent intent = new Intent();
        intent.setClass(this, AdbService.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(intent);


//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    0);
//        }


//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            if (!Environment.isExternalStorageManager()) {
//                Intent intent2 = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                startActivity(intent2);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // 权限被授予，可以执行创建文件夹和文件的操作
        } else {
            // 权限被拒绝，处理相应逻辑
        }
//        if (requestCode == REQUEST_CODE) {
//        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        LogUtil.i(TAG, "dispatchTouchEvent: " + event.getAction());
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getRawX();
            float y = event.getRawY();
            LogUtil.d("TouchEvent", "Activity 点击坐标: (" + x + ", " + y + ")");
        }
        return super.dispatchTouchEvent(event); // 确保触摸事件继续传递
    }

    public void onTextClick(View view) {
//        Toast.makeText(this, "Text Clicked !!!", Toast.LENGTH_SHORT).show();
        ToastUtil.showToast("Text Clicked !!!");
        LogUtil.i("TouchEvent", "onTextClick: Text Clicked !!!");
    }

    private boolean hasEle(TServiceWrapper tService, String eleType, String eleValue) throws Exception {
        switch (eleType) {
            case "id" : return tService.getXml().contains(String.format("id=\"%s\"", eleValue));
            case "accessibilityId" : return tService.getXml().contains(String.format("desc=\"%s\"", eleValue));
            case "xpath" : return tService.findNode(By.xpath(eleValue)) != null;
            case "className" : return tService.getXml().contains(String.format("class=\"%s\"", eleValue));
            default :

                LogUtil.e(TAG, "findEle: " + "查找控件元素失败" + "这个控件元素类型: " + eleType + " 不存在!!!");
        }
        return false;
    }

    void test_hasEle() throws Exception {
        TServiceWrapper tServiceWrapper = InjectorService.getService(TServiceWrapper.class);
        String xml = tServiceWrapper.getXml();
        Log.i(TAG, "onButtonClick: xml: " + xml );
        Log.i(TAG, "onButtonClick: -------1--------");
        Assert.assertTrue(hasEle(tServiceWrapper, "id", "com.autotest.sonicclient:id/bt2"));
        Log.i(TAG, "onButtonClick: -------2--------");
        Assert.assertTrue(hasEle(tServiceWrapper, "accessibilityId", "Button2"));
        Log.i(TAG, "onButtonClick: -------3--------");
        Assert.assertTrue(hasEle(tServiceWrapper, "xpath", "//android.widget.Button[@resource-id='com.autotest.sonicclient:id/bt2']"));
        Log.i(TAG, "onButtonClick: -------4--------");
        Assert.assertTrue(hasEle(tServiceWrapper, "className", "android.widget.Button"));
        Log.i(TAG, "onButtonClick: -------5--------");
    }

    void test_swipe() {
        TServiceWrapper tServiceWrapper = InjectorService.getService(TServiceWrapper.class);
        AdbServiceWrapper adbService = InjectorService.getService(AdbServiceWrapper.class);
        adbService.execCmd(String.format("input swipe %d %d %d %d %d",
                        10, 10, 10, 300, 300));
    }

    void test_clickPos() {
        TServiceWrapper tService = InjectorService.getService(TServiceWrapper.class);
        try {
            tService.clickPos(500, 550);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

    }

    public void onButtonClick(View view) throws Exception {
        switch (view.getId()) {
            case R.id.edit_text:
                InjectorService.getService(DeviceService.class).sendText("sendText");

            case R.id.bt1:
                String packageName = getPackageName();
                ToastUtil.showToast(packageName);
                
                TServiceWrapper tService = InjectorService.getService(TServiceWrapper.class);
                AdbServiceWrapper adbService = InjectorService.getService(AdbServiceWrapper.class);
                DeviceService deviceService = InjectorService.getService(DeviceService.class);

//                Point imageLocation = ImageMatcher.findImageLocation("/sdcard/DCIM/Camera/IMG_20250313_145832736_HDR2.jpg",
//                        "/sdcard/DCIM/Camera/IMG_20250313_145832736_HDR.jpg");
//                ToastUtil.showToast(String.format("point: %s", imageLocation) );


//                File file2 = new File("/sdcard/DCIM/Camera/IMG_20250315_154007688_MFNR_PORTRAIT.jpg");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        double similarMSSIMScore = ImageMatcher.getSimilarMSSIMScore(
                                "/sdcard/DCIM/Camera/IMG_20250315_154007688_MFNR_PORTRAIT.jpg",
                                "/sdcard/DCIM/Camera/IMG_20250315_153949736_MFNR_PORTRAIT.jpg", false);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.showToast("相似度: " + similarMSSIMScore);
                            }
                        });
                    }
                }).start();




//                String xml = deviceService.getXml();
//                Node nodeByXpath = XMLUtil.findNodeByXpath("//*[@text=\"While using the app\"]", xml);
//                AccessibilityNodeInfoImpl accessibilityNodeInfo = new AccessibilityNodeInfoImpl(nodeByXpath, tService);
//                accessibilityNodeInfo.click();

//                Point point = XMLUtil.parseBoundsCenter(nodeByXpath.getAttributes().getNamedItem("bounds").getNodeValue());
//                deviceService.click(point);

//                String eleType = "id";
//                String eleValue = "";
//                String xpath= "";
//                switch (eleType) {
//                    case "id" :
//                        xpath = "//*[@resource-id=\"While using the app\"]";
//                        return tService.getXml().contains(String.format("id=\"%s\"", eleValue));
//                    case "accessibilityId" :
//                        xpath = "//*[@content-desc=\"While using the app\"]";
//                        return tService.getXml().contains(String.format("desc=\"%s\"", eleValue));
//                    case "xpath" :
//                        xpath = eleValue;
//                        return tService.findNode(By.xpath(eleValue)) != null;
//                    case "className" :
//                        xpath = "//*[@class=\"While using the app\"]";
//                        return tService.getXml().contains(String.format("class=\"%s\"", eleValue));
//                    default :
//                        LogUtil.e(TAG, "findEle: " + "查找控件元素失败" + "这个控件元素类型: " + eleType + " 不存在!!!");
//                }
//                String classXml = XMLUtil.tag2Class(xml);
//                LogUtil.i(TAG, "onButtonClick: " + classXml);

//                deviceService.execCmd("settings put secure enabled_accessibility_services com.autotest.sonicclient/com.autotest.sonicclient.services.TService");



//                deviceService.longPress(new Point(500, 550), 300);
//                InjectorService.getService(StepHandler.class).getTextAndAssert("xxxx", "xpath", "//android.widget.Button[@resource-id='com.autotest.sonicclient:id/bt2']", "BUTTON2");

//                InjectorService.getService(DeviceService.class).uninstallPkg("com.baidu.searchbox");
//                    InjectorService.getService(DeviceService.class).pressKeyCode(KeyEvent.KEYCODE_POWER);
//                ele = InjectorService.getService(StepHandler.class).findEle("accessibilityId", "Button2");
//                jsonObject = ((AccessibilityNodeInfoImpl) ele).toJson(false);
//                attrValue = jsonObject.getString("text");
//                LogUtil.i(TAG, "onButtonClick: attr = " + attrValue);
//
//                ele = InjectorService.getService(StepHandler.class).findEle("xpath", "//android.widget.Button[@resource-id='com.autotest.sonicclient:id/bt2']");
//                LogUtil.i(TAG, "onButtonClick: attr = " + attrValue);
//                jsonObject = ((AccessibilityNodeInfoImpl) ele).toJson(false);
//                attrValue = jsonObject.getString("text");
//                LogUtil.i(TAG, "onButtonClick: attr = " + attrValue);
//
//                ele = InjectorService.getService(StepHandler.class).findEle("className", "android.widget.Button");
//                jsonObject = ((AccessibilityNodeInfoImpl) ele).toJson(false);
//                attrValue = jsonObject.getString("text");
//                LogUtil.i(TAG, "onButtonClick: attr = " + attrValue);


//                test_adb();
//                TServiceWrapper tService = InjectorService.getService(TServiceWrapper.class);
//                AdbServiceWrapper adbService = InjectorService.getService(AdbServiceWrapper.class);
//                tService.clickPos(613, 649);

//                SystemClock.sleep(5000);
//
//                ArrayList<Point> points = new ArrayList<>();
//                points.add(new Point(100, 100));
//                points.add(new Point(200, 200));
//                points.add(new Point(100, 300));
//                points.add(new Point(300, 400));
//
//                TServiceWrapper service = InjectorService.getService(TServiceWrapper.class);
//                assert service != null;
//                service.zoomIn(points);

                break;
            case R.id.bt2:
                ToastUtil.showToast("click on bt2");
        }
    }

    public void test_okHttp() {
        HttpUtil.get("http://172.22.22.120/server/api/controller/testSuites/custom-list?model=lamu_g&board=lamu", new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                System.out.println("请求失败");
                LogUtil.e(TAG, "onFailure: ", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                LogUtil.i(TAG, "onResponse: 返回值为：");
                LogUtil.i(TAG, "onResponse: " + response.body().string());
                response.close();
            }
        });
    }
}