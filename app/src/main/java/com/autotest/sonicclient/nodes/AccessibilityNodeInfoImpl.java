package com.autotest.sonicclient.nodes;

import android.view.accessibility.AccessibilityNodeInfo;

import com.autotest.sonicclient.utils.XMLUtil;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AccessibilityNodeInfoImpl extends AccessibilityNodeInfo {

    public AccessibilityNodeInfoImpl(@NotNull Node node) {
        NamedNodeMap attributes = node.getAttributes();

        setPackageName(attributes.getNamedItem("package").getNodeValue());
        setText(attributes.getNamedItem("text").getNodeValue());
        setViewIdResourceName(attributes.getNamedItem("resource-id").getNodeValue());
        setClassName(attributes.getNamedItem("class").getNodeValue());
        setContentDescription(attributes.getNamedItem("content-desc").getNodeValue());
        setCheckable(Boolean.parseBoolean(attributes.getNamedItem("checkable").getNodeValue()));
        setChecked(Boolean.parseBoolean(attributes.getNamedItem("checked").getNodeValue()));
        setClickable(Boolean.parseBoolean(attributes.getNamedItem("clickable").getNodeValue()));
        setEnabled(Boolean.parseBoolean(attributes.getNamedItem("enabled").getNodeValue()));
        setFocusable(Boolean.parseBoolean(attributes.getNamedItem("focusable").getNodeValue()));
        setFocused(Boolean.parseBoolean(attributes.getNamedItem("focused").getNodeValue()));
        setScrollable(Boolean.parseBoolean(attributes.getNamedItem("scrollable").getNodeValue()));
        setLongClickable(Boolean.parseBoolean(attributes.getNamedItem("long-clickable").getNodeValue()));
        setPassword(Boolean.parseBoolean(attributes.getNamedItem("password").getNodeValue()));
        setSelected(Boolean.parseBoolean(attributes.getNamedItem("selected").getNodeValue()));
        setBoundsInScreen(XMLUtil.parseBounds(attributes.getNamedItem("bounds").getNodeValue()));
    }
}
