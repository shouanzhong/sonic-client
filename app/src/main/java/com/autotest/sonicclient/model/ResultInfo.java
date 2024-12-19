package com.autotest.sonicclient.model;


import android.os.Build;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.enums.StepType;
import com.autotest.sonicclient.utils.CommonUtil;

public class ResultInfo {
    private String stepDes;
    private String detail;
    private Throwable e;
    private int caseId;
    private int resultId;
    private String pic;
    private JSONObject packInfo;

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

    public void reset() {
        clearStep();
        this.caseId = -1;
        this.resultId = -1;
        this.packInfo.clear();
    }

    public void clearStep() {
        this.stepDes = "";
        this.detail = "";
        this.e = null;
        this.pic = "";
    }

    public JSONObject pack(int status, String des, String detail) {
        packInfo = new JSONObject();
        packInfo.put("msg", "step");
        packInfo.put("des", des);
        packInfo.put("status", status);
        packInfo.put("log", detail);
        packInfo.put("cid", caseId);
        packInfo.put("rid", resultId);
        packInfo.put("udId", Build.getSerial());
        packInfo.put("pic", pic);
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
        this.pic = CommonUtil.takeShot(caseId + "");
        return pack(StepType.WARN, stepDes + "异常！");
    }

    public JSONObject packError() {
        this.pic = CommonUtil.takeShot(caseId + "");
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
