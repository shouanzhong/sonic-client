package com.autotest.sonicclient.model;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.enums.StepType;

public class CaseResult {
    int cid;
    int rid;
    int deviceId;
    String caseName;
    JSONArray steps;
    String logUri;
    boolean isPass;

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getRid() {
        return rid;
    }

    public void setRid(int rid) {
        this.rid = rid;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getCaseName() {
        return caseName;
    }

    public void setCaseName(String caseName) {
        this.caseName = caseName;
    }

    public JSONArray getSteps() {
        return steps;
    }

    public void setSteps(JSONArray steps) {
        this.steps = steps;
    }

    public String getLogUri() {
        return logUri;
    }

    public void setLogUri(String logUri) {
        this.logUri = logUri;
    }

    public boolean isPass() {
        if (steps.isEmpty()) {
            return true;
        }
        for (StepResult stepResult : steps.toJavaList(StepResult.class)) {
            if (stepResult.getStatus() == StepType.ERROR) {
                return false;
            }
        }
        return true;
    }

    public void setPass(boolean pass) {
        isPass = pass;
    }

    public String toJSONString() {
        return JSONObject.toJSONString(this);
    }
}
