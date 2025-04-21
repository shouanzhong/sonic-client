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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.enums.ConditionEnum;
import com.autotest.sonicclient.enums.SonicEnum;
import com.autotest.sonicclient.interfaces.CollectionObject;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.interfaces.IStepHandler;
import com.autotest.sonicclient.model.ResultInfo;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.LogUtil;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@HandlerService
public class StepHandlerWrapper extends StepHandlerBase {
    private static final String TAG = "StepHandlerWrapper";
    private boolean recordStop = false;
    private final ConcurrentHashMap<ConditionEnum, IStepHandler> stepHandlersMap =
            new ConcurrentHashMap<>(8);

    public StepHandlerWrapper(Context context) {
        super(context);
    }

    public ResultInfo runStep(JSONObject stepJSON, ResultInfo resultInfo) throws Throwable {
        resultInfo.clearStep();

        if (isStopped() && !recordStop) {
            recordStop = true;
            return stopStep(resultInfo);
        }
        JSONObject step = stepJSON.getJSONObject(Constant.KEY_STEP_INFO_STEP);
        LogUtil.d(TAG, "StepHandlerWrapper runStep: %s", step.toJSONString());
        // 兼容childSteps
        if (JsonParser.isJSONObjectEmpty(step)) {
            step = stepJSON;
        }
        Integer conditionType = step.getInteger(Constant.KEY_STEP_INFO_CONDITION_TYPE);
        getSupportedCondition(SonicEnum.valueToEnum(ConditionEnum.class, conditionType))
                .runStep(stepJSON, resultInfo);
        resultInfo.collect();
        return resultInfo;
    }

    @NonNull
    private ResultInfo stopStep(ResultInfo resultInfo) {
        resultInfo.setStepDes("#### App用例执行被手动停止 ####");
        resultInfo.packError();
        resultInfo.collect();
        return resultInfo;
    }

    @CollectionObject(IStepHandler.class)
    public StepHandlerWrapper addConditionHandlers(@Nullable Collection<IStepHandler> stepHandlers) {
        if (!stepHandlers.isEmpty()) {
            for (IStepHandler handler : stepHandlers) {
                if (this.stepHandlersMap.containsKey(handler.getCondition())) {
                    throw new RuntimeException("Same condition type implements must be unique");
                }
                LogUtil.d(TAG, "addConditionHandlers: handler: {}", handler);
                this.stepHandlersMap.put(handler.getCondition(), handler);
            }
        }
        return this;
    }

    private IStepHandler getSupportedCondition(ConditionEnum conditionEnum) {
        LogUtil.d(TAG, "getSupportedCondition: conditionEnum: {}", conditionEnum);
        IStepHandler handler = stepHandlersMap.getOrDefault(conditionEnum, null);
        if (handler == null) {
            throw new RuntimeException("condition handler for 「" + conditionEnum + "」 not found");
        }
        return handler;
    }
}
