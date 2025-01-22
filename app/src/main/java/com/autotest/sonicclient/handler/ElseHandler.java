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
import com.autotest.sonicclient.handler.StepHandlerBase;
import com.autotest.sonicclient.handler.StepHandlerWrapper;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.interfaces.IStepHandler;
import com.autotest.sonicclient.model.ResultInfo;
import com.autotest.sonicclient.utils.LogUtil;


import java.util.List;

/**
 * else 条件步骤
 *
 * @author JayWenStar
 * @date 2022/3/13 2:30 下午
 */
@HandlerService
public class ElseHandler extends StepHandlerBase implements IStepHandler {
    private static final String TAG = "ElseHandler";

    @Assemble
    private StepHandlerWrapper stepHandlerWrapper;

    public ElseHandler(Context context) {
        super(context);
    }

    @Override
    public ResultInfo runStep(JSONObject stepJSON, ResultInfo resultInfo) throws Throwable {
        if (isStopped()) {
            return null;
        }
        // else 前应当必定有if，如果前面的if执行成功，则直接跳过
        if (resultInfo.getE() == null) {
            LogUtil.i(TAG, "runStep: 「else」前的条件步骤执行通过，「else」跳过");
            return resultInfo;
        }
        LogUtil.i(TAG, "「else」前的条件步骤失败，开始执行「else」下的步骤");

        // 取出 else 下所属的步骤，丢给stepHandlers处理
        List<JSONObject> steps = stepJSON.getJSONObject("step").getJSONArray("childSteps").toJavaList(JSONObject.class);
        // 上述步骤有异常则取出 else 下的步骤，再次丢给 stepHandlers 处理
        if (resultInfo.getE() != null) {
            resultInfo.clearStep();
            for (JSONObject step : steps) {
                stepHandlerWrapper.runStep(handlerPublicStep(step), resultInfo);
            }
        }
        LogUtil.i(TAG, "「else」步骤执行完毕");
        return resultInfo;
    }

    @Override
    public ConditionEnum getCondition() {
        return ConditionEnum.ELSE;
    }
}
