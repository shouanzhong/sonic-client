package com.autotest.sonicclient.handler;

import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.application.ApplicationImpl;
import com.autotest.sonicclient.model.CaseResult;
import com.autotest.sonicclient.model.SuitResult;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.HttpUtil;
import com.autotest.sonicclient.utils.MinioUtil;

import java.io.File;
import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ReportHandler {
    private static final String TAG = "ReportHandler";
    public static String REPORT_URL = "";
    public static String LOG_FILE_URL = "";

    public static void send(SuitResult suitResult) {
        sendLog(suitResult);
        sendResult(REPORT_URL, suitResult);
    }

    private static void sendLog(SuitResult suitResult) {

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
                MinioUtil.builder().setBucketName(ApplicationImpl.getInstance().getPackageName())
                        .build().upload(file.getAbsolutePath(), file.getName());
                caseInfo.setLogUri(file.getName());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "sendLog: fail: " + file, e);
            }
        }
    }

    private static void sendResult(String url, SuitResult suitResult) {
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
}
