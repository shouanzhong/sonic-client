package com.autotest.sonicclient.threads;

import com.autotest.sonicclient.utils.LogUtil;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MExecutor {
    private static final String TAG = "MExecutor";
    static ExecutorService gExecutor = Executors.newSingleThreadExecutor();

    public static void execute(Runnable runnable) {
        execute(runnable, 5000);
    }

    public static void execute(Runnable runnable, long timeout) {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(runnable);
        executor.shutdown();
        try {
            // 等待
            long start = System.currentTimeMillis();
            if (timeout < 0) {
                while (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    LogUtil.i(TAG, "等待任务完成...");
                }
            } else if (!executor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                long l = System.currentTimeMillis() - start;
                LogUtil.w(TAG, "execCmd: 强制终止任务, 等待时长：" + l);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LogUtil.w(TAG, "execCmd: interrupted 异常, 强制终止任务", e);
            executor.shutdownNow();
        }
        executor = null;
    }

    public static ExecutorService getGExecutor() {
        return gExecutor;
    }
}
