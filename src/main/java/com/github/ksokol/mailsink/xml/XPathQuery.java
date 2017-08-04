package com.github.ksokol.mailsink.xml;

import io.vavr.control.Try;
import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DomSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.util.List;
import java.util.Map;

import static javax.xml.xpath.XPathConstants.NODESET;
import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * @author Kamill Sokol
 */
public class XPathQuery {

    private final String xpath;

    public XPathQuery(String xpath) {
        this.xpath = xpath;
    }

    public List<Map<String, Object>> query(String value) {
        return doQuery(toDocument(value));
    }

    private static Document toDocument(String value) {
        HtmlCleaner htmlCleaner = new HtmlCleaner();
        CleanerProperties cleanerProperties = htmlCleaner.getProperties();
        cleanerProperties.setOmitXmlDeclaration(true);
        DomSerializer domSerializer = new DomSerializer(htmlCleaner.getProperties());
        return Try.of(() -> domSerializer.createDOM(htmlCleaner.clean(defaultString(value)))).get();
    }

    private List<Map<String, Object>> doQuery(Document document) {
        return Try.of(() -> {
            XPathExpression xpathExpression = XPathFactory.newInstance().newXPath().compile(xpath);
            NodeList nodeList = (NodeList) xpathExpression.evaluate(document, NODESET);
            return new XmlEntities(nodeList).toListMap();
        }).getOrElseThrow(XpathQueryException::new);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private class XpathQueryException extends RuntimeException {}
}
