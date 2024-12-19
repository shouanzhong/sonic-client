package com.autotest.sonicclient.utils;

import java.io.File;

public class FileUtil {
    public static boolean createDirs(String s) {
        File file = new File(s);
        if (!file.exists()) {
            return file.mkdirs();
        } else {
            return true;
        }
    }
}
