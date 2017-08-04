package com.github.ksokol.mailsink.xml;

import org.w3c.dom.Node;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * @author Kamill Sokol
 */
class TextXmlEntity implements XmlEntity {

    private final String value;

    TextXmlEntity(Node node) {
        value = node.getNodeValue();
    }

    @Override
    public Map<String, Object> toMap() {
        return isBlank(value) ? emptyMap() : singletonMap("text", value);
    }
}
