package com.autotest.sonicclient.handler;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.application.ApplicationImpl;
import com.autotest.sonicclient.config.GConfig;
import com.autotest.sonicclient.enums.StepType;
import com.autotest.sonicclient.model.CaseResult;
import com.autotest.sonicclient.model.StepResult;
import com.autotest.sonicclient.model.SuitResult;
import com.autotest.sonicclient.threads.MExecutor;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.http.HttpUtil;
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
    private static boolean DEBUG = false;

    public static void createRemoteSuitResult(Context context, JSONObject suitInfo) {
        if (DEBUG) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("suiteId", suitInfo.getInteger(Constant.KEY_SUIT_INFO_SID));
        jsonObject.put("udid", "unknown");  // Build.getSerial()
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
        LogUtil.d(TAG, "send: suitResult: " + suitResult);
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
                LogUtil.e(TAG, "sendLog: fail: " + file, e);
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
                response.close();
            }
        });
    }

    public static void sendCaseResult(Context context, CaseResult caseResult) {
        if (DEBUG) {
            return;
        }
        JSONObject bodyJson = new JSONObject();
        for (StepResult step : caseResult.getSteps().toJavaList(StepResult.class)) {
            bodyJson.put("caseId", caseResult.getCid());
            bodyJson.put("desc", step.getDes());
            bodyJson.put("deviceId", caseResult.getDeviceId());
            bodyJson.put("log", step.getLog());
            bodyJson.put("resultId", step.getRid());
            bodyJson.put("status", step.getStatus());
            bodyJson.put("type", "step");
            break;
        }
        bodyJson.put("desc", "用例结束");
        bodyJson.put("log", "");
        bodyJson.put("type", "status");
        bodyJson.put("status", caseResult.isPass() ? StepType.PASS : StepType.ERROR);
        LogUtil.d(TAG, "sendFinish: json: " + bodyJson);
//        try {
//            String s = HttpUtil.postSync(Constant.URL_SERVER_STEP_RESULT, bodyJson.toString());
//        } catch (IOException e) {
//            LogUtil.d(TAG, String.format("upload case info fail: [%s]", bodyJson), e);
//        }
        HttpUtil.post(Constant.URL_SERVER_STEP_RESULT, bodyJson.toString(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.e(TAG, "sendCaseResult: 请求发送失败 ", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                response.close();
            }
        });
//        MExecutor.getGExecutor().submit(new Runnable() {
//            @Override
//            public void run() {
////                sendCaseResult(context, caseResult);
//                try {
//                    String s = HttpUtil.postSync(Constant.URL_SERVER_STEP_RESULT, bodyJson.toString());
//                    Log.d(TAG, "sendCaseResult: " + s);
//                } catch (IOException e) {
//                    LogUtil.d(TAG, String.format("upload case info fail: [%s]", bodyJson), e);
//                }
//            }
//        });

        // 写入文件
        FileUtil.writeString(caseResult.toJSONString(), String.format("%s/results/Json/%s/%s_%s.log",
                        GConfig.DATA_BASE_DIR,
                        caseResult.getCaseName(), caseResult.getCaseName(), System.currentTimeMillis()),
                StandardCharsets.UTF_8);
    }

    public static void sendStepsResult(Context context, CaseResult caseResult) {
        if (DEBUG) {
            return;
        }
        for (StepResult step : caseResult.getSteps().toJavaList(StepResult.class)) {
            JSONObject bodyJson = step.jsonForReport();
            LogUtil.d(TAG, "sendStepsResult: json: " + bodyJson);
            MExecutor.getGExecutor().submit(() -> {
                try {
                    String s = HttpUtil.postSync(Constant.URL_SERVER_STEP_RESULT, bodyJson.toString());
                    LogUtil.d(TAG, "sendStepsResult: " + s);
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtil.d(TAG, String.format("upload step log fail: [%s]", bodyJson), e);
                }
            });
        }
    }

//    public static void sendStepResult(Context context, StepResult stepResult) {
    public static void sendStepResult(StepResult stepResult) {
        JSONObject bodyJson = stepResult.jsonForReport();
        LogUtil.d(TAG, "sendStepResult: json: " + bodyJson);
        HttpUtil.post(Constant.URL_SERVER_STEP_RESULT, bodyJson.toString(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtil.e(TAG, "sendStepsResult: 请求发送失败 ", e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                response.close();
            }
        });
    }
}
