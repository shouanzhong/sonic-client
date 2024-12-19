package com.autotest.sonicclient.application;

import android.app.Application;
import android.util.Log;

import com.autotest.sonicclient.utils.Constant;
import com.autotest.sonicclient.config.GConfig;
import com.autotest.sonicclient.utils.PermissionHelper;
import com.autotest.sonicclient.utils.SharePreferenceUtil;
import com.autotest.sonicclient.utils.ToastUtil;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import dalvik.system.DexFile;

public class ApplicationImpl extends Application {
    private static final String TAG = "ApplicationImpl";
    private static ApplicationImpl _instance;

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        init();
        // about log
        Logger.addLogAdapter(new AndroidLogAdapter(new FormatStrategy() {
            @Override
            public void log(int priority, String tag, String message) {
                Log.println(priority, "SONICCLIENT-" + tag, message);
            }
        }) {
            @Override
            public boolean isLoggable(int priority, String tag) {
                return true;
            }
        });
        PermissionHelper.openAccessibilityActivityIfNotGranted(this);
        //
        GConfig.SONIC_TOKEN = SharePreferenceUtil.getInstance(this).getString(Constant.KEY_SONIC_TOKEN, "");
    }

    public static ApplicationImpl getInstance() {
        return _instance;
    }

    public void showToast(String msg) {
        ToastUtil.showToast(this, msg);
    }

    public void init() {
        Log.i(TAG, "init: ");
        // 加载内部代码
        try {
            DexFile dexFile = new DexFile(new File(getPackageCodePath()));
            Enumeration<String> classNames = dexFile.entries();

            while (classNames.hasMoreElements()) {
                String className = classNames.nextElement();
                if (className.contains(getApplicationInfo().packageName)) {
                    try {
                        Log.d(TAG, String.format("init: Scan class for %s", className));
                        Class childClazz = Class.forName(className);
//                        classes.add(childClazz);
                        // 不要影响类扫描
                    } catch (ClassNotFoundException e) {
                        Log.e(TAG, String.format("init: Can't get class instance of %s", className), e);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Class<?>> scanClasses() {
        Log.d(TAG, "scanClasses: ---------------------");
        List<Class<?>> classes = new ArrayList<>();
        HashSet<Class<?>> classSet = new HashSet<>();

        try {
            // 获取 ClassLoader 实例
            ClassLoader classLoader = this.getClassLoader();

            // 获取 BaseDexClassLoader 的 pathList 字段
            Field pathListField = Class.forName("dalvik.system.BaseDexClassLoader").getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(classLoader);

            // 获取 DexPathList 的 dexElements 字段
            Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object[] dexElements = (Object[]) dexElementsField.get(pathList);

            // 遍历每个 dexElement
            for (Object element : dexElements) {
                // 获取 DexFile 实例
                Field dexFileField = element.getClass().getDeclaredField("dexFile");
                dexFileField.setAccessible(true);
                Object dexFile = dexFileField.get(element);

                // 调用 DexFile 的 entries() 方法
                if (dexFile != null) {
                    Method entriesMethod = dexFile.getClass().getDeclaredMethod("entries");
                    entriesMethod.setAccessible(true);

                    Enumeration<String> classNames = (Enumeration<String>) entriesMethod.invoke(dexFile);
                    while (classNames.hasMoreElements()) {
                        String className = classNames.nextElement();

                        // 过滤当前应用包名的类
                        if (className.contains(this.getApplicationInfo().packageName)) {
                            try {
                                Class<?> clazz = Class.forName(className);
                                classes.add(clazz);
                                if (classSet.add(clazz)) {
                                    Log.d(TAG, String.format("scanClasses: Scan class for %s", className));

                                }
                            } catch (ClassNotFoundException e) {
                                Log.e(TAG, String.format("scanClasses: Can't get class instance of %s", className), e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during class scanning", e);
        }

        return classes;
    }
}
