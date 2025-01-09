package com.autotest.sonicclient.handler;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.model.CaseResult;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.LogUtil;

import java.io.IOException;
import java.util.List;

public class CaseHandler {
    private static final String TAG = "CaseHandler";

    public static CaseResult runCase(Context context, JSONObject caseInfo) {
        LogHandler.clearLogcat();
        CaseResult caseResult = new CaseResult();
        StepHandler stepHandler = new StepHandler(context);
        int cid = caseInfo.getInteger(Constant.KEY_CASE_INFO_CID);
        int rid = caseInfo.getInteger(Constant.KEY_CASE_INFO_RID);
        stepHandler.getResultInfo().setCaseId(cid);
        stepHandler.getResultInfo().setResultId(rid);
        caseResult.setCid(cid);
        caseResult.setRid(rid);
        List<JSONObject> steps = JsonParser.parseStep(caseInfo);
        for (JSONObject step : steps) {
            LogUtil.i(TAG, "SuitInfoListener: step: " + step);
            try {
                stepHandler.runStep(step);
            } catch (Throwable e) {
                LogUtil.e(TAG, String.format("步骤[%s]执行异常: ", step.getJSONObject("step").getString("stepType")), e);
                stepHandler.getResultInfo().setE(e);
                stepHandler.getResultInfo().packError();
                stepHandler.getResultInfo().collect();
                break;
            }
        }
        String logPath = "";
        Throwable e1 = stepHandler.getResultInfo().getE();
        if (e1 != null) {
            try {
                logPath = LogHandler.dumpLogcat(context, (String) caseInfo.get("desc"));
            } catch (IOException e) {
                e.printStackTrace();
                logPath = "Fail to dump logcat !! " + e.getMessage();
            }
        }
        JSONArray stepResultList = stepHandler.getResultInfo().getStepResultList();
        caseResult.setSteps(stepResultList);
        caseResult.setLogUri(logPath);
        Log.i(TAG, "handle: case result:" + caseResult);
        return caseResult;
    }
}
