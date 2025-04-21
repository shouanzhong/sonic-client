package com.autotest.sonicclient.adapters;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.autotest.sonicclient.activities.LoginActivity;
import com.autotest.sonicclient.config.GConfig;
import com.orhanobut.logger.LogAdapter;
import com.orhanobut.logger.Logger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileLogAdapter implements LogAdapter {
    private static final String TAG = "FileLogAdapter";

    private final Context context;
    private final ExecutorService executor;
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat;

    public FileLogAdapter(Context context) {
        this.context = context.getApplicationContext();
        this.executor = Executors.newSingleThreadExecutor();
        this.timeFormat = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    @Override
    public boolean isLoggable(int priority, String tag) {
        return true;
    }

    @Override
    public void log(int priority, String tag, String message) {
        String formattedTag = "SONICCLIENT-" + tag; // 统一添加前缀
        String time = timeFormat.format(new Date());
        String date = dateFormat.format(new Date());
        String level = getLevelString(priority);
        String logMessage = String.format(Locale.getDefault(), "%s %s %s/%s: %s",
                date, time, level, formattedTag, message);

        executor.execute(() -> writeToFile(logMessage));
    }

    private String getLevelString(int priority) {
        switch (priority) {
            case Log.VERBOSE: return "V";
            case Log.DEBUG: return "D";
            case Log.INFO: return "I";
            case Log.WARN: return "W";
            case Log.ERROR: return "E";
            case Log.ASSERT: return "A";
            default: return "?";
        }
    }

    private void writeToFile(String logMessage) {
        File logDir = new File(GConfig.DATA_BASE_DIR + "logs");
        if (!logDir.exists() && !logDir.mkdirs()) {
            Log.e(TAG, "无法创建日志目录");
            return;
        }

        String fileName = dateFormat.format(new Date()) + ".log";
        File logFile = new File(logDir, fileName);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
            bw.write(logMessage);
            bw.newLine();
        } catch (IOException e) {
            Log.e(TAG, "写入日志失败", e);
        }
    }
}