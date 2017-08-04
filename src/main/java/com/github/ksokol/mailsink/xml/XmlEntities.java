package com.github.ksokol.mailsink.xml;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Kamill Sokol
 */
class XmlEntities {

    private final List<XmlEntity> entities;

    XmlEntities(NodeList nodeList) {
        entities = new ArrayList<>(nodeList.getLength());

        for(int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            switch (node.getNodeType()) {
                case Node.ELEMENT_NODE:
                    entities.add(new ElementXmlEntity(node));
                    break;
                case Node.TEXT_NODE:
                    entities.add(new TextXmlEntity(node));
                    break;
                case Node.ATTRIBUTE_NODE:
                    entities.add(new AttributeXmlEntity(node));
                    break;
                default:
                    // ignore other node types
                    break;
            }
        }
    }

    List<Map<String, Object>> toListMap() {
        return entities.stream()
                .map(XmlEntity::toMap)
                .filter(xmlNode -> !xmlNode.isEmpty())
                .collect(Collectors.toList());
    }
}
