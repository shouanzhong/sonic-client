package com.autotest.sonicclient.handler;

import android.content.Context;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.CustomService;
import com.autotest.sonicclient.model.CaseResult;
import com.autotest.sonicclient.model.SuitResult;
import com.autotest.sonicclient.services.AdbServiceWrapper;
import com.autotest.sonicclient.services.MonitorService;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.InstrumentImpl;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.LogUtil;

@CustomService
public class SuitHandler {
    private static final String TAG = "SuitHandler";
    @Assemble
    static MonitorService monitorService;
    @Assemble
    static AdbServiceWrapper adbService;


    public static SuitResult runSuit(Context context, JSONObject suitInfo, MonitorService.StatusListener statusListener) {

        LogUtil.d(TAG, "runSuit: suitInfo: " + suitInfo);
        SuitResult suitResult = new SuitResult();
        suitResult.setSid(suitInfo.getInteger(Constant.KEY_SUIT_INFO_SID));

        monitorService.getStatus().setListener(statusListener);

        beforeTest(context, suitInfo);

        JsonParser.parseTestSuit(suitInfo, new JsonParser.SuitInfoListener() {
            @Override
            public void handle(JSONObject caseInfo) {
                CaseResult caseResult = CaseHandler.runCase(context, caseInfo);
//                ReportHandler.sendStepsResult(context, caseResult);
                try {
                    ReportHandler.sendCaseResult(context, caseResult);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                suitResult.getCases().add(caseResult);
            }

            @Override
            public boolean doBreak() {
                return !(monitorService.getStatus().isRunning());
            }
        });
        LogUtil.d(TAG, "runSuit: suitResult: " + suitResult);
        return suitResult;
    }

    private static void beforeTest(Context context, JSONObject suitInfo) {
        ReportHandler.createRemoteSuitResult(context, suitInfo);
        SystemClock.sleep(2000);
    }
}
