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
import com.autotest.sonicclient.exceptions.SonicRespException;


import java.util.List;

/**
 * else if 条件步骤
 *
 * @author JayWenStar
 * @date 2022/3/13 2:23 下午
 */
@HandlerService
public class ElseIfHandler extends StepHandlerBase implements IStepHandler {
    private static final String TAG = "ElseIfHandler";

    @Assemble
    private StepHandler stepHandler;
    @Assemble
    private StepHandlerWrapper stepHandlerWrapper;

    public ElseIfHandler(Context context) {
        super(context);
    }

    @Override
    public ResultInfo runStep(JSONObject stepJSON, ResultInfo resultInfo) throws Throwable {
        if (isStopped()) {
            return null;
        }
        // else if 前应当必定有if，如果前面的if执行成功，则直接跳过
        if (resultInfo.getE() == null) {
            LogUtil.w(TAG, "runStep: 「else if」前的条件步骤执行通过，「else if」跳过");
            return resultInfo;
        }
        resultInfo.clearStep();

        // 取出 else if下的步骤集合
        JSONObject conditionStep = stepJSON.getJSONObject("step");
        List<JSONObject> steps = conditionStep.getJSONArray("childSteps").toJavaList(JSONObject.class);
        // 执行条件步骤
        LogUtil.i(TAG, "runStep: 开始执行「else if」步骤");
        try {
            stepHandlerWrapper.runStep(stepJSON, resultInfo);
        } catch (Throwable e) {
            resultInfo.setE(e);
        }
        // 上述步骤没有异常则取出else if下的步骤，再次丢给 stepHandlers 处理
        if (resultInfo.getE() == null) {
            LogUtil.i(TAG, "「else if」步骤通过，开始执行「else if」子步骤");
            resultInfo.clearStep();
            for (JSONObject step : steps) {
                stepHandlerWrapper.runStep(handlerPublicStep(step), resultInfo);
            }
            LogUtil.i(TAG, "「else if」子步骤执行完毕");

        } else {
            resultInfo.setE(new SonicRespException("IGNORE:" + resultInfo.getE().getMessage()));
            LogUtil.i(TAG, "「else if」步骤执行失败，跳过");
        }
        resultInfo.collect();
        return resultInfo;
    }

    @Override
    public ConditionEnum getCondition() {
        return ConditionEnum.ELSE_IF;
    }
}
