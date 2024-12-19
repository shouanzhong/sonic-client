package com.autotest.sonicclient.handler;

import com.alibaba.fastjson2.JSONObject;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

public class TextHandler {
    public static String replaceTrans(String text, JSONObject globalParams) {
        if (text.contains("{{random}}")) {
            String random = (int) (Math.random() * 10 + Math.random() * 10 * 2) + 5 + "";
            text = text.replace("{{random}}", random);
        }
        if (text.contains("{{timestamp}}")) {
            String timeMillis = Calendar.getInstance().getTimeInMillis() + "";
            text = text.replace("{{timestamp}}", timeMillis);
        }
        if (text.contains("{{") && text.contains("}}")) {
            String tail = text.substring(text.indexOf("{{") + 2);
            if (tail.contains("}}")) {
                String child = tail.substring(tail.indexOf("}}") + 2);
                String middle = tail.substring(0, tail.indexOf("}}"));
                text = text.substring(0, text.indexOf("}}") + 2);
                if (globalParams.getString(middle) != null) {
                    text = text.replace("{{" + middle + "}}", globalParams.getString(middle));
                } else {
                    if (middle.matches("random\\[\\d\\]")) {
                        int t = Integer.parseInt(middle.replace("random[", "").replace("]", ""));
                        int digit = (int) Math.pow(10, t - 1);
                        int rs = new Random().nextInt(digit * 10);
                        if (rs < digit) {
                            rs += digit;
                        }
                        text = text.replace("{{" + middle + "}}", rs + "");
                    }
                    if (middle.matches("random\\[\\d-\\d\\]")) {
                        String t = middle.replace("random[", "").replace("]", "");
                        int[] size = Arrays.stream(t.split("-")).mapToInt(Integer::parseInt).toArray();
                        text = text.replace("{{" + middle + "}}", (int) (Math.random() * (size[1] - size[0] + 1)) + size[0] + "");
                    }
                    if (middle.matches("random\\[.+\\|.+\\]")) {
                        String t = middle.replace("random[", "").replace("]", "");
                        String[] size = t.split("\\|");
                        text = text.replace("{{" + middle + "}}", size[new Random().nextInt(size.length)]);
                    }
                }
                text = text + replaceTrans(child, globalParams);
            }
        }
        return text;
    }
}
