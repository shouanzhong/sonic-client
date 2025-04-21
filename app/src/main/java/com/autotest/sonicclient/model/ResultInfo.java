package com.autotest.sonicclient.model;


import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.enums.StepType;
import com.autotest.sonicclient.handler.ReportHandler;
import com.autotest.sonicclient.services.DeviceService;
import com.autotest.sonicclient.services.InjectorService;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.LogUtil;


/**
 * Step result info
 */
public class ResultInfo {
    private static final String TAG = "ResultInfo";
    private String stepType;
    private String prefixStepDes;
    private String stepDes;
    private String detail = "";
    private Throwable e;
    private int caseId;
    private int resultId;
    private int deviceId;
    private String pic;
    private JSONObject packInfo;
    private final JSONArray stepResultList = new JSONArray();
    private String logPath;
    private boolean haveError = false;  // case 级别标识

    public String getStepType() {
        return stepType;
    }

    public void setStepType(String stepType) {
        this.stepType = stepType;
        LogUtil.i(TAG, String.format("步骤类型 [%s]", this.stepType));
    }

    public String getPrefixStepDes() {
        return prefixStepDes;
    }

    public void setPrefixStepDes(String prefixStepDes) {
        this.prefixStepDes = prefixStepDes;
    }

    public String getStepDes() {
        return stepDes;
    }

    public void setStepDes(String stepDes) {
        this.stepDes = TextUtils.isEmpty(this.prefixStepDes) ? stepDes : this.prefixStepDes + ": " + stepDes;
        LogUtil.i(TAG, this.stepDes);
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
        LogUtil.i(TAG, this.detail);
    }

    public Throwable getE() {
        return e;
    }

    public void setE(Throwable e) {
        LogUtil.e(TAG, String.format("步骤 [%s] [%s] 执行失败", stepType, stepDes), e);
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

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
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
        this.prefixStepDes = "";
        clearStep();
        this.caseId = -1;
        this.resultId = -1;
        this.deviceId = -1;
        this.packInfo.clear();
        this.haveError = false;
    }

    public void clearStep() {
        this.stepType = "";
        this.stepDes = "";
        this.detail = "";
        this.e = null;
        this.pic = "";
    }

    public void collect() {
        if (TextUtils.isEmpty(stepDes)) return;
        stepResultList.add(packInfo);
        // 调试性能
        ReportHandler.sendStepResult(packInfo.toJavaObject(StepResult.class));
        this.stepDes = "";
    }

    public JSONObject pack(int status, String des, String detail) {
        packInfo = new JSONObject();
        packInfo.put(Constant.KEY_STEP_RESULT_MSG, "step");
        packInfo.put(Constant.KEY_STEP_RESULT_DES, des);
        packInfo.put(Constant.KEY_STEP_RESULT_STATUS, status);
        packInfo.put(Constant.KEY_STEP_RESULT_LOG_DETAIL, String.format("%s %s", des, detail));
        packInfo.put(Constant.KEY_STEP_RESULT_CID, caseId);
        packInfo.put(Constant.KEY_CASE_INFO_RID, resultId);
        packInfo.put(Constant.KEY_CASE_INFO_DEVICE_ID, deviceId);
        packInfo.put(Constant.KEY_STEP_RESULT_UDID, "unknown");  // Build.getSerial()
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
        this.pic = InjectorService.getService(DeviceService.class).takeShot(stepDes);
        haveError = true;
        return pack(StepType.WARN, stepDes + " 警告！");
    }

    public JSONObject packError() {
        this.pic = InjectorService.getService(DeviceService.class).takeShot(stepDes);
        haveError = true;
        LogUtil.e(TAG,  stepType + " " + stepDes + " 异常！ " + detail + " pic: " + pic);
        return pack(StepType.ERROR, stepDes + " 异常！", detail);
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
