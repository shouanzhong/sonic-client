package com.autotest.sonicclient.model;

import com.autotest.sonicclient.enums.SelectorTypes;

public class By {
    public static Selector text(String text) {
        return new Selector(SelectorTypes.TEXT, text);
    }

    public static Selector desc(String desc) {
        return new Selector(SelectorTypes.CONTENT_DESC, desc);
    }

    public static Selector res(String res) {
        return new Selector(SelectorTypes.RESOURCE_ID, res);
    }

    public static Selector className(String res) {
        return new Selector(SelectorTypes.CLASS, res);
    }

    public static Selector xpath(String path) {
        return new Selector(SelectorTypes.XPATH, path);
    }
}
