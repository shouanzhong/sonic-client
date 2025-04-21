package com.autotest.sonicclient.services;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInstaller;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.autotest.sonicclient.enums.XpathSelectorTypes;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.CustomService;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.model.Selector;
import com.autotest.sonicclient.nodes.AccessibilityNodeInfoImpl;
import com.autotest.sonicclient.receivers.InstallReceiver;
import com.autotest.sonicclient.receivers.UninstallReceiver;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.FileUtil;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ShellUtil;
import com.autotest.sonicclient.utils.XMLUtil;

import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import groovyjarjarantlr.StringUtils;

@HandlerService
public class DeviceService {
    private static final String TAG = "DeviceService";
    Rotation rotation = new Rotation();
    Context context;

    @Assemble
    AdbServiceWrapper adbService;
    @Assemble
    TServiceWrapper tService;

    public DeviceService(Context context) {
        this.context = context;
    }

    public boolean isReady() {
        return adbService.isConnected() && tService.isReady();
    }

    public Rotation getRotation() {
        return rotation;
    }

    public void pressKeyCode(int keyCode) {
        execCmd("input keyevent " + keyCode, 100);
    }

    public Point getScreenSize() {
        String wm_size = execCmd("wm size");
        String wmSize = wm_size.replace("Physical size:", "").trim();
        String[] xes = wmSize.split("x");

        return new Point(Integer.parseInt(xes[0]), Integer.parseInt(xes[1]));
    }

    public int getScreenOrientation() {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        int rotation = display.getRotation();
        int orientation = context.getResources().getConfiguration().orientation;

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            }
        } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
        }
        return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public String takeShot(String name) {
        @SuppressLint("SdCardPath") String dir = Constant.BASE_DIR +  "screenshot";
        FileUtil.createDirs(dir);
        long timeMillis = System.currentTimeMillis();
        String filePath = String.format("%s/%s_%s.png", dir, name, timeMillis);
        String cmd = String.format("screencap -p %s", filePath);
        execCmd(cmd);
        return filePath;
    }

    public boolean startApp(String pkg) {
        String s = execCmd(String.format("monkey -p %s -c android.intent.category.LAUNCHER 1", pkg));

        return !s.contains("No activities found");
    }

    public String forceStop(String packageName) {
        return execCmd("am force-stop " + packageName);
    }

    public String getCurrentActivity() {
        String cmd = execCmd(String.format("dumpsys window %s", ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? "displays" : "windows")));
        String result = "";
        try {
            String start = cmd.substring(cmd.indexOf("mCurrentFocus="));
            String end = start.substring(start.indexOf("/") + 1);
            result = end.substring(0, end.indexOf("}"));
        } catch (Exception e) {
        }
        if (result.length() == 0) {
            try {
                String start = cmd.substring(cmd.indexOf("mFocusedApp="));
                String end = start.substring(start.indexOf("/") + 1);
                String endCut = end.substring(0, end.indexOf(" "));
                result = endCut;
            } catch (Exception e) {
            }
        }
        return result;
    }

    public void setAirPlaneMode(Boolean enable) {
        int val = enable ? 1 : 0;
        execCmd("settings put global airplane_mode_on " + val);
        execCmd("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true");
    }

    public void setWifiState(Boolean enable) {
        String val = enable ? "enable" : "disable";
        execCmd("svc wifi " + val);
    }

    public void setLocationState(Boolean enable) {
        execCmd(String.format("settings put secure location_providers_allowed %sgps,network", enable ? "+" : "-"));
        execCmd(String.format("cmd location set-location-enabled %s", enable));
    }

    public void clearPkg(String pkg) throws Exception {
        String s = execCmd("pm clear " + pkg, 3000);
        if (s.contains("Error")) {
            throw new Exception(s);
        }
    }

    public void uninstallPkg(String pkg) throws Exception {
        String s = this.execCmd("pm uninstall " + pkg);
        if (!s.contains("Success")) {
            throw new Exception(String.format("应用[%s]卸载失败", pkg));
        }
    }

    public void installApk(String path) throws Exception {
        String tmpPath = "/data/local/tmp/test.apk";
        this.execCmd(String.format("cp %s %s", path, tmpPath));
        String s = this.execCmd("pm install " + tmpPath);
        if (s.contains("Error:")) {
            throw new Exception(String.format("应用[%s]安装失败", path));
        }
    }

    public String execCmd(String cmd) {
        return adbService.execCmd(cmd);
    }

    public String execCmd(String cmd, int timeout) {
        return adbService.execCmd(cmd, timeout);
    }

    public void execCmd(String cmd, ShellUtil.OnStreamChangedListener listener, int timeout) {
        adbService.execCmd(cmd, listener, timeout);
    }

    public void stopAppium() {
        this.execCmd("am force-stop io.appium.uiautomator2.server");
    }

    public boolean waitAdbConnected() {
        for (int i = 0; i < 3 && !adbConnected(); i++) {
            SystemClock.sleep(1000);
        }
        return adbConnected();
    }

    public boolean adbConnected() {
        return adbService != null && adbService.isConnected();
    }

    public void openAccessibilityActivity() {
        stopAppium();
        this.execCmd("settings put secure enabled_accessibility_services com.autotest.sonicclient/com.autotest.sonicclient.services.TService");
        this.execCmd("content call --uri content://settings/secure --method PUT_secure --arg enabled_accessibility_services  --extra _user:i:0 --extra value:s:com.autotest.sonicclient/com.autotest.sonicclient.services.TService");
        this.execCmd("settings put secure accessibility_enabled 1");
    }

    public void sendText(String text) {
        this.execCmd("input text " + text.replaceAll(" ", "%s"), 1000);
    }

    public void swipe(Point point1, Point point2, int duration) {
        this.execCmd(String.format("input swipe %d %d %d %d %d", point1.x, point1.y, point2.x, point2.y, duration), duration);
    }

    public void swipe(Point point1, Point point2) {
        swipe(point1, point2, 300);
    }

    public Point computedPoint(double x, double y) {
        if (x <= 1 && y <= 1) {
            int screenOrientation = getScreenOrientation();
            Point screenSize = getScreenSize();
            // 竖屏
            if (screenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT) {
                x = screenSize.x * x;
                y = screenSize.y * y;
            } else {
                x = screenSize.y * x;
                y = screenSize.x * y;
            }
        }
        return new Point((int) x, (int) y);
    }

    public void longPress(Point point, int duration) {
        swipe(point, point, duration);
    }

    public void click(Point point) {
        this.execCmd(String.format("input tap %s %s", point.x, point.y));
    }

    public void click(Object x, Object y) {
        this.execCmd(String.format("input tap %s %s", x, y));
    }

    public String getXml() {
        String xml = tService.getXml();
        if (TextUtils.isEmpty(xml)) {
            String uiautomator_dump = this.execCmd("uiautomator dump");
            xml = FileUtil.readUtf8String("/sdcard/window_dump.xml");
            try {
                xml = XMLUtil.tag2Class(xml);
            } catch (ParserConfigurationException | IOException | SAXException | TransformerException e) {
                e.printStackTrace();
            }
        }
        return xml;
    }

    public AccessibilityNodeInfoImpl findNode(Selector selector) {
        try {
            return tService.findNode(selector);
        } catch (Exception e) {
            LogUtil.w(TAG, "读取界面失败，尝试uiautomator获取", e);
            String xml = getXml();
            String type = XpathSelectorTypes.getValue(selector.getType());
            String value = selector.getValue();
            String formatString = "//*[@%s=\"%s\"]";
            String xpath = TextUtils.isEmpty(type) ? value : String.format(formatString, type, value);
            Node nodeByXpath = XMLUtil.findNodeByXpath(xpath, xml);
            return new AccessibilityNodeInfoImpl(nodeByXpath, tService);
        }
    }

    public void zoomIn(ArrayList<Point> points) {
        tService.zoomIn(points);
    }

    public class Rotation {
        // 启用屏幕旋转
            public void enable(Context context) {
            execCmd("settings put system accelerometer_rotation 1");
        }

        // 禁用屏幕旋转
        public void disable(Context context) {
            execCmd("settings put system accelerometer_rotation 0");
        }

        // 右旋转
        public void setRight(Context context) {
            execCmd("settings put system user_rotation 1");
        }

        // 左旋转
        public void setLeft(Context context) {
            execCmd("settings put system user_rotation 3");
        }
    }

}
