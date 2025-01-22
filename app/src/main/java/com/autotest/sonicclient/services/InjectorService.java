package com.autotest.sonicclient.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InjectorService {
    private static final HashMap<Class<?>, Object> INJECT_SERVICE = new HashMap<Class<?>, Object>();

    public static void register(Object object) {
        INJECT_SERVICE.put(object.getClass(), object);
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
