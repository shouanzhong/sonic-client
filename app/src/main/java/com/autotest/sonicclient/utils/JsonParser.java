package com.autotest.sonicclient.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.nodes.AccessibilityNodeInfoImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class JsonParser {
    private static final String TAG = "JsonParser";

    public static boolean isJSONObjectEmpty(JSONObject jsonObject) {
        return jsonObject == null || jsonObject.isEmpty();
    }

    public static JSONObject readJsonFromAssets(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder();
        try(
                InputStream inputStream = context.getAssets().open(fileName);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            ) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                Log.i(TAG, "readJsonFromAssets: " + line);
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return JSON.parseObject(stringBuilder.toString());
    }

    public static void parseTestSuit(JSONObject suitInfo, SuitInfoListener listener) {
        List<JSONObject> cases;
        if (suitInfo.containsKey("data")) {
            cases = suitInfo.getJSONArray("data").toJavaList(JSONObject.class);
            for (JSONObject caseInfo : cases) {
                if (listener.doBreak()) {
                    break;
                }
                caseInfo.put(Constant.KEY_CASE_INFO_RID, suitInfo.getInteger(Constant.KEY_CASE_INFO_RID));
                caseInfo.put(Constant.KEY_CASE_INFO_DEVICE_ID, suitInfo.getInteger(Constant.KEY_CASE_INFO_DEVICE_ID));
                listener.handle(caseInfo);
//                Log.i(TAG, "parseTestSuit: caseInfo: " + caseInfo);
//                JSONObject caseStepVo = caseInfo.getJSONObject("caseStepVo");
//                List<JSONObject> steps = parseStep(caseInfo);
//                for (JSONObject step : steps) {
//                    Log.i(TAG, "parseTestSuit: step: " + step);
//                }
            }
        } else {
            cases = suitInfo.getJSONArray(Constant.KEY_SUIT_INFO_CASES).toJavaList(JSONObject.class);
            for (JSONObject caseInfo : cases) {
                if (listener.doBreak()) {
                    break;
                }
                caseInfo.put(Constant.KEY_CASE_INFO_RID, suitInfo.getInteger(Constant.KEY_CASE_INFO_RID));
                caseInfo.put(Constant.KEY_CASE_INFO_DEVICE_ID, suitInfo.getInteger(Constant.KEY_CASE_INFO_DEVICE_ID));
                listener.handle(caseInfo);
//                Log.i(TAG, "parseTestSuit: caseInfo: " + caseInfo);
//                List<JSONObject> steps = parseStep(caseInfo);
//                for (JSONObject step : steps) {
//                    Log.i(TAG, "parseTestSuit: step: " + step);
//                }
//                break; // debug 调试用
            }
        }

    }

    public static List<JSONObject> parseStep(JSONObject caseInfo) {
        List<JSONObject> steps;
        String keySteps = "steps";
        if (caseInfo.containsKey("caseStepVo")) {
            JSONObject caseStepVo = caseInfo.getJSONObject("caseStepVo");  // 多此一层
            steps = caseStepVo.getJSONArray(keySteps).toJavaList(JSONObject.class);
        } else {
            steps = caseInfo.getJSONArray(keySteps).toJavaList(JSONObject.class);
        }
        return steps;
    }

    public JSONObject nodeToJson(AccessibilityNodeInfoImpl node) {

        JSONObject jsonNode = new JSONObject();
        try {
            // 添加节点属性
            jsonNode.put("index", node.getParent() != null ? node.getIndex() : 0);
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
                AccessibilityNodeInfoImpl childNode = (AccessibilityNodeInfoImpl) node.getChild(i);
                if (childNode != null) {
                    childrenArray.add(nodeToJson(childNode)); // 递归子节点
                }
            }
            jsonNode.put("children", childrenArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonNode;
    }



    public interface SuitInfoListener {
        void handle(JSONObject caseInfo);

        default boolean doBreak() {
            return false; // 是否需要继续
        }
    }
}
