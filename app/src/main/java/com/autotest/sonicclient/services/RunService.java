package com.autotest.sonicclient.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.handler.StepHandler;
import com.autotest.sonicclient.utils.JsonParser;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ToastUtil;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class RunService extends IntentService {
    private static final String TAG = "RunService";

    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_RUN_SUIT = "com.autotest.sonicclient.services.action.RUN_SUIT";
    private static final String ACTION_BAZ = "com.autotest.sonicclient.services.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_SUIT_INFO = "com.autotest.sonicclient.services.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "com.autotest.sonicclient.services.extra.PARAM2";

    public RunService() {
        super("RunService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionRunSuit(Context context, JSONObject suitInfo) {
        LogUtil.d(TAG, "startActionRunSuit: suitInfo: " + suitInfo);
        Intent intent = new Intent(context, RunService.class);
        intent.setAction(ACTION_RUN_SUIT);
        intent.putExtra(EXTRA_SUIT_INFO, suitInfo.toJSONString());
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, RunService.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_SUIT_INFO, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtil.d(TAG, "onHandleIntent: ");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RUN_SUIT.equals(action)) {
//                final JSONObject suitInfo = (JSONObject) intent.getExtras().get(EXTRA_SUIT_INFO);
                final String suitString = intent.getStringExtra(EXTRA_SUIT_INFO);
                JSONObject suitInfo = JSONObject.parseObject(suitString);
                handleActionRunSuit(suitInfo);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_SUIT_INFO);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRunSuit(JSONObject suitInfo) {
        LogUtil.d(TAG, "handleActionRunSuit: ");
        StepHandler stepHandler = new StepHandler(this);
        JsonParser.parseTestSuit(suitInfo, new JsonParser.SuitInfoListener() {
            @Override
            public void handle(JSONObject caseInfo) {
                int rid = caseInfo.getInteger("rid");
                int cid = caseInfo.getInteger("cid");
                stepHandler.getResultInfo().setCaseId(cid);
                stepHandler.getResultInfo().setResultId(rid);
                List<JSONObject> steps = JsonParser.parseStep(caseInfo);
                for (JSONObject step : steps) {
                    LogUtil.i(TAG, "SuitInfoListener: step: " + step);
                    try {
                        stepHandler.runStep(step);
                    } catch (Throwable e) {
                        LogUtil.e(TAG, String.format("步骤[%s]执行异常: ", step.getJSONObject("step").getString("stepType")), e);
                        stepHandler.getResultInfo().setE(e);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}