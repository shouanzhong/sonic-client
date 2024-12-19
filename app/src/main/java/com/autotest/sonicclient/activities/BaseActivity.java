package com.autotest.sonicclient.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.autotest.sonicclient.config.GConfig;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String sonicToken = GConfig.SONIC_TOKEN;
        if (sonicToken == null) {
            redirectLogin();
        }
    }

    protected void redirectLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}