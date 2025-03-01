package com.autotest.sonicclient.utils;

import android.os.Build;

import com.autotest.sonicclient.config.GConfig;

import java.net.URLEncoder;

public class Constant {
    public static final CharSequence APP_NAME = "Sonic Client";
    static public String URL_SERVER_LOGIN = GConfig.URL_SERVER_BASE + "/api/controller/viewCase/login";
    static public String URL_SERVER_PROJECT_LIST = GConfig.URL_SERVER_BASE + "/api/controller/projects/list";
//    static public String URL_SERVER_TESTSUITE_LIST = GConfig.URL_SERVER_BASE + "/api/controller/testSuites/list?projectId=%s&name=&page=1&pageSize=25";
    static public String URL_SERVER_TESTSUITE_LIST = GConfig.URL_SERVER_BASE + "/api/controller/testSuites/custom-list?model=" + Build.MODEL + "&board=" + Build.BOARD;
//    static public String URL_SERVER_TESTCASE_LIST = GConfig.URL_SERVER_BASE + "/api/controller/testSuites/getSuit?id=%s";
    static public String URL_SERVER_TESTCASE_LIST = GConfig.URL_SERVER_BASE + "/api/controller/testSuites/list-steps?suiteId=%s";
    static public final String URL_SERVER_TESTSUITE_RESULT_CREATE = GConfig.URL_SERVER_BASE + "/api/controller/viewCase/createJob";
    static public final String URL_SERVER_STEP_RESULT = GConfig.URL_SERVER_BASE + "/api/controller/viewCase/accept-step-log";
//
    public static final String ACTION_ACC_PERMISSION = "com.autotest.sonicclient.ACTION_ACC_PERMISSION";

    public static final String PREFS_NAME = "SonicPrefs";
    public static final String AES_KEY = "b2cyqmekle6fcu7m";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SONIC_TOKEN = "Auth-Code";
    public static final String KEY_ATHENA_TOKEN = "Auth-Code";
    public static final String KEY_REMEMBER_ME = "remember_me";
    public static final String KEY_PROJECT_ID = "KEY_PROJECT_ID";

    public static final String KEY_SUIT_INFO_SID = "sid";
    public static final String KEY_SUIT_INFO_CASES = "cases";

    public static final String KEY_CASE_INFO_CID = "testCasesId";
    public static final String KEY_CASE_INFO_RID = "rid";
    public static final String KEY_CASE_INFO_STEPS = "steps";
    public static final String KEY_CASE_INFO_LOG_URI = "logUri";
    public static final String KEY_CASE_INFO_DEVICE_ID = "deviceId";
    public static final String KEY_CASE_INFO_CASE_NAME = "caseName";
    public static final String KEY_CASE_INFO_GLOBAL_PARAMS = "gp";

    public static final String KEY_STEP_INFO_STEP = "step";
    public static final String KEY_STEP_INFO_TYPE = "stepType";
    public static final String KEY_STEP_INFO_CONDITION_TYPE = "conditionType";

    public static final String KEY_STEP_RESULT_MSG = "msg";
    public static final String KEY_STEP_RESULT_DES = "des";
    public static final String KEY_STEP_RESULT_STATUS = "status";
    public static final String KEY_STEP_RESULT_LOG_DETAIL = "log";
    public static final String KEY_STEP_RESULT_CID = "cid";
    public static final String KEY_STEP_RESULT_RID = "rid";
    public static final String KEY_STEP_RESULT_UDID = "udId";
    public static final String KEY_STEP_RESULT_PIC = "pic";
    public static final String KEY_STEP_RESULT_LOGCAT = "logcatPath";

}
