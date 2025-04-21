package com.autotest.sonicclient.enums;


public enum SelectorTypes {
    TEXT("getText"),
    RESOURCE_ID("getViewIdResourceName"),
    CLASS("getClassName"),
    PACKAGE("getPackageName"),
    CONTENT_DESC("getContentDescription"),
    CHECKABLE("isCheckable"),
    CHECKED("isChecked"),
    CLICKABLE("isClickable"),
    ENABLED("isEnabled"),
    FOCUSABLE("isFocusable"),
    FOCUSED("isFocused"),
    SCROLLABLE("isScrollable"),
    LONG_CLICKABLE("isLongClickable"),
    PASSWORD("isPassword"),
    SELECTED("isSelected"),
    XPATH("");

    private final String value;

    SelectorTypes(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
