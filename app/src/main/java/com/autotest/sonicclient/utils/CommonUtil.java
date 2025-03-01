package com.autotest.sonicclient.utils;

import android.annotation.SuppressLint;

import com.autotest.sonicclient.interfaces.BaseService;

import java.lang.annotation.Annotation;

public class CommonUtil {

    public static boolean isBaseService(Class<?> clazz) {
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(BaseService.class)) {
                return true;
            }
        }
        return false;
    }
}
