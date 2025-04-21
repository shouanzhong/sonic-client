package com.autotest.sonicclient.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import com.autotest.sonicclient.MainActivity;
import com.autotest.sonicclient.config.GConfig;
import com.autotest.sonicclient.services.AdbService;
import com.autotest.sonicclient.services.AdbServiceWrapper;
import com.autotest.sonicclient.services.InjectorService;
import com.autotest.sonicclient.threads.MExecutor;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.PermissionHelper;
import com.autotest.sonicclient.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    private static final int REQUEST_PERMISSIONS_CODE = 1000;
    private static final String[] PERMISSIONS = {
//            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //
        String sonicToken = GConfig.SONIC_TOKEN;
        LogUtil.d(TAG, "onCreate: token : " + sonicToken);
        if (sonicToken == null || TextUtils.isEmpty(sonicToken)) {
            redirectLogin();
        }

        //
        checkAndRequestPermissions();

        //
        new Thread(new Runnable() {
            @Override
            public void run() {
                PermissionHelper.checkAdbStatus(BaseActivity.this);
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Environment.isExternalStorageManager()) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }
    }

    private void checkAndRequestPermissions() {
        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS_CODE);
        }
    }

    private boolean hasAllPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    ToastUtil.showToast("请授予工具所需权限");
                    finish();
                }
            }
        }
    }

    protected void redirectLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}