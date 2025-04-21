package com.autotest.sonicclient.application;

import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.autotest.sonicclient.adapters.FileLogAdapter;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.CollectionObject;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.interfaces.CustomService;
import com.autotest.sonicclient.services.InjectorService;
import com.autotest.sonicclient.utils.CommonUtil;
import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.config.GConfig;
import com.autotest.sonicclient.utils.LogUtil;
import com.autotest.sonicclient.utils.SharePreferenceUtil;
import com.autotest.sonicclient.utils.SkeletonUtil;
import com.autotest.sonicclient.utils.ToastUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

public class ApplicationImpl extends Application {
    private static final String TAG = "ApplicationImpl";
    private static ApplicationImpl _instance;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        //
        GConfig.SONIC_TOKEN = SharePreferenceUtil.getInstance(this).getString(Constant.KEY_SONIC_TOKEN, "");
        GConfig.DATA_BASE_DIR = String.format("%s/sonic/", Environment.getExternalStorageDirectory());
        // about log
        initLog();
        //
        initConfig();

        // 允许访问隐藏 API
        try {
            Class<?> vmDebugClass = Class.forName("dalvik.system.VMDebug");
            java.lang.reflect.Method allowHiddenApiReflectionFrom = vmDebugClass.getDeclaredMethod("allowHiddenApiReflectionFrom", Class.class);
            allowHiddenApiReflectionFrom.setAccessible(true);
            allowHiddenApiReflectionFrom.invoke(null, ApplicationImpl.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLog() {
        Logger.addLogAdapter(
                new AndroidLogAdapter(new FormatStrategy() {
                    @Override
                    public void log(int priority, String tag, String message) {
                        Log.println(priority, "SONICCLIENT-" + tag, message);
                    }
                }) {
                    @Override
                    public boolean isLoggable(int priority, String tag) {
                        return true;
                    }
                }
        );
        Logger.addLogAdapter(new FileLogAdapter(this));
    }

    public static ApplicationImpl getInstance() {
        return _instance;
    }

    private void initConfig() {
        LogUtil.i(TAG, "init: ");
        try {
            SkeletonUtil.init(this);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
    }
}
