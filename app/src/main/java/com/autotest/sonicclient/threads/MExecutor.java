package com.autotest.sonicclient.threads;

import com.autotest.sonicclient.adblibs.AdbStream;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.ShellUtil;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MExecutor {
    private static final String TAG = "MExecutor";

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
            if (!executor.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                long l = System.currentTimeMillis() - start;
                LogUtil.w(TAG, "execCmd: 强制终止任务, 等待时长：" + l);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            LogUtil.e(TAG, "execCmd: interrupted 异常, 强制终止任务", e);
            executor.shutdownNow();
        }
        executor = null;
    }
}
