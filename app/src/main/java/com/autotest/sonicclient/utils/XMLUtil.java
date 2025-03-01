package com.autotest.sonicclient.utils;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.WindowInsetsAnimation;
import android.view.accessibility.AccessibilityNodeInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class XMLUtil {
    private static final String TAG = "XMLUtil";


    public static String nodeToXml(AccessibilityNodeInfo node) {
        return nodeToXml(node, true);
    }

    public static String nodeToXml(AccessibilityNodeInfo node, boolean doRecursion) {
        if (node == null) {
            return "<node/>";
        }

        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<").append(safeString(node.getClassName()));

        // 节点属性
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

        // 边界属性
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        xmlBuilder.append(String.format(" bounds=\"[%d,%d][%d,%d]\"", bounds.left, bounds.top, bounds.right, bounds.bottom));

        // 子节点
        if (node.getChildCount() == 0) {
            xmlBuilder.append("/>");
        } else {
            xmlBuilder.append(">");

            if (doRecursion) {
                // 遍历
                for (int i = 0; i < node.getChildCount(); i++) {
                    AccessibilityNodeInfo child = node.getChild(i);
                    if (child != null) {
                        xmlBuilder.append(nodeToXml(child));
                        child.recycle();
                    }
                }
            }
            // 闭合
            xmlBuilder.append("</").append(safeString(node.getClassName())).append(">");
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
        return findNodesByXpath(xString, xml, false);
    }

    public static NodeList findNodesByXpath(String xString, String xml, boolean toAppium) {
        try {
            if (toAppium) {
                xml = tag2Class(xml);
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true); // 启用命名空间
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xpath = xPathFactory.newXPath();

            XPathExpression expr = xpath.compile(xString);

            NodeList nodes = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
            return nodes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String tag2Class(String xml) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new java.io.ByteArrayInputStream(xml.getBytes()));

        // 更新
        modifyNodeNames(document, document.getDocumentElement());

        // 输出
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
        return writer.toString();
    }

    private static void modifyNodeNames(Document document, Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            String classAttr = element.getAttribute("class");
            if (!classAttr.isEmpty()) {
                // 改成 class 属性值
                document.renameNode(element, element.getNamespaceURI(), classAttr);
            }
        }

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            modifyNodeNames(document, childNodes.item(i));
        }
    }

    public static Point parseBoundsCenter(String s) {
        Rect rect = parseBounds(s);
        return parseBoundsCenter(rect);
    }

    public static Point parseBoundsCenter(Rect rect) {
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
