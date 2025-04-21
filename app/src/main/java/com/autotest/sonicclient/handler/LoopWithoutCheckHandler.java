package com.autotest.sonicclient.handler;

import android.content.Context;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.config.GConfig;
import com.autotest.sonicclient.enums.ConditionEnum;
import com.autotest.sonicclient.enums.StepType;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.interfaces.IStepHandler;
import com.autotest.sonicclient.model.ResultInfo;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ToastUtil;

import java.util.List;

@HandlerService
public class LoopWithoutCheckHandler extends StepHandlerBase implements IStepHandler {
    private static final String TAG = "LoopWithoutCheckHandler";

    @Assemble
    private StepHandler stepHandler;
    @Assemble
    private StepHandlerWrapper handlerWrapper;

    public LoopWithoutCheckHandler(Context context) {
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
        LogUtil.i(TAG, "开始执行「无条件循环」步骤");
        JSONObject gp = stepJSON.getJSONObject(Constant.KEY_CASE_INFO_GLOBAL_PARAMS);
        int cycleTime = (Integer) gp.getOrDefault("cycleTime", -1);
        Integer cycleCount = gp.getInteger("cycleCount");
        // 内存分析
        if (cycleTime != -1) {
            long start = System.currentTimeMillis();
            long end = start + (long) (cycleTime * 3600 * 1000);
            int i = 1;
            while (System.currentTimeMillis() < end && resultInfo.getE() == null && !isStopped()) {
                LogUtil.i(TAG, "开始执行第「" + i + "」次「无条件循环」步骤", "");
                handlerWrapper.runStep(stepJSON, resultInfo);
                LogUtil.i(TAG, "第「" + i + "」次子步骤执行完毕", "");
//                resultInfo.clearStep();
                i++;
            }
            LogUtil.i(TAG, "合计循环「" + (i - 1) + "」次「无条件循环」步骤", "");
        } else {
            // 如果是性能分析传递单场景次数，就以单场景次数为准
            Integer whileCount = cycleCount != null ? cycleCount : conditionStep.getInteger("whileCount");
            if (whileCount > 0 && isRunning()) {
                int i = 1;
                while (resultInfo.getE() == null && !isStopped()) {
                    stepJSON.put("count", i);

                    LogUtil.i(TAG, "开始执行第「" + i + "」次「无条件循环」步骤", "");
                    handlerWrapper.runStep(stepJSON, resultInfo);
                    LogUtil.i(TAG, "第「" + i + "」次子步骤执行完毕", "");

                    i++;
                    if (i > whileCount) {
                        LogUtil.i(TAG, "合计循环「" + (i - 1) + "」次「无条件循环」步骤", "");
                        break;
                    }
                }
            }
        }
//        int i = 1;
//        while (resultInfo.getE() == null && isRunning()) {
//            // 条件步骤成功，取出while下所属的步骤丢给stepHandlers处理
//            LogUtil.i(TAG, "「无条件循环」步骤通过，开始执行第「" + i + "」次子步骤循环");
//
//            for (JSONObject step : steps) {
//                handlerWrapper.runStep(handlerPublicStep(step), resultInfo);
//            }
//            LogUtil.i(TAG, "第「" + i + "」次子步骤执行完毕");
//
//            resultInfo.clearStep();
//            LogUtil.i(TAG, "开始执行第「" + (i + 1) + "」次「无条件循环」步骤");
//            handlerWrapper.runStep(stepJSON, resultInfo);
//
//            i++;
//        }
        if (resultInfo.getE() != null) {
            LogUtil.w(TAG, "「无条件循环」步骤执行失败，循环结束", "");
        } else {
            LogUtil.w(TAG, "「无条件循环」步骤执行成功，循环结束", "");
        }
        if (isStopped()) {
            ToastUtil.showToast(contextWeakReference.get(), "「无条件循环」被强制中断");
            LogUtil.w(TAG, "「无条件循环」被强制中断", "");
        }
        // 不满足条件则返回
        return resultInfo;
    }

    @Override
    public ConditionEnum getCondition() {
        return ConditionEnum.LOOP_WITHOUT_CHECK;
    }
}
