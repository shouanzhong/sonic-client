package com.autotest.sonicclient.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.handler.ReportHandler;
import com.autotest.sonicclient.handler.SuitHandler;
import com.autotest.sonicclient.model.SuitResult;
import com.autotest.sonicclient.utils.LogUtil;

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

    // TODO: Rename parameters
    private static final String EXTRA_SUIT_INFO = "com.autotest.sonicclient.services.extra.PARAM1";

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

    @Override
    protected void onHandleIntent(Intent intent) {
        LogUtil.d(TAG, "onHandleIntent: ");
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RUN_SUIT.equals(action)) {
                final String suitString = intent.getStringExtra(EXTRA_SUIT_INFO);
                JSONObject suitInfo = JSONObject.parseObject(suitString);
                handleActionRunSuit(suitInfo);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionRunSuit(JSONObject suitInfo) {
        LogUtil.d(TAG, "handleActionRunSuit: ");
        SuitResult suitResult = SuitHandler.runSuit(this, suitInfo);
        ReportHandler.send(suitResult);
    }
}