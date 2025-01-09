package com.autotest.sonicclient.nodes;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public interface NodesList extends NodeList {
    Node item(int var1);

    int getLength();
}