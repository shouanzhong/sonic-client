package com.autotest.sonicclient.utils;

import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.Locale;

public class XMLUtil {


    public static String nodeToXml(AccessibilityNodeInfo node) {
        if (node == null) {
            return "<node/>";
        }

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<node");

        // 添加节点的基本属性
        xmlBuilder.append(String.format(Locale.US, " index=\"%d\"", node.getParent() == null ? 0 : getIndex(node)));
        xmlBuilder.append(String.format(" text=\"%s\"", safeString(node.getText())));
        xmlBuilder.append(String.format(" resource-id=\"%s\"", safeString(node.getViewIdResourceName())));
        xmlBuilder.append(String.format(" class=\"%s\"", safeString(node.getClassName())));
        xmlBuilder.append(String.format(" package=\"%s\"", safeString(node.getPackageName())));
        xmlBuilder.append(String.format(" content-desc=\"%s\"", safeString(node.getContentDescription())));
        xmlBuilder.append(String.format(" checkable=\"%s\"", node.isCheckable()));
        xmlBuilder.append(String.format(" checked=\"%s\"", node.isChecked()));
        xmlBuilder.append(String.format(" clickable=\"%s\"", node.isClickable()));
        xmlBuilder.append(String.format(" enabled=\"%s\"", node.isEnabled()));
        xmlBuilder.append(String.format(" focusable=\"%s\"", node.isFocusable()));
        xmlBuilder.append(String.format(" focused=\"%s\"", node.isFocused()));
        xmlBuilder.append(String.format(" scrollable=\"%s\"", node.isScrollable()));
        xmlBuilder.append(String.format(" long-clickable=\"%s\"", node.isLongClickable()));
        xmlBuilder.append(String.format(" password=\"%s\"", node.isPassword()));
        xmlBuilder.append(String.format(" selected=\"%s\"", node.isSelected()));

        // 添加节点的边界属性
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        xmlBuilder.append(String.format(" bounds=\"[%d,%d][%d,%d]\"", bounds.left, bounds.top, bounds.right, bounds.bottom));

        // 检查是否有子节点
        if (node.getChildCount() == 0) {
            xmlBuilder.append("/>"); // 无子节点时闭合标签
        } else {
            xmlBuilder.append(">"); // 开始子节点部分

            // 遍历子节点
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    xmlBuilder.append(nodeToXml(child)); // 递归处理子节点
                    child.recycle(); // 回收子节点
                }
            }

            xmlBuilder.append("</node>"); // 闭合当前节点标签
        }

        return xmlBuilder.toString();
    }

    // 获取节点的索引
    private static int getIndex(AccessibilityNodeInfo node) {
        AccessibilityNodeInfo parent = node.getParent();
        if (parent == null) {
            return 0;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (node.equals(parent.getChild(i))) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 安全处理字符串，防止 NullPointerException
     *
     * @param charSequence 待处理的字符串
     * @return 非空字符串
     */
    private static String safeString(CharSequence charSequence) {
        return charSequence == null ? "" : charSequence.toString().replace("\"", "&quot;");
    }
}
