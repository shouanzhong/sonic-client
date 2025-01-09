package com.autotest.sonicclient.model;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;

public class SuitResult {
    String msg= "result";
    int sid;
    JSONArray cases = new JSONArray();

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public JSONArray getCases() {
        return cases;
    }

    public void setCases(JSONArray cases) {
        this.cases = cases;
    }

    @NonNull
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }
}
