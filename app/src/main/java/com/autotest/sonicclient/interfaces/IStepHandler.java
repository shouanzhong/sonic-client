package com.autotest.sonicclient.interfaces;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.enums.ConditionEnum;
import com.autotest.sonicclient.model.ResultInfo;

public interface IStepHandler {

    /**
     * 如果返回null则表示任务停止了
     */
    ResultInfo runStep(JSONObject step, ResultInfo resultInfo) throws Throwable;

    ConditionEnum getCondition();

    default JSONObject handlerPublicStep(JSONObject step) {
        if (step.containsKey("pubSteps")) {
            return step;
        }
        return new JSONObject() {
            {
                put("step", step);
            }
        };
    }
}
