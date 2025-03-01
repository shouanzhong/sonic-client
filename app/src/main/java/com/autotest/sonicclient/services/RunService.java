package com.autotest.sonicclient.services;

import static com.autotest.sonicclient.services.RunServiceHelper.ACTION_RUN_SUIT;
import static com.autotest.sonicclient.services.RunServiceHelper.EXTRA_SUIT_INFO;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.R;
import com.autotest.sonicclient.activities.SuitActivity;
import com.autotest.sonicclient.handler.SuitHandler;
import com.autotest.sonicclient.model.SuitResult;
import com.autotest.sonicclient.receivers.StopServiceReceiver;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.utils.LogUtil;

public class RunService extends ServiceBase {
    private static final String TAG = "RunService";
    private static final String CHANNEL_ID = "RunServiceChannel";
    private NotificationCompat.Builder notificationBuilder;
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager manager;

    @Override
    public void onCreate() {
        super.onCreate();
        manager = getSystemService(NotificationManager.class);
        startForegroundService();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_RUN_SUIT.equals(action)) {
                final String suitString = intent.getStringExtra(EXTRA_SUIT_INFO);
                JSONObject suitInfo = JSONObject.parseObject(suitString);
                HandlerThread handlerThread = new HandlerThread("RunServiceThread");
                handlerThread.start();
                Handler handler = new Handler(handlerThread.getLooper());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        isRunning = true;
                        // 后台操作
                        handleActionRunSuit(suitInfo);
                        isRunning = false;
                    }
                });
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void handleActionRunSuit(JSONObject suitInfo) {
        LogUtil.d(TAG, "handleActionRunSuit: ");
        SuitResult suitResult = SuitHandler.runSuit(this, suitInfo, new MonitorService.StatusListener() {
            @Override
            public boolean isRunning() {
                return isRunning;
            }
        });
        // ReportHandler.send(suitResult);
        updateNotificationContext("测试完成");
    }

    private void startForegroundService() {
        createNotificationChannel();
        // 停止服务
        Intent stopIntent = new Intent(this, StopServiceReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        // 下拉菜单
        Intent notificationIntent = new Intent(this, SuitActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(Constant.APP_NAME)
                .setContentText("测试中...")
                .setSmallIcon(R.drawable.ic_baseline_run_circle_24)
                .setColor(ContextCompat.getColor(this, R.color.purple_200))
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.ic_noti_stop, "停止", stopPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);;
        Notification notification = notificationBuilder
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Run Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            manager.createNotificationChannel(serviceChannel);
        }
    }

    private void updateNotificationContext(String text) {
        Notification notification = notificationBuilder.setContentText(text).build();
        manager.notify(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        Log.i(TAG, "onDestroy: ");
    }
}