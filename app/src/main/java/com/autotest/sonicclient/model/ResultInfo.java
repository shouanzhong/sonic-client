package com.autotest.sonicclient.model;


import android.os.Build;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.application.ApplicationImpl;
import com.autotest.sonicclient.enums.StepType;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.DeviceUtil;

/**
 * Step result info
 */
public class ResultInfo {
    private String stepDes;
    private String detail = "";
    private Throwable e;
    private int caseId;
    private int resultId;
    private String pic;
    private JSONObject packInfo;
    private final JSONArray stepResultList = new JSONArray();
    private String logPath;
    private boolean haveError = false;  // case 级别标识

    public String getStepDes() {
        return stepDes;
    }

    public void setStepDes(String stepDes) {
        this.stepDes = stepDes;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Throwable getE() {
        return e;
    }

    public void setE(Throwable e) {
        this.e = e;
    }

    public int getCaseId() {
        return caseId;
    }

    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public JSONObject getPackInfo() {
        return packInfo;
    }

    public JSONArray getStepResultList() {
        return stepResultList;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public boolean haveError() {
        return haveError;
    }

    public void setError(boolean haveError) {
        this.haveError = haveError;
    }

    public void reset() {
        clearStep();
        this.caseId = -1;
        this.resultId = -1;
        this.packInfo.clear();
        this.haveError = false;
    }

    public void clearStep() {
        this.stepDes = "";
        this.detail = "";
        this.e = null;
        this.pic = "";
    }

    public void collect() {
        stepResultList.add(packInfo);
    }

    public JSONObject pack(int status, String des, String detail) {
        packInfo = new JSONObject();
        packInfo.put(Constant.KEY_STEP_RESULT_MSG, "step");
        packInfo.put(Constant.KEY_STEP_RESULT_DES, des);
        packInfo.put(Constant.KEY_STEP_RESULT_STATUS, status);
        packInfo.put(Constant.KEY_STEP_RESULT_LOG_DETAIL, String.format("%s %s", des, detail));
        packInfo.put(Constant.KEY_STEP_RESULT_CID, caseId);
        packInfo.put(Constant.KEY_CASE_INFO_RID, resultId);
        packInfo.put(Constant.KEY_STEP_RESULT_UDID, Build.getSerial());
        packInfo.put(Constant.KEY_STEP_RESULT_PIC, pic);
        packInfo.put(Constant.KEY_STEP_RESULT_LOGCAT, logPath);
        return packInfo;
    }

    public JSONObject pack(int status, String des) {
        return pack(status, des, stepDes);
    }

    public JSONObject pack(int status) {
        return pack(status, stepDes, detail);
    }

    public JSONObject packPass() {
        return pack(StepType.PASS, stepDes, detail);
    }

    public JSONObject packIgnore() {
        return pack(StepType.PASS, stepDes + "异常！已忽略...", detail);
    }

    public JSONObject packWarning() {
        this.pic = DeviceUtil.takeShot(caseId + "");
        haveError = true;
        return pack(StepType.WARN, stepDes + " 警告！");
    }

    public JSONObject packError() {
        this.pic = DeviceUtil.takeShot(caseId + "");
        haveError = true;
        return pack(StepType.ERROR, stepDes + "异常！", detail);
    }

    @Override
    public String toString() {
        return "ResultInfo{" +
                "stepDes='" + stepDes + '\'' +
                ", detail='" + detail + '\'' +
                ", e=" + e +
                ", caseId=" + caseId +
                ", resultId=" + resultId +
                ", pic='" + pic + '\'' +
                '}';
    }
}
