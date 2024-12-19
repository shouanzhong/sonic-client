package com.autotest.sonicclient.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharePreferenceUtil {

    private static SharedPreferences sharedPreferences;
    private static SharePreferenceUtil _instance;

    public static SharePreferenceUtil getInstance(Context context) {
        if (_instance == null) {
            synchronized (SharePreferenceUtil.class) {
                if (_instance == null) {
                    _instance = new SharePreferenceUtil(context);
                }
            }
        }
        return _instance;
    }

    private SharePreferenceUtil(Context context) {
        sharedPreferences = context.getSharedPreferences(Constant.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void putMessage(OnPutMessage onPutMessage) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        onPutMessage.fill(editor);
        editor.apply();
    }

    public void clear(String ...keys) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        for (String key : keys) {
            editor.remove(key);
        }
        editor.apply();
    }

    public String getString(String key, String defVal) {
        return sharedPreferences.getString(key, defVal);
    }

    public Boolean getBoolean(String key, Boolean defVal) {
        return sharedPreferences.getBoolean(key, defVal);
    }

    public interface OnPutMessage {
        void fill(SharedPreferences.Editor editor);
    }
}
