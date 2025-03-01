package com.autotest.sonicclient.handler;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.model.CaseResult;
import com.autotest.sonicclient.model.ResultInfo;
import com.autotest.sonicclient.services.InjectorService;
import com.autotest.sonicclient.services.MonitorService;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.LogUtil;

import java.io.IOException;
import java.util.List;

@HandlerService
public class CaseHandler {
    private static final String TAG = "CaseHandler";
    @Assemble
    static StepHandlerWrapper stepHandlerWrapper;
    @Assemble
    static MonitorService monitorService;

    public CaseHandler(Context context) {
    }

    public static CaseResult runCase(Context context, JSONObject caseInfo) {
        LogHandler.clearLogcat();
        int cid = caseInfo.getInteger(Constant.KEY_CASE_INFO_CID);
        int rid = caseInfo.getInteger(Constant.KEY_CASE_INFO_RID);
        int deviceId = caseInfo.getInteger(Constant.KEY_CASE_INFO_DEVICE_ID);
        String caseName = caseInfo.getString(Constant.KEY_CASE_INFO_CASE_NAME);

        CaseResult caseResult = new CaseResult();
        caseResult.setCid(cid);
        caseResult.setRid(rid);
        caseResult.setDeviceId(deviceId);
        caseResult.setCaseName(caseName);

        ResultInfo resultInfo = new ResultInfo();
        resultInfo.setCaseId(cid);
        resultInfo.setResultId(rid);

        List<JSONObject> steps = JsonParser.parseStep(caseInfo);
        for (JSONObject step : steps) {
            LogUtil.i(TAG, "SuitInfoListener: step: " + step);
            stepHandlerWrapper.setRunning(monitorService.getStatus().isRunning());
            try {
//                resultInfo.clearStep();
                step.put(Constant.KEY_CASE_INFO_GLOBAL_PARAMS, caseInfo.getJSONObject("caseStepVo").get(Constant.KEY_CASE_INFO_GLOBAL_PARAMS));
                stepHandlerWrapper.runStep(step, resultInfo);
            } catch (Throwable e) {
                LogUtil.e(TAG, String.format("步骤[%s]执行异常: ", step.getJSONObject(Constant.KEY_STEP_INFO_STEP).getString(Constant.KEY_STEP_INFO_TYPE)), e);
                resultInfo.setE(e);
                resultInfo.packError();
                resultInfo.collect();
                break;
            }
        }
        String logPath = "";
        if (resultInfo.haveError()) {
            try {
                logPath = LogHandler.dumpLogcat(context, (String) caseInfo.get("desc"));
            } catch (IOException e) {
                e.printStackTrace();
                logPath = "Fail to dump logcat !! " + e.getMessage();
            }
        }
        JSONArray stepResultList = resultInfo.getStepResultList();
        caseResult.setSteps(stepResultList);
        caseResult.setLogUri(logPath);
        Log.i(TAG, "handle: case result:" + caseResult);
        return caseResult;
    }
}
