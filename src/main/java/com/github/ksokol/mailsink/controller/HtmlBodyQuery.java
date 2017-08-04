package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.xml.XPathQuery;

import java.util.List;
import java.util.Map;

/**
 * @author Kamill Sokol
 */
class HtmlBodyQuery {

    private String xpath;

    void setXpath(String xpath) {
        this.xpath = xpath;
    }

    List<Map<String, Object>> query(Mail mail) {
        return new XPathQuery(xpath).query(mail.getHtml());
    }
}
