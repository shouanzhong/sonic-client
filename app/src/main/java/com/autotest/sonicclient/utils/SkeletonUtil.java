package com.autotest.sonicclient.utils;

import android.content.Context;

import com.autotest.sonicclient.interfaces.Assemble;
import com.autotest.sonicclient.interfaces.CollectionObject;
import com.autotest.sonicclient.interfaces.CustomService;
import com.autotest.sonicclient.interfaces.HandlerService;
import com.autotest.sonicclient.services.InjectorService;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;

public class SkeletonUtil {
    private static final String TAG = "SkeletonUtil";

    private static HashSet<Class<?>> classes = new HashSet<>();
    private static HashSet<Class<?>> services = new HashSet<>();

    public static void init(Context context) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException {
        scanClasses(context);
        inject(context);
        assemble();
        collect();
    }

    public static void inject(Context context) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        for (Class<?> aClass : classes) {
            if (aClass.isAnnotationPresent(HandlerService.class)) {
                services.add(aClass);
                Constructor<?> constructor = null;
                Object instance = null;
                constructor = aClass.getDeclaredConstructor(Context.class);
                instance = constructor.newInstance(context.getApplicationContext());
                InjectorService.register(instance);
            } else if (aClass.isAnnotationPresent(CustomService.class)) {
                services.add(aClass);
                Constructor<?> mConstructor = aClass.getDeclaredConstructor();
                Object mInstance = mConstructor.newInstance();
                InjectorService.register(mInstance);
            }
        }
    }

    public static void assemble() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        for (Class<?> aClass : services) {

            if (CommonUtil.isBaseService(aClass)) {
                for (Field field : aClass.getDeclaredFields()) {
                    if (field.isAnnotationPresent(Assemble.class)) {
                        field.setAccessible(true);
                        Class<?> type = field.getType();
                        Object service = InjectorService.getService(type);
                        if (service == null) {
                            LogUtil.w(TAG, String.format("assemble: not register %s", type));
                            continue;
                        }
                        field.set(InjectorService.getService(aClass), service);
                        LogUtil.d(TAG, String.format("assemble: class: %s, field: %s", aClass.getName(), field.getName()));
                    }
                }
            }
        }
    }

    public static void collect() {
        for (Class<?> aClass : services) {
            for (Method method : aClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(CollectionObject.class)) {
                    method.setAccessible(true);
                    CollectionObject annotation = method.getAnnotation(CollectionObject.class);
                    Class<?> clazz = annotation.value();
                    ArrayList<?> services = InjectorService.getServices(clazz);
                    if (services.isEmpty()) {
                        LogUtil.e(TAG, String.format("collect: find service fail: %s", clazz));
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

    public static void scanClasses(Context context) {
        LogUtil.d(TAG, "scanClasses: ---------------------");

        try {
            ClassLoader classLoader = context.getClassLoader();

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
                        if (className.contains(context.getApplicationInfo().packageName)) {
                            try {
                                Class<?> clazz = Class.forName(className);
                                if (classes.add(clazz)) {
                                    LogUtil.d(TAG, String.format("scanClasses: Scan class for %s", className));
                                }
                            } catch (ClassNotFoundException e) {
                                LogUtil.e(TAG, String.format("scanClasses: Can't get class instance of %s", className), e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, "Error during class scanning", e);
        }
    }

}
