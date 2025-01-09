package com.autotest.sonicclient.services;

import java.util.HashMap;

public class InjectorService {
    private static final HashMap<Class<?>, Object> injectService = new HashMap<Class<?>, Object>();

    public static void register(Object object) {
        injectService.put(object.getClass(), object);
    }

    public static <T> T getService(Class<T> key) {
        return (T) injectService.get(key);
    }

    public static void remove(Class<?> key) {
        injectService.remove(key);
    }
}
