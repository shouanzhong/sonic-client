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
        // 初始化界面组件
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        rememberMeCheckBox = findViewById(R.id.remember_me);

        // 检查 SharedPreferences 是否有保存的登录信息
        SharePreferenceUtil sharePreferenceUtil = SharePreferenceUtil.getInstance(this);
        if (sharePreferenceUtil.getBoolean(Constant.KEY_REMEMBER_ME, false)) {
            // 如果勾选了“记住密码”，自动填充用户名和密码
            usernameEditText.setText(sharePreferenceUtil.getString(Constant.KEY_USERNAME, ""));
            passwordEditText.setText(sharePreferenceUtil.getString(Constant.KEY_PASSWORD, ""));
            rememberMeCheckBox.setChecked(true);
        }

        // 设置登录按钮的点击事件
        loginButton.setOnClickListener(v -> {
            // 获取输入的用户名和密码
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            // 简单的验证：用户名和密码都不能为空
            if (username.isEmpty() || password.isEmpty()) {
                // 提示用户输入完整信息
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
            // 如果没有勾选“记住密码”，清除保存的用户名和密码
            SharePreferenceUtil.getInstance(this).clear(Constant.KEY_USERNAME, Constant.KEY_PASSWORD, Constant.KEY_SONIC_TOKEN, Constant.KEY_REMEMBER_ME);
//            editor.putBoolean(Constant.KEY_REMEMBER_ME, false);
        }
    }

    private void login(final String username, final String password) {
        JSONObject json = new JSONObject();
        try {
            json.put(Constant.KEY_USERNAME, username);
            json.put(Constant.KEY_PASSWORD, password);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 设置 MediaType 和 请求体
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, json.toString());

        // 构造请求
        Request request = new Request.Builder()
//                .url("http://172.16.63.29/api/controller/users/login")
                .url(Constant.URL_SERVER_LOGIN)
                .post(body)
//                .header("SonicToken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOlsidGVzdCIsIjI5NDEzOTRkLWNkMzItNGEzZS05M2ZjLWIyOGM0NDZmY2I1ZSJdLCJleHAiOjE3MzUwMjQ4NjR9.7IYckg3-_w-7VYIBoL1wzbJVUKXUq2l9wBIGUxtzGQw")
                .build();

        // 使用 OkHttpClient 发送请求
        client.newCall(request).enqueue(new okhttp3.Callback() {

            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                // 请求失败，处理错误
                Log.e(TAG, "Request failed: " + e.getMessage());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtil.showToast("Login request failed");
                    }
                });
            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    // 请求成功，处理返回的数据
                    String responseData = response.body().string();
                    Log.d(TAG, "Response: " + responseData);

                    // 在 UI 线程上更新 UI
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONObject responseJson = JSONObject.parse(responseData);
                                String dataString = (String) responseJson.getOrDefault("data", "");
                                if (!dataString.isEmpty()) {
                                    ToastUtil.showToast("Login successful");
                                    rememberMe(username, password, dataString);

                                    // 登录成功，跳转到主界面
                                    Intent intent = new Intent(LoginActivity.this, ProjectActivity.class);
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
                    // 请求失败，处理错误
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