/*
 *   sonic-agent  Agent of Sonic Cloud Real Machine Platform.
 *   Copyright (C) 2022 SonicCloudOrg
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published
 *   by the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.autotest.sonicclient.handler;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.enums.ConditionEnum;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.interfaces.IStepHandler;
import com.autotest.sonicclient.model.ResultInfo;
import com.autotest.sonicclient.utils.LogUtil;

import java.util.List;

/**
 * if 条件步骤
 */
@HandlerService
public class IfHandler extends StepHandlerBase implements IStepHandler {
    private static final String TAG = "IfHandler";

    @Assemble
    private StepHandler stepHandler;
    @Assemble
    private StepHandlerWrapper stepHandlerWrapper;

    public IfHandler(Context context) {
        super(context);
    }

    @Override
    public ResultInfo runStep(JSONObject stepJSON, ResultInfo resultInfo) throws Throwable {
        if (isStopped()) {
            return null;
        }

        // 取出if下的步骤集合
        JSONObject conditionStep = stepJSON.getJSONObject("step");
        List<JSONObject> steps = conditionStep.getJSONArray("childSteps").toJavaList(JSONObject.class);
        // 执行条件步骤
        LogUtil.i(TAG, "runStep: 开始执行「if」步骤");
        LogUtil.d(TAG, String.format("runStep: stepJSON: %s", stepJSON));
        try {
            stepHandler.runStep(stepJSON, resultInfo);
        } catch (Throwable e) {
            resultInfo.setE(e);
        }
        // 上述步骤无异常则取出if下的步骤，再次丢给 StepHandlerWrapper 处理
        if (resultInfo.getE() == null) {
            LogUtil.i(TAG, "「if」步骤通过，开始执行子步骤");
            for (JSONObject step : steps) {
                stepHandlerWrapper.runStep(handlerPublicStep(step), resultInfo);
            }
            LogUtil.i(TAG, "runStep: 「if」子步骤执行完毕", "");
        } else {
            resultInfo.setE(new Exception("IGNORE:" + resultInfo.getE().getMessage()));
            LogUtil.w(TAG, "runStep: 「if」步骤执行失败，跳过", "");
        }
        return resultInfo;
    }

    @Override
    public ConditionEnum getCondition() {
        return ConditionEnum.IF;
    }
}
