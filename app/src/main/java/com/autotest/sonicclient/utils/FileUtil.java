package com.autotest.sonicclient.utils;

import java.io.File;

public class FileUtil extends cn.hutool.core.io.FileUtil {
    public static boolean createDirs(String s) {
        File file = new File(s);
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return true;
        }
    }
}
