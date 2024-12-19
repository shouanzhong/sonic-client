package com.autotest.sonicclient.utils;

import com.autotest.sonicclient.config.GConfig;

public class Constant {
    static public String URL_SERVER_LOGIN = GConfig.URL_SERVER_BASE + "/api/controller/users/login";
    static public String URL_SERVER_PROJECT_LIST = GConfig.URL_SERVER_BASE + "/api/controller/projects/list";
    static public String URL_SERVER_TESTSUITE_LIST = GConfig.URL_SERVER_BASE + "/api/controller/testSuites/list?projectId=%s&name=&page=1&pageSize=25";
    static public String URL_SERVER_TESTCASE_LIST = GConfig.URL_SERVER_BASE + "/api/controller/testSuites/getSuit?id=%s";


    public static final String PREFS_NAME = "SonicPrefs";
    public static final String KEY_USERNAME = "userName";
    public static final String KEY_PASSWORD = "password";
    public static final String KEY_SONIC_TOKEN = "SonicToken";
    public static final String KEY_REMEMBER_ME = "remember_me";
    public static final String KEY_PROJECT_ID = "KEY_PROJECT_ID";
}
