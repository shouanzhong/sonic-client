package com.autotest.sonicclient.model;

import com.alibaba.fastjson2.JSONObject;

public class StepResult {
    String msg;
    String des;
    int status;
    String log;
    int cid;
    int rid;
    int deviceId;
    String udId;
    String pic;
    String logcatPath;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

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

    public String getUdId() {
        return udId;
    }

    public void setUdId(String udId) {
        this.udId = udId;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getLogcatPath() {
        return logcatPath;
    }

    public void setLogcatPath(String logcatPath) {
        this.logcatPath = logcatPath;
    }

    @Override
    public String toString() {
        return "StepResult{" +
                "msg='" + msg + '\'' +
                ", des='" + des + '\'' +
                ", status='" + status + '\'' +
                ", log='" + log + '\'' +
                ", cid=" + cid +
                ", rid=" + rid +
                ", udId='" + udId + '\'' +
                ", pic='" + pic + '\'' +
                ", logcatPath='" + logcatPath + '\'' +
                '}';
    }

    public String toJSONString() {
        return JSONObject.toJSONString(this);
    }

    public JSONObject jsonForReport() {
        JSONObject reportJson = new JSONObject();
        reportJson.put("caseId", getCid());
        reportJson.put("desc", getDes());
        reportJson.put("deviceId", getDeviceId());
        reportJson.put("log", getLog());
        reportJson.put("resultId", getRid());
        reportJson.put("status", getStatus());
        reportJson.put("type", "step");
        return reportJson;
    }
}
