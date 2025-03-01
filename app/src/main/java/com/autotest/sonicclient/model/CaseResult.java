package com.autotest.sonicclient.model;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

public class CaseResult {
    int cid;
    int rid;
    int deviceId;
    String caseName;
    JSONArray steps;
    String logUri;

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

    public String toJSONString() {
        return JSONObject.toJSONString(this);
    }
}
