package com.autotest.sonicclient.utils;

import com.autotest.sonicclient.services.DeviceService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GroovyHelper {
    private static final String TAG = "GroovyHelper";
    static Pattern cmdCompile = Pattern.compile("executeCommand\\(.*?,\"([^\"]+)\"\\)");

//    static Binding binding = new Binding();
    public static void executeCmd(String script, GroovyCmdLineListener listener) {
        List<String> list = extractCmd(script);
        for (String cmd : list) {
            listener.onParse(cmd);
        }
    }

    public static String transform(String script) {


        return script;
    }

    static List<String> extractCmd(String script) {
        ArrayList<String> list = new ArrayList<>();
        // 遍历字符串数组
        for (String line : script.split("\n")) {
            Matcher matcher = cmdCompile.matcher(line);
            if (matcher.find()) {
                String group = matcher.group(1);
                list.add(group);
            }
        }
        return list;
    }

    public static void bind(String name, Object value) {
//        binding.setVariable(name, value);
    }

    public interface GroovyCmdLineListener {
        void onParse(String cmd);
    }
}
