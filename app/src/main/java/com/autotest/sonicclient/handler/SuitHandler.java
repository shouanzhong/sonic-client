package com.autotest.sonicclient.handler;

import android.content.Context;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.model.CaseResult;
import com.autotest.sonicclient.model.SuitResult;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.LogUtil;

public class SuitHandler {
    private static final String TAG = "SuitHandler";


    public static SuitResult runSuit(Context context, JSONObject suitInfo) {
        SuitResult suitResult = new SuitResult();
        suitResult.setSid(suitInfo.getInteger(Constant.KEY_SUIT_INFO_SID));

        JsonParser.parseTestSuit(suitInfo, new JsonParser.SuitInfoListener() {
            @Override
            public void handle(JSONObject caseInfo) {
                CaseResult caseResult = CaseHandler.runCase(context, caseInfo);
                suitResult.getCases().add(caseResult);
            }
        });
        LogUtil.d(TAG, "handleActionRunSuit: " + suitResult);
        return suitResult;
    }
}
