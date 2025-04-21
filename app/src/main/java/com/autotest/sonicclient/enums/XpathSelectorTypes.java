package com.autotest.sonicclient.enums;


public enum XpathSelectorTypes {
    TEXT(SelectorTypes.TEXT, "text"),
    RESOURCE_ID(SelectorTypes.RESOURCE_ID, "resource-id"),
    CLASS(SelectorTypes.CLASS, "class"),
    PACKAGE(SelectorTypes.PACKAGE, "package"),
    CONTENT_DESC(SelectorTypes.CONTENT_DESC,"content-desc"),
    XPATH(SelectorTypes.XPATH,"");

    private final SelectorTypes selectorTypes;
    private final String value;

    XpathSelectorTypes(SelectorTypes selectorTypes, String value) {
        this.selectorTypes = selectorTypes;
        this.value = value;
    }

    public static String getValue(SelectorTypes selectorType) {
        for (XpathSelectorTypes type : XpathSelectorTypes.values()) {
            if (type.selectorTypes == selectorType) {
                return type.value;
            }
        }
        throw new IllegalArgumentException("No matching XpathSelectorTypes for: " + selectorType);
    }
}
