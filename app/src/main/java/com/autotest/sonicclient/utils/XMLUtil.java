package com.autotest.sonicclient.utils;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.WindowInsetsAnimation;
import android.view.accessibility.AccessibilityNodeInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class XMLUtil {
    private static final String TAG = "XMLUtil";


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
            xmlBuilder.append("/>");
        } else {
            xmlBuilder.append(">");

            // 遍历
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    xmlBuilder.append(nodeToXml(child));
                    child.recycle();
                }
            }
            // 闭合
            xmlBuilder.append("</node>");
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

    public static boolean hasNodeByXpath(String xString, String xml) {
        try {
            // 创建 Document 对象
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

            // 创建 XPath 工厂和 XPath 对象
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            // 编译 XPath 表达式
            XPathExpression expr = xpath.compile(xString);

            // 执行 XPath 查询
            NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            return nodes.getLength() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Node findNodeByXpath(String xpath, String xmlContent) {
        NodeList nodes = findNodesByXpath(xpath, xmlContent);
        // 取第一个
        for (int i = 0; i < nodes.getLength(); i++) {
            return nodes.item(i);
        }
        return null;
    }

    public static NodeList findNodesByXpath(String xString, String xml) {
        try {
            // 创建 Document 对象
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

            // 创建 XPath 工厂和 XPath 对象
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            // 编译 XPath 表达式
            XPathExpression expr = xpath.compile(xString);

            // 执行 XPath 查询
            NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Point parseBoundsCenter(String s) {
        Rect rect = parseBounds(s);
        int x = (rect.left + rect.right) / 2;
        int y = (rect.top + rect.bottom) / 2;
        System.out.printf("Rect: %s, Center point: x: %s, y: %s%n", rect, x, y);
        return new Point(x, y);
    }

    public static Rect parseBounds(String BoundsString) {
        Pattern compile = Pattern.compile("(\\d+)");
        Matcher matcher = compile.matcher(BoundsString);
        ArrayList<Integer> arrayList = new ArrayList<>();
        while (matcher.find()) {
            String val = matcher.group();
            arrayList.add(Integer.parseInt(val));
        }
        if (arrayList.size() != 4) {
            Log.e(TAG, "parseBounds: List: " + arrayList, new Exception("4 Param is required"));
            return null;
        }
        return new Rect(arrayList.get(0), arrayList.get(1), arrayList.get(2), arrayList.get(3));
    }
}
