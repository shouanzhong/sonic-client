package com.autotest.sonicclient.utils;

import com.orhanobut.logger.Logger;


public class LogUtil {
    // 日志级别
    public static int LOG_LEVEL = 0;

    public static void d(String tag, String message, Object... args) {
        if (LOG_LEVEL > Logger.DEBUG) {
            return;
        }
        Logger.t(tag).d(message, args);
    }

    public static void i(String tag, String message, Object... args) {
        if (LOG_LEVEL > Logger.INFO) {
            return;
        }
        Logger.t(tag).i(message, args);
    }

    public static void w(String tag, String message, Object... args) {
        if (LOG_LEVEL > Logger.WARN) {
            return;
        }
        Logger.t(tag).w(message, args);
    }

    public static void e(String tag, String message, Object... args) {
        if (LOG_LEVEL > Logger.ERROR) {
            return;
        }
//        Logger.t(tag).e(message + "\n" + getCurrentStrackTraceString(), args);
        Logger.t(tag).e(message, args);
    }

    public static void e(String tag, Throwable throwable, String message, Object... args) {
        if (LOG_LEVEL > Logger.ERROR) {
            return;
        }
        Logger.t(tag).e(throwable, message, args);
    }

    public static void i(String tag, String message, Throwable t) {
        if (LOG_LEVEL > Logger.INFO) {
            return;
        }
        Logger.log(Logger.INFO, tag, message, t);
    }

    public static void w(String tag, String message, Throwable t) {
        if (LOG_LEVEL > Logger.WARN) {
            return;
        }
        Logger.log(Logger.WARN, tag, message, t);
    }

    public static void d(String tag, String message, Throwable t) {
        if (LOG_LEVEL > Logger.DEBUG) {
            return;
        }
        Logger.log(Logger.DEBUG, tag, message, t);
    }

    public static void e(String tag, String message, Throwable t) {
        e(tag, t, message);
    }

    public static void v(String tag, String message, Object... args) {
        if (LOG_LEVEL > Logger.VERBOSE) {
            return;
        }
        Logger.t(tag).v(message, args);
    }

    public static void t(String tag, String message, Object... args) {
        if (LOG_LEVEL > Logger.ASSERT) {
            return;
        }
        Logger.t(tag).wtf(message, args);
    }

    public static String stackTraceToString(StackTraceElement[] stackTraceElements) {
        if (stackTraceElements == null || stackTraceElements.length <= 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTraceElements) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String getCurrentStrackTraceString() {
        return stackTraceToString(Thread.currentThread().getStackTrace());
    }

}
