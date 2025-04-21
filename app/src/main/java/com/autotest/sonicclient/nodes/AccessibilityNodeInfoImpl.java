package com.autotest.sonicclient.nodes;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.autotest.sonicclient.services.TServiceWrapper;
import com.autotest.sonicclient.utils.XMLUtil;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class AccessibilityNodeInfoImpl extends AccessibilityNodeInfo {

    private AccessibilityNodeInfo nodeInfo;
    TServiceWrapper tService;

    public AccessibilityNodeInfoImpl(AccessibilityNodeInfo nodeInfo) {
        super(nodeInfo);
    }

    public AccessibilityNodeInfoImpl(AccessibilityNodeInfo nodeInfo, TServiceWrapper tService) {
        super(nodeInfo);
        this.tService = tService;
    }

    public AccessibilityNodeInfoImpl(@NotNull Node node, TServiceWrapper tService) {
        this.tService = tService;
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

    public void inputText(String text) {
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);

        this.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    public Point getVisibleCenter() {
        Rect rect = new Rect();
        this.getBoundsInScreen(rect);
        return XMLUtil.parseBoundsCenter(rect);
    }

    public void click() throws Exception {
        tService.clickPos(getVisibleCenter());
    }

    @Override
    public AccessibilityNodeInfo getParent() {
        try {
            return super.getParent();
        } catch (Exception e) {
            return null;
        }
    }

    // 获取节点的索引
    public int getIndex() {
        AccessibilityNodeInfo parent = this.getParent();
        if (parent == null) {
            return 0;
        }
        for (int i = 0; i < parent.getChildCount(); i++) {
            if (this.equals(parent.getChild(i))) {
                return i;
            }
        }
        return 0;
    }

    public JSONObject toJson() {
        return toJson(true);
    }

    public JSONObject toJson(boolean doRecursion) {

        JSONObject jsonNode = new JSONObject();

            // 添加节点属性
            jsonNode.put("index", this.getParent() != null ? this.getIndex() : 0);
            jsonNode.put("text", this.getText() != null ? this.getText().toString() : "");
            jsonNode.put("resource-id", this.getViewIdResourceName() != null ? this.getViewIdResourceName() : "");
            jsonNode.put("class", this.getClassName() != null ? this.getClassName().toString() : "");
            jsonNode.put("package", this.getPackageName() != null ? this.getPackageName().toString() : "");
            jsonNode.put("content-desc", this.getContentDescription() != null ? this.getContentDescription().toString() : "");
            jsonNode.put("checkable", this.isCheckable());
            jsonNode.put("checked", this.isChecked());
            jsonNode.put("clickable", this.isClickable());
            jsonNode.put("enabled", this.isEnabled());
            jsonNode.put("focusable", this.isFocusable());
            jsonNode.put("focused", this.isFocused());
            jsonNode.put("scrollable", this.isScrollable());
            jsonNode.put("long-clickable", this.isLongClickable());
            jsonNode.put("password", this.isPassword());
            jsonNode.put("selected", this.isSelected());
            Rect rect = new Rect();
            this.getBoundsInScreen(rect);
            jsonNode.put("bounds", rect);

            if (doRecursion) {
                // 处理子节点
                JSONArray childrenArray = new JSONArray();
                for (int i = 0; i < this.getChildCount(); i++) {
                    AccessibilityNodeInfoImpl childNode = (AccessibilityNodeInfoImpl) this.getChild(i);
                    if (childNode != null) {
                        childrenArray.add(toJson());
                    }
                }
                jsonNode.put("children", childrenArray);
            }
        return jsonNode;
    }
}
