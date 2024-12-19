package com.autotest.sonicclient.utils;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

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

    public static void parseTestSuit(JSONObject jsonObject, SuitInfoListener listener) {
        List<JSONObject> cases;
        if (jsonObject.containsKey("data")) {
            cases = jsonObject.getJSONArray("data").toJavaList(JSONObject.class);
            for (JSONObject caseInfo : cases) {
                listener.handle(caseInfo);
//                Log.i(TAG, "parseTestSuit: caseInfo: " + caseInfo);
//                JSONObject caseStepVo = caseInfo.getJSONObject("caseStepVo");
//                List<JSONObject> steps = parseStep(caseInfo);
//                for (JSONObject step : steps) {
//                    Log.i(TAG, "parseTestSuit: step: " + step);
//                }
            }
        } else {
            cases = jsonObject.getJSONArray("cases").toJavaList(JSONObject.class);
            for (JSONObject caseInfo : cases) {
                listener.handle(caseInfo);
//                Log.i(TAG, "parseTestSuit: caseInfo: " + caseInfo);
//                List<JSONObject> steps = parseStep(caseInfo);
//                for (JSONObject step : steps) {
//                    Log.i(TAG, "parseTestSuit: step: " + step);
//                }
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

    public interface SuitInfoListener {
        void handle(JSONObject caseInfo);
    }
}
