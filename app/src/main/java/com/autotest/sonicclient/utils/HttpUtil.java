package com.autotest.sonicclient.utils;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import com.autotest.sonicclient.config.GConfig;

public class HttpUtil {
    private static final String TAG = "HttpUtil";
    private static OkHttpClient _instance;
    static public final String REMOTE_SERVER = "http://172.16.142.253:8000";

    /**
     * 获取HttpClient
     * @return
     */
    public static OkHttpClient getHttpClient() {
        if (_instance == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .retryOnConnectionFailure(true)
                    .cache(null)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS);

            _instance = builder.build();
        }

        return _instance;
    }

    /**
     * get请求
     * @param url 请求地址
     * @param callback 回调
     */
    public static void get(String url, Headers headers, okhttp3.Callback callback) {
        if (TextUtils.isEmpty(url)) {
            LogUtil.e(TAG, "无法解析空连接");
            return;
        }
        Headers.Builder builder = headers.newBuilder();
        builder.add(Constant.KEY_SONIC_TOKEN, GConfig.SONIC_TOKEN);
        headers = builder.build();

        OkHttpClient client = getHttpClient();
        Request request = new Request.Builder()
                .get().url(url).headers(headers).build();

        client.newCall(request).enqueue(callback);
    }

    public static void get(String url, okhttp3.Callback callback) {
        Headers headers = Headers.of(Constant.KEY_SONIC_TOKEN, GConfig.SONIC_TOKEN);
        get(url, headers, callback);
    }

    /**
     * get请求
     * @param url 请求地址
     */
    public static String getSync(String url) throws IOException {
        if (TextUtils.isEmpty(url)) {
            LogUtil.e(TAG, "无法解析空连接");
            return null;
        }

        OkHttpClient client = getHttpClient();
        Request request = new Request.Builder()
                .get().url(url).header(Constant.KEY_SONIC_TOKEN, GConfig.SONIC_TOKEN).build();

        ResponseBody body = client.newCall(request).execute().body();
        if (body == null) {
            return null;
        }
        return body.string();
    }

    /**
     * post请求
     * @param url 请求地址
     * @param body 参数
     * @param callback 回调
     */
    public static void post(String url, RequestBody body, okhttp3.Callback callback) {
        if (TextUtils.isEmpty(url)) {
            LogUtil.e(TAG, "无法解析空连接");
            return;
        }

        OkHttpClient client = getHttpClient();
        Request request = new Request.Builder()
                .post(body).url(url).header(Constant.KEY_SONIC_TOKEN, GConfig.SONIC_TOKEN).build();

        client.newCall(request).enqueue(callback);
    }

    public static void post(String url, String jsonString, okhttp3.Callback callback) {
        MediaType type = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(type, jsonString);
        post(url, body, callback);
    }

    /**
     * 同步post
     * @param url
     * @param body
     * @return
     * @throws IOException
     */
    public static String postSync(String url, RequestBody body) throws IOException {
        if (TextUtils.isEmpty(url)) {
            LogUtil.e(TAG, "无法解析空连接");
            return null;
        }

        OkHttpClient client = getHttpClient();
        Request request = new Request.Builder()
                .post(body).url(url).header(Constant.KEY_SONIC_TOKEN, GConfig.SONIC_TOKEN).build();

        ResponseBody resBody = client.newCall(request).execute().body();
        if (resBody == null) {
            return null;
        }
        return resBody.string();
    }

    public static abstract class Callback<T> implements okhttp3.Callback {

        @Override
        public void onResponse(Call call, Response response) throws IOException {
            String bodyString = null;
            if (!response.isSuccessful() || !(JSONObject.parse(bodyString = response.body().string()).getInteger("code") + "").startsWith("2")) {
                onFailure(call, new IOException(String.format("Received http response :" +
                        "\nresponse: %s" +
                        "\nbody: %s",
                        response, bodyString)));
                return;
            }
            LogUtil.d(TAG, String.format("onResponse: %s", bodyString));

            JSONObject jsonObject = JSONObject.parse(bodyString);
            T data = (T) jsonObject.get("data");
            onResponse(call, data);
        }

        public abstract void onResponse(Call call, T item) throws IOException;

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            LogUtil.e(TAG, "request error ", e);
            ToastUtil.showToast("请求发送失败");
//            SharePreferenceUtil.getInstance(ApplicationImpl.getInstance()).clear(Constant.KEY_SONIC_TOKEN);
//            GConfig.SONIC_TOKEN = "";
        }
    }
}
