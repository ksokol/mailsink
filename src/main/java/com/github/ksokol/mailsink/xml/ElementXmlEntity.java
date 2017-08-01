package com.github.ksokol.mailsink.xml;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

/**
 * @author Kamill Sokol
 */
class ElementXmlEntity implements XmlEntity {

    private final String nodeName;
    private final List<AttributeXmlEntity> attributes;
    private final XmlEntities xmlEntities;

    ElementXmlEntity(Node node) {
        nodeName = node.getNodeName();
        attributes = attributes(node.getAttributes());
        xmlEntities = new XmlEntities(node.getChildNodes());
    }

    @Override
    public Map<String, Object> toMap() {
        List<Map<String, Object>> children = xmlEntities.toListMap();

        if(children.isEmpty() && attributes.isEmpty()) {
            return emptyMap();
        }

        Map<String, Object> mapInner = new LinkedHashMap<>();

        attributes.stream()
                .map(XmlEntity::toMap)
                .forEach(mapInner::putAll);

        mapInner.put("children", children);

        return singletonMap(nodeName, mapInner);
    }

    private static List<AttributeXmlEntity> attributes(NamedNodeMap nodeMap) {
        List<AttributeXmlEntity> attributes = new ArrayList<>(nodeMap.getLength());

        for (int i = 0; i < nodeMap.getLength(); i++) {
            Node node = nodeMap.item(i);
            attributes.add(new AttributeXmlEntity(node));
        }

        return attributes;
    }
}
