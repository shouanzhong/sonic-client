package com.autotest.sonicclient.handler;

import android.content.Context;

import com.autotest.sonicclient.utils.FileUtil;
import com.autotest.sonicclient.utils.ShellUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LogHandler {

    public static void clearLogcat() {
        ShellUtil.execCmd("logcat -c");
    }

    public static String dumpLogcat(Context context, String fName) throws IOException {
        String fileName = String.format("%s_%s.log", fName, System.currentTimeMillis());
        File file = new File(context.getExternalFilesDir(null), fileName);
        file.createNewFile();
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            ShellUtil.execCmd("logcat -d -time -v", new ShellUtil.OnStreamChangedListener() {
                @Override
                public void onStreamChanged(String line) {
                    try {
                        writer.write(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onErrorStreamChanged(String line) {
                    try {
                        writer.write(line);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, 10 * 1000);
        }
        return fileName;
    }
}
