package com.autotest.sonicclient.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.CustomService;
import com.autotest.sonicclient.model.Selector;
import com.autotest.sonicclient.nodes.AccessibilityNodeInfoImpl;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.XMLUtil;

import org.w3c.dom.Node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@CustomService
public class TServiceWrapper {
    private static final String TAG = "TServiceWrapper";
    private final int TIMEOUT = 5000;

    @Assemble
    private AccessibilityService accessibilityService;
    @Assemble
    private AdbServiceWrapper adbService;

    public boolean isReady() {
        return accessibilityService != null && ((TService) accessibilityService).isConnected();
    }

    boolean waitService(int timeout) {
        long tStart = System.currentTimeMillis();
        while (!isReady() && tStart + timeout > System.currentTimeMillis()) {
            SystemClock.sleep(500);
        }
        return isReady();
    }

    AccessibilityNodeInfo getRootInActiveWindow() {
        waitService(5000);
        return accessibilityService.getRootInActiveWindow();
    }

    @NonNull
    AccessibilityNodeInfo getAccessibilityNodeInfo() throws Exception {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        long timeStart = System.currentTimeMillis();
        while (timeStart + TIMEOUT > System.currentTimeMillis() && rootNode == null) {
            rootNode = getRootInActiveWindow();
            SystemClock.sleep(50);
        }
        if (rootNode == null) {
            throw new Exception("Get node info fail !");
        }
        return rootNode;
    }

//    public AccessibilityNodeInfo findNodeById(String id) throws Exception {
//        AccessibilityNodeInfo accessibilityNodeInfo = getAccessibilityNodeInfo();
//        return accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id).get(0);
//    }

    public void pressHomeKey() {
        accessibilityService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    // 将节点转换成 JSON 格式
    private JSONObject toJson(AccessibilityNodeInfo node) {

        JSONObject jsonNode = new JSONObject();
        try {
            // 添加节点属性
            jsonNode.put("index", node.getParent() != null ? getIndex(node) : 0);
            jsonNode.put("text", node.getText() != null ? node.getText().toString() : "");
            jsonNode.put("resource-id", node.getViewIdResourceName() != null ? node.getViewIdResourceName() : "");
            jsonNode.put("class", node.getClassName() != null ? node.getClassName().toString() : "");
            jsonNode.put("package", node.getPackageName() != null ? node.getPackageName().toString() : "");
            jsonNode.put("content-desc", node.getContentDescription() != null ? node.getContentDescription().toString() : "");
            jsonNode.put("checkable", node.isCheckable());
            jsonNode.put("checked", node.isChecked());
            jsonNode.put("clickable", node.isClickable());
            jsonNode.put("enabled", node.isEnabled());
            jsonNode.put("focusable", node.isFocusable());
            jsonNode.put("focused", node.isFocused());
            jsonNode.put("scrollable", node.isScrollable());
            jsonNode.put("long-clickable", node.isLongClickable());
            jsonNode.put("password", node.isPassword());
            jsonNode.put("selected", node.isSelected());
            Rect rect = new Rect();
            node.getBoundsInScreen(rect);
            jsonNode.put("bounds", rect);

            // 处理子节点
            JSONArray childrenArray = new JSONArray();
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo childNode = node.getChild(i);
                if (childNode != null) {
                    childrenArray.add(toJson(childNode)); // 递归子节点
                }
            }
            jsonNode.put("children", childrenArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonNode;
    }

    public String getXml() {
        AccessibilityNodeInfo rootNode;
        try {
            rootNode = getAccessibilityNodeInfo();
        } catch (Exception e) {
            LogUtil.w(TAG, "读取界面失败", e.getMessage());
            return "";
        }
        return XMLUtil.nodeToXml(rootNode);
    }

    // 获取节点的索引
    private int getIndex(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = node.getParent();
        if (parent == null) {
            return 0;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (node.equals(parent.getChild(i))) {
                return i;
            }
        }
        return 0;
    }

    // 根据 ContentDescription 查找并点击元素
    private boolean performClickByContentDescription(AccessibilityNodeInfo rootNode, String contentDescription) {
        if (rootNode == null) {
            return false;
        }

        // 如果当前节点的 ContentDescription 匹配，则尝试点击
        if (contentDescription.equals(rootNode.getContentDescription())) {
            return rootNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        // 遍历子节点递归查找
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            if (childNode != null) {
                boolean result = performClickByContentDescription(childNode, contentDescription);
                if (result) {
                    return true; // 如果子节点点击成功，停止继续查找
                }
            }
        }

        return false;
    }

    public AccessibilityNodeInfoImpl findNode(Selector selector) throws Exception {
        AccessibilityNodeInfo accessibilityNodeInfo = getAccessibilityNodeInfo();
        if (selector.getType().getValue().isEmpty()) {
            return findByXpath(selector);
        }
        return findNode(accessibilityNodeInfo, selector);
    }

    private AccessibilityNodeInfoImpl findByXpath(Selector selector) {
        String xml = getXml();
        Node node = XMLUtil.findNodeByXpath(selector.getValue(), xml);
        if (node == null) {
            return null;
        }
        return new AccessibilityNodeInfoImpl(node, this);
    }

    public AccessibilityNodeInfoImpl findNode(AccessibilityNodeInfo rootNode, Selector selector) throws Exception {
        if (rootNode == null) {
            return null;
        }

        Class<AccessibilityNodeInfo> accessibilityNodeInfoClass = AccessibilityNodeInfo.class;
        Method method = accessibilityNodeInfoClass.getMethod(selector.getType().getValue());
        Object resVal = method.invoke(rootNode);
        String val = null;
        if (resVal != null) {
            if (resVal instanceof String) {
                val = (String) resVal;
            } else if (resVal.getClass().getName().equals("android.text.method.ReplacementTransformationMethod$SpannedReplacementCharSequence")) {
                val = resVal.toString();
            }  else if (resVal instanceof Boolean) {
                val = String.valueOf(resVal);
            } else {
                throw new Exception(String.format("Analyse Error: Value from Method is : %s, type: %s", resVal, resVal.getClass()));
            }
            if (selector.getValue().equals(val)) {
                return new AccessibilityNodeInfoImpl(rootNode, this);
            }
        }

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            if (childNode != null) {
                AccessibilityNodeInfo node = findNode(childNode, selector);
                if (node != null) {
                    return new AccessibilityNodeInfoImpl(node, this);
                }
            }
        }

        return null;
    }

    private AccessibilityNodeInfo findByDesc(AccessibilityNodeInfo rootNode, String contentDescription) throws Exception {
        if (rootNode == null) {
            return null;
        }

        // 如果当前节点的 ContentDescription 匹配，则尝试点击
        if (contentDescription.equals(rootNode.getContentDescription())) {
            return rootNode;
        }

        // 遍历子节点递归查找
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            if (childNode != null) {
                return findByDesc(childNode, contentDescription);
            }
        }

        return null;
    }

    private boolean performClickByClassName(AccessibilityNodeInfo rootNode, String className) {
        if (rootNode == null) {
            return false;
        }

        // 如果当前节点的 className 匹配，则尝试点击
        if (className.equals(rootNode.getClassName())) {
            return rootNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }

        // 遍历子节点递归查找
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            if (childNode != null) {
                boolean result = performClickByContentDescription(childNode, className);
                if (result) {
                    return true; // 如果子节点点击成功，停止继续查找
                }
            }
        }

        return false;
    }

    // 点击节点
    private boolean performClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) return false;

        try {
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        } catch (Exception e) {
            Log.e(TAG, "performClick: ", e);
            return false;
        }
        return true;
//        if (nodeInfo.isClickable()) {
//            return true;
//        } else {
//            LogUtil.e(TAG, String.format("performClick: nodeinfo %s not clickable", nodeInfo));
//        }
//
//        AccessibilityNodeInfo parent = nodeInfo.getParent();
//        return parent != null && performClick(parent);
    }

    boolean click(String MethodName, String param) throws Exception {
        AccessibilityNodeInfo rootNode = getAccessibilityNodeInfo();

        List<AccessibilityNodeInfo> nodes = null;
        try {
            Class<AccessibilityNodeInfo> accessibilityNodeInfoClass = AccessibilityNodeInfo.class;
            Method method = accessibilityNodeInfoClass.getDeclaredMethod(MethodName, String.class);
            method.setAccessible(true);
            nodes = (List<AccessibilityNodeInfo>) method.invoke(rootNode, param);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }

        if (!nodes.isEmpty()) {
            for (AccessibilityNodeInfo node : nodes) {
                if (performClick(node)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void clickText(String text) throws Exception {
        click("findAccessibilityNodeInfosByText", text);
    }

    public void clickID(String id) throws Exception {
        click("findAccessibilityNodeInfosByViewId", id);
    }

    public void clickDesc(String desc) throws Exception {
        AccessibilityNodeInfo rootNode = getAccessibilityNodeInfo();
        performClickByContentDescription(rootNode, desc);
    }

    public void clickClassName(String className) throws Exception {
        AccessibilityNodeInfo rootNode = getAccessibilityNodeInfo();
        performClickByClassName(rootNode, className);
    }

    public void clickPos(Point point) throws Exception {
        clickPos(point.x, point.y);
    }

    public void clickPos(float x, float y) throws Exception {
        // 路径
        Path path = new Path();
        path.moveTo(x, y); // 定义路径起点为点击的坐标

        // 描述
        GestureDescription.StrokeDescription stroke = new GestureDescription.StrokeDescription(path, 0, 200);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(stroke).build();

        // 执行
        boolean result = accessibilityService.dispatchGesture(gesture, new AccessibilityService.GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
                LogUtil.d(TAG, "Gesture completed successfully.");
            }

            @Override
            public void onCancelled(GestureDescription gestureDescription) {
                super.onCancelled(gestureDescription);
                LogUtil.d(TAG, "Gesture cancelled.");
            }
        }, new Handler(Looper.getMainLooper()));
        LogUtil.d(TAG, "clickPos: 点击结果：" + result);
        if (!result) {
            LogUtil.e(TAG, "Gesture dispatch failed.");
            throw new Exception(String.format("执行点击失败 (%s, %s)", x, y));
        }
    }

    public void clickByXpath(String xpath) throws Exception {
        String xml = getXml();
        Node node = XMLUtil.findNodeByXpath(xpath, xml);
        String bounds = node.getAttributes().getNamedItem("bounds").getNodeValue();
        Point point = XMLUtil.parseBoundsCenter(bounds);
        clickPos(point.x, point.y);
    }

    public void zoomIn(ArrayList<Point> points) {
        Point point0 = points.get(0);
        Point point1 = points.get(1);
        Path path1 = new Path();
        path1.moveTo(point0.x, point0.y); // 第一个手指位置
        path1.lineTo(point1.x, point1.y);

        Path path2 = new Path();
        path2.moveTo(points.get(2).x, points.get(2).y); // 第二个手指位置
        path2.lineTo(points.get(3).x, points.get(3).y);

        GestureDescription.StrokeDescription strokeDescription1 = new GestureDescription.StrokeDescription(path1, 0, 1000);
        GestureDescription.StrokeDescription strokeDescription2 = new GestureDescription.StrokeDescription(path2, 0, 1000);

        GestureDescription gestureDescription = new GestureDescription.Builder()
                .addStroke(strokeDescription1)
                .addStroke(strokeDescription2)
                .build();

        accessibilityService.dispatchGesture(gestureDescription, null, null);
    }
}
