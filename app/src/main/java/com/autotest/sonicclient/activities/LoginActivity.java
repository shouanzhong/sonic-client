package com.autotest.sonicclient.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.R;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.config.GConfig;
import com.autotest.sonicclient.utils.SharePreferenceUtil;
import com.autotest.sonicclient.utils.ToastUtil;

import java.io.IOException;

import cn.hutool.crypto.SecureUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private CheckBox rememberMeCheckBox;

    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        client = new OkHttpClient();
        // 初始化
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        rememberMeCheckBox = findViewById(R.id.remember_me);

        // 检查
        SharePreferenceUtil sharePreferenceUtil = SharePreferenceUtil.getInstance(this);
        if (sharePreferenceUtil.getBoolean(Constant.KEY_REMEMBER_ME, false)) {
            // 填充
            usernameEditText.setText(sharePreferenceUtil.getString(Constant.KEY_USERNAME, ""));
            passwordEditText.setText(sharePreferenceUtil.getString(Constant.KEY_PASSWORD, ""));
            rememberMeCheckBox.setChecked(true);
        }

        // 登录
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // 都不能为空
            if (username.isEmpty() || password.isEmpty()) {
                ToastUtil.showToast("Please enter both username and password");
            } else {
                login(username, password);
            }
        });
    }

    private void rememberMe(String username, String password, String token) {
        if (rememberMeCheckBox.isChecked()) {
            // 如果勾选了“记住密码”，保存用户名、密码和状态
            SharePreferenceUtil.getInstance(this).putMessage(editor -> {
                editor.putString(Constant.KEY_USERNAME, username);
                editor.putString(Constant.KEY_PASSWORD, password);
                editor.putBoolean(Constant.KEY_REMEMBER_ME, true);
                editor.putString(Constant.KEY_SONIC_TOKEN, token);
                GConfig.SONIC_TOKEN = token;
            });
        } else {
            // 清除用户名密码
            SharePreferenceUtil.getInstance(this).clear(Constant.KEY_USERNAME, Constant.KEY_PASSWORD, Constant.KEY_SONIC_TOKEN, Constant.KEY_REMEMBER_ME);
//            editor.putBoolean(Constant.KEY_REMEMBER_ME, false);
        }
    }

    private void login(final String username, final String password) {
        JSONObject json = new JSONObject();
        try {
            json.put(Constant.KEY_USERNAME, username);
            json.put(Constant.KEY_PASSWORD, SecureUtil.aes(Constant.AES_KEY.getBytes()).encryptHex(password));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 设置 MediaType 和 请求体
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json.toString());

        Request request = new Request.Builder()
                .url(Constant.URL_SERVER_LOGIN)
                .post(body)
                .build();

        // 发送请求
        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Request failed: ", e);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast("登录失败，请检查账号和网络");
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    Log.d(TAG, "Response: " + responseData);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject responseJson = JSONObject.parse(responseData);
                                String dataString = (String) responseJson.getOrDefault("data", "");
                                if (!dataString.isEmpty()) {
                                    // 登录成功
                                    ToastUtil.showToast("Login successful");
                                    rememberMe(username, password, dataString);

                                    Intent intent = new Intent(LoginActivity.this, SuitActivity.class);
                                    startActivity(intent);
                                    finish(); // 结束当前 Activity
                                } else {
                                    ToastUtil.showToast("Invalid username or password");
                                }
                            } catch (Exception e) {
                                ToastUtil.showToast("Login failed");
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "Request failed: " + response.message());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtil.showToast("Login request failed");
                        }
                    });
                }
            }
        });
    }
}