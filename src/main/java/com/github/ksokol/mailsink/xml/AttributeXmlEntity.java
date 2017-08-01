package com.github.ksokol.mailsink.xml;

import org.w3c.dom.Node;

import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * @author Kamill Sokol
 */
class AttributeXmlEntity implements XmlEntity {

    private final String nodeName;
    private final String nodeValue;

    AttributeXmlEntity(Node node) {
        nodeName = node.getNodeName();
        nodeValue = node.getNodeValue();
    }

    @Override
    public Map<String, Object> toMap() {
        return singletonMap(nodeName, nodeValue);
    }
}
