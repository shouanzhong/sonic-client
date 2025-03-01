package com.autotest.sonicclient.services;

import android.util.Log;

import com.autotest.sonicclient.interfaces.Assemble;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class InjectorService {
    private static final String TAG = "InjectorService";
    private static final HashMap<Class<?>, Object> INJECT_SERVICE = new HashMap<Class<?>, Object>();

    public static void register(Object object) {
        INJECT_SERVICE.put(object.getClass(), object);
    }

    public static void register(Object object, boolean update) throws IllegalAccessException {
        INJECT_SERVICE.put(object.getClass(), object);
        if (update) {
            ArrayList<?> services = getServices(Object.class);
            for (Object service : services) {
                Log.d(TAG, "register: update class : " + service.getClass());
                for (Field field : service.getClass().getDeclaredFields()) {
                    if (field.isAnnotationPresent(Assemble.class)) {
                        field.setAccessible(true);
                        Class<?> type = field.getType();
                        Log.d(TAG, String.format("register: update type = %s, fileClass = %s", type, object.getClass()));
                        if (!type.isAssignableFrom(object.getClass())) {
                            continue;
                        }

                        field.set(service, object);
                        Log.d(TAG, String.format("update: class: %s, field: %s", service.getClass().getName(), field.getName()));
                    }
                }
            }
        }
    }

    public static <T> T getService(Class<T> key) {
        return (T) INJECT_SERVICE.getOrDefault(key, null);
    }

    public static <T> ArrayList<T> getServices(Class<T> key) {
        ArrayList<T> ts = new ArrayList<>();
        for (Map.Entry<Class<?>, Object> entry : INJECT_SERVICE.entrySet()) {
            if (key.isAssignableFrom(entry.getKey())) {
                ts.add((T) entry.getValue());
            }
        }
        return ts;
    }

    public static void remove(Class<?> key) {
        INJECT_SERVICE.remove(key);
    }
}
