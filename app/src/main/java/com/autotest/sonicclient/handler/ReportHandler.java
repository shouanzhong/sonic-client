package com.autotest.sonicclient.handler;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.application.ApplicationImpl;
import com.autotest.sonicclient.enums.StepType;
import com.autotest.sonicclient.model.CaseResult;
import com.autotest.sonicclient.model.StepResult;
import com.autotest.sonicclient.model.SuitResult;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.HttpUtil;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.MinioUtil;
import com.autotest.sonicclient.utils.ShellUtil;
import com.autotest.sonicclient.utils.ToastUtil;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import cn.hutool.core.io.FileUtil;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ReportHandler {
    private static final String TAG = "ReportHandler";
    public static String REPORT_URL = "";
    public static String LOG_FILE_URL = "";
    static JSONObject postBody;
    private static boolean DEBUG = false;

    public static void createRemoteSuitResult(Context context, JSONObject suitInfo) {
        if (DEBUG) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("suiteId", suitInfo.getInteger(Constant.KEY_SUIT_INFO_SID));
        jsonObject.put("udid", Build.getSerial());
        jsonObject.put("deviceName", Build.PRODUCT);
        jsonObject.put("model", Build.MODEL);
        jsonObject.put("board", Build.BOARD);

        String ver = Build.PRODUCT;
        if (ver.isEmpty()) {
            ver = ShellUtil.execCmd("getprop ro.internal.build.version");
            ver = ver.isEmpty() ? ShellUtil.execCmd("getprop ro.product.tran.version.release") : ver;
        }
        jsonObject.put("version", ver);
        jsonObject.put("manufacturer", Build.MANUFACTURER);
        jsonObject.put("fingerprint", Build.FINGERPRINT);


        HttpUtil.post(Constant.URL_SERVER_TESTSUITE_RESULT_CREATE, jsonObject.toString(), new HttpUtil.Callback<JSONObject>() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                super.onFailure(call, e);
                ToastUtil.showToast(context, "远程测试报告创建失败", true);
            }

            @Override
            public void onResponse(Call call, JSONObject item) throws IOException {
                ToastUtil.showToast(context.getApplicationContext(), "远程测试报告创建成功", true);
                // {resultId: int, deviceId: int}
                suitInfo.put(Constant.KEY_CASE_INFO_RID, item.getInteger("resultId"));
                suitInfo.put(Constant.KEY_CASE_INFO_DEVICE_ID, item.getInteger("deviceId"));
            }
        });
    }

    public static void send(SuitResult suitResult) {
        Log.d(TAG, "send: suitResult: " + suitResult);
        sendLog(suitResult);
        sendSuitResult(REPORT_URL, suitResult);
    }

    private static void sendLog(SuitResult suitResult) {
        if (DEBUG) {
            return;
        }

        JSONArray cases = suitResult.getCases();
        for (int i = 0; i < cases.size(); i++) {
            CaseResult caseInfo = cases.getObject(i, CaseResult.class);
            //
            String logURI = caseInfo.getLogUri();
            if (logURI.isEmpty()) {
                continue;
            }

            File file = new File(logURI);
            if (!file.exists()) {
                continue;
            }

            // 上传文件到minio
            try {
                String url = MinioUtil.builder().setBucketName(ApplicationImpl.getInstance().getPackageName())
                        .build().upload(file.getAbsolutePath(), file.getName());
                caseInfo.setLogUri(url);
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "sendLog: fail: " + file, e);
            }
        }
    }

    private static void sendSuitResult(String url, SuitResult suitResult) {
        if (DEBUG) {
            return;
        }
        HttpUtil.post(url, suitResult.toString(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // TODO:
            }
        });
    }

    public static void sendStepsResult(Context context, CaseResult caseResult) {
        if (DEBUG) {
            return;
        }
        boolean haveError = false;
        for (StepResult step : caseResult.getSteps().toJavaList(StepResult.class)) {
            haveError = haveError || step.getStatus() != StepType.PASS;
            postBody = new JSONObject();
            postBody.put("caseId", caseResult.getCid());
            postBody.put("desc", step.getDes());
            postBody.put("deviceId", caseResult.getDeviceId());
            postBody.put("log", step.getLog());
            postBody.put("resultId", step.getRid());
            postBody.put("status", step.getStatus());
            postBody.put("type", "step");
            LogUtil.d(TAG, "sendCaseResult: json: " + postBody);
            HttpUtil.post(Constant.URL_SERVER_STEP_RESULT, postBody.toString(), new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    ToastUtil.showToast(context, "上传步骤执行结果失败");
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    Log.d(TAG, "onResponse: sendCaseResult: " + response.body().string());
                }
            });
            FileUtil.writeString(caseResult.toJSONString(), String.format("/sdcard/sonic/%s/%s_%s.log",
                    caseResult.getCaseName(), caseResult.getCaseName(), System.currentTimeMillis()),
                    StandardCharsets.UTF_8);
        }
        sendCaseResult(context, haveError);
    }

    public static void sendCaseResult(Context context, boolean isError) {
        if (DEBUG) {
            return;
        }
        postBody.put("type", "status");
        postBody.put("status", isError ? StepType.ERROR : StepType.PASS);
        LogUtil.d(TAG, "sendFinish: json: " + postBody);
        HttpUtil.post(Constant.URL_SERVER_STEP_RESULT, postBody.toString(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ToastUtil.showToast(context, "发送结束信号失败", true);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                Log.d(TAG, "onResponse: sendFinish: " + response.body().string());
            }
        });
    }
}
