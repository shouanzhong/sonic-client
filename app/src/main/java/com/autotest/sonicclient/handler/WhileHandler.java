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

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.enums.ConditionEnum;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.interfaces.IStepHandler;
import com.autotest.sonicclient.model.ResultInfo;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ToastUtil;


import java.util.List;

/**
 * while 条件步骤
 *
 * @author JayWenStar
 * @date 2022/3/13 2:33 下午
 */
@HandlerService
public class WhileHandler extends StepHandlerBase implements IStepHandler {
    private static final String TAG = "WhileHandler";

    @Assemble
    private StepHandler stepHandler;
    @Assemble
    private StepHandlerWrapper stepHandlerWrapper;

    public WhileHandler(Context context) {
        super(context);
    }

    @Override
    public ResultInfo runStep(JSONObject stepJSON, ResultInfo resultInfo) throws Throwable {
        if (isStopped()) {
            return null;
        }

        // 取出 while 下的步骤集合
        JSONObject conditionStep = stepJSON.getJSONObject("step");
        List<JSONObject> steps = conditionStep.getJSONArray("childSteps").toJavaList(JSONObject.class);
        // 设置了判断条件步骤，则先运行判断条件的步骤
        LogUtil.i(TAG, "开始执行「while」步骤");

        try {
            stepHandlerWrapper.runStep(stepJSON, resultInfo);
        } catch (Throwable e) {
            resultInfo.setE(e);
        }
        int i = 1;
        while (resultInfo.getE() == null && isRunning()) {
            // 条件步骤成功，取出while下所属的步骤丢给stepHandlers处理
            LogUtil.i(TAG, "「while」步骤通过，开始执行第「" + i + "」次子步骤循环");

            for (JSONObject step : steps) {
                stepHandlerWrapper.runStep(handlerPublicStep(step), resultInfo);
            }
            LogUtil.i(TAG, "第「" + i + "」次子步骤执行完毕");

            resultInfo.clearStep();
            LogUtil.i(TAG, "开始执行第「" + (i + 1) + "」次「while」步骤");
            stepHandlerWrapper.runStep(stepJSON, resultInfo);

            i++;
        }
        if (resultInfo.getE() != null) {
            if ("exit while".equals(resultInfo.getE().getMessage())) {
                resultInfo.setE(null);
                LogUtil.w(TAG, "「while」步骤执行退出，循环结束", "");
            } else {
                LogUtil.w(TAG, "「while」步骤执行失败，循环结束", "");
            }
        }
        if (isStopped()) {
            ToastUtil.showToast(contextWeakReference.get(), "「while」被强制中断");
            LogUtil.w(TAG, "「while」被强制中断", "");
        }
        // 不满足条件则返回
        return resultInfo;
    }

    @Override
    public ConditionEnum getCondition() {
        return ConditionEnum.WHILE;
    }
}
