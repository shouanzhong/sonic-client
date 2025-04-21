package com.autotest.sonicclient.utils.http;

import android.util.Log;
import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpRetryInterceptor implements Interceptor {
    private final int maxRetryTime = 120; // 最长等待 120 秒
    private final int retryInterval = 5;  // 每次等待 5 秒
    private final int maxAttempts = maxRetryTime / retryInterval; // 最大尝试次数

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        IOException exception = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                Response response = chain.proceed(request);
                if (response.isSuccessful()) {
                    return response;
                } else {
                    Log.e("OKHTTP-RETRY", "请求失败，HTTP Code: " + response.code() + "，尝试第 " + attempt + " 次...");
                }
            } catch (IOException e) {
                exception = e;
                Log.e("OKHTTP-RETRY", "请求异常：" + e.getMessage() + "，尝试第 " + attempt + " 次...");
            }

            try {
                TimeUnit.SECONDS.sleep(retryInterval); // 等待 retryInterval 秒
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("请求被中断", e);
            }
        }

        throw new IOException("请求失败，已尝试 " + maxAttempts + " 次", exception);
    }
}

