package com.autotest.sonicclient.application;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.handler.ReportHandler;
import com.autotest.sonicclient.handler.SuitHandler;
import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.CollectionObject;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.interfaces.IStepHandler;
import com.autotest.sonicclient.model.SuitResult;
import com.autotest.sonicclient.services.InjectorService;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;

import dalvik.system.DexFile;

public class ApplicationImpl extends Application {
    private static final String TAG = "ApplicationImpl";
    private static ApplicationImpl _instance;
    private static HashSet<Class<?>> classes;
    private static HashSet<Class<?>> services = new HashSet<>();
    private static InjectorService injectorService;

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
        try {
            classes = scanClasses();
            inject();
            assemble();
            collect();
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

    public void inject() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        for (Class<?> aClass : classes) {

            if (aClass.isAnnotationPresent(HandlerService.class)) {
                services.add(aClass);
                Constructor<?> constructor = null;
                Object instance = null;
                constructor = aClass.getDeclaredConstructor(Context.class);
                instance = constructor.newInstance(getApplicationContext());
                InjectorService.register(instance);
            }
        }
    }

    public void assemble() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        for (Class<?> aClass : services) {

            if (aClass.isAnnotationPresent(HandlerService.class)) {
                for (Field field : aClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Assemble.class)) {
                        field.setAccessible(true);
                        Class<?> type = field.getType();
                        Object service = InjectorService.getService(type);
                        if (service == null) {
                            Log.e(TAG, String.format("assemble: 未注册 %s", type));
                            continue;
                        }
                        field.set(InjectorService.getService(aClass), service);
                        Log.d(TAG, String.format("assemble: class: %s, field: %s", aClass.getName(), field.getName()));
                    }
                }
            }
        }
    }

    public void collect() {
        for (Class<?> aClass : services) {
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(CollectionObject.class)) {
                    method.setAccessible(true);
                    CollectionObject annotation = method.getAnnotation(CollectionObject.class);
                    Class<?> clazz = annotation.value();
                    ArrayList<?> services = InjectorService.getServices(clazz);
                    if (services.isEmpty()) {
                        Log.e(TAG, String.format("collect: find service fail: %s", clazz));
                        continue;
                    }
                    try {
                        method.invoke(InjectorService.getService(aClass), services);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public HashSet<Class<?>> scanClasses() {
        Log.d(TAG, "scanClasses: ---------------------");
        HashSet<Class<?>> classSet = new HashSet<>();

        try {
            ClassLoader classLoader = this.getClassLoader();

            //
            Field pathListField = Class.forName("dalvik.system.BaseDexClassLoader").getDeclaredField("pathList");
            pathListField.setAccessible(true);
            Object pathList = pathListField.get(classLoader);

            Field dexElementsField = pathList.getClass().getDeclaredField("dexElements");
            dexElementsField.setAccessible(true);
            Object[] dexElements = (Object[]) dexElementsField.get(pathList);

            for (Object element : dexElements) {
                Field dexFileField = element.getClass().getDeclaredField("dexFile");
                dexFileField.setAccessible(true);
                Object dexFile = dexFileField.get(element);

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

        return classSet;
    }
}
