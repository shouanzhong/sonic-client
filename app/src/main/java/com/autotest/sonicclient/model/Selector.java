package com.autotest.sonicclient.model;

import com.autotest.sonicclient.enums.SelectorTypes;

public class Selector {
    SelectorTypes type;
    String value;

    public Selector(SelectorTypes type, String value) {
        this.type = type;
        this.value = value;
    }

    public SelectorTypes getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
