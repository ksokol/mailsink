package com.github.ksokol.mailsink.xml

import groovy.xml.DOMBuilder
import org.junit.Test
import org.w3c.dom.NodeList

import javax.xml.xpath.XPathFactory

import static javax.xml.xpath.XPathConstants.NODESET

/**
 * @author Kamill Sokol
 */
class XmlEntitiesTest {

    def html = '''<table>
                    <![CDATA[content]]>
                    <tbody>
                        <tr>
                            <td>text</td>
                            <td>
                                <a href="#">link</a>
                            </td>
                            <td class="value"></td>
                            <td></td>
                        </tr>
                    </tbody>
                </table>'''

    @Test
    void shouldConvertNodeListToListMap() {
        def document = DOMBuilder.newInstance().parseText(html)
        def nodeList = document.getElementsByTagName("table")

        def actual = new XmlEntities(nodeList).toListMap()

        assert actual == [[table:
                           [children: [
                               [tbody: [
                                   children: [
                                       [tr: [
                                           children: [
                                               [td: [
                                                       children: [[text: 'text']]
                                               ]],
                                               [td: [
                                                   children: [
                                                       [a:
                                                            [href    : '#',
                                                             children: [[text: 'link']]
                                                            ]
                                                       ]]
                                               ]],
                                               [td: [
                                                   class   : 'value',
                                                   children: []
                                               ]]
                                           ]
                                       ]]
                                   ]
                               ]]
                           ]]
                      ]]
    }

    @Test
    void shouldConvertNodeListToSingleAttributeValue() {
        def nodeList = extractFromHtml('<td class="value"></td>', '//@class')
        def actual = new XmlEntities(nodeList).toListMap()

        assert actual == [[class: 'value']]
    }

    @Test
    void shouldConvertNodeListToSingleTextValue() {
        def nodeList = extractFromHtml('<td>text value</td>', '//text()')
        def actual = new XmlEntities(nodeList).toListMap()

        assert actual == [[text: 'text value']]
    }

    static def extractFromHtml(String html, String xpath) {
        def document = DOMBuilder.newInstance().parseText(html)
        def xpathExpression = XPathFactory.newInstance().newXPath().compile(xpath)
        return xpathExpression.evaluate(document, NODESET) as NodeList
    }
}
