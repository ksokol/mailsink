package com.github.ksokol.mailsink.entity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContentAssert;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@JsonTest
public class MailJsonTest {

    @Autowired
    private JacksonTester<Mail> json;

    private Mail mail;

    @Before
    public void setUp() {
        mail = new Mail();
        mail.setId(1L);
        mail.setSource("expected source");
        mail.setAttachments(Collections.emptyList());
        mail.setMessageId("expected messageId");
        mail.setCreatedAt(new Date(1000));
        mail.setRecipient("expected recipient");
        mail.setSender("expected sender");
        mail.setSubject("expected subject");
        mail.setText("expected text");

        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        mockHttpServletRequest.setContextPath("mailsink");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));
    }

    @Test
    public void shouldSerializeFields() throws Exception {
        JsonContentAssert jsonContentAssert = assertThat(json.write(mail));

        jsonContentAssert.extractingJsonPathValue("messageId").isEqualTo("expected messageId");
        jsonContentAssert.extractingJsonPathValue("recipient").isEqualTo("expected recipient");
        jsonContentAssert.extractingJsonPathValue("sender").isEqualTo("expected sender");
        jsonContentAssert.extractingJsonPathValue("subject").isEqualTo("expected subject");
        jsonContentAssert.extractingJsonPathValue("text").isEqualTo("expected text");
        jsonContentAssert.extractingJsonPathValue("createdAt").isEqualTo("1970-01-01T00:00:01.000+0000");
    }

    @Test
    public void shouldNotSerializeFields() throws Exception {
        JsonContentAssert jsonContentAssert = assertThat(json.write(mail));

        jsonContentAssert.doesNotHaveJsonPathValue("id");
        jsonContentAssert.doesNotHaveJsonPathValue("source");
    }

    @Test
    public void shouldSerializeAttachmentsWithFalseValueWhenNoAttachmentsAvailable() throws Exception {
        JsonContentAssert jsonContentAssert = assertThat(json.write(mail));

        jsonContentAssert.extractingJsonPathValue("attachments").isEqualTo(false);
    }

    @Test
    public void shouldSerializeAttachmentsWithTrueValueWhenAttachmentsAvailable() throws Exception {
        MailAttachment mailAttachment = new MailAttachment();
        mailAttachment.setId(0L);
        mailAttachment.setContentId("irrelevant");
        mail.setAttachments(Collections.singletonList(mailAttachment));
        JsonContentAssert jsonContentAssert = assertThat(json.write(mail));

        jsonContentAssert.extractingJsonPathValue("attachments").isEqualTo(true);
    }

    @Test
    public void shouldSerializeHtmlBodyWithFullQualifiedUrls() throws Exception {
        MailAttachment mailAttachment = new MailAttachment();
        mailAttachment.setId(888L);
        mailAttachment.setContentId("1234");

        mail = new Mail();
        mail.setHtml("<img src=\"cid:1234\">");
        mail.setId(4L);
        mail.setAttachments(Collections.singletonList(mailAttachment));

        JsonContentAssert jsonContentAssert = assertThat(json.write(mail));

        jsonContentAssert.extractingJsonPathValue("html")
                .isEqualTo("<img src=\"http://localhost/mailsink/mailAttachments/888/data\">");
    }
}
