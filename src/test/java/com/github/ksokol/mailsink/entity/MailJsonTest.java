package com.github.ksokol.mailsink.entity;

import com.github.ksokol.mailsink.TestMails;
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
    public void setUp() throws Exception {
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
        mockHttpServletRequest.setPathInfo("info");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(mockHttpServletRequest));
    }

    @Test
    public void shouldSerializeFields() throws Exception {
        JsonContentAssert jsonContentAssert = json.write(mail).assertThat();

        jsonContentAssert.extractingJsonPathValue("messageId").isEqualTo("expected messageId");
        jsonContentAssert.extractingJsonPathValue("recipient").isEqualTo("expected recipient");
        jsonContentAssert.extractingJsonPathValue("sender").isEqualTo("expected sender");
        jsonContentAssert.extractingJsonPathValue("subject").isEqualTo("expected subject");
        jsonContentAssert.extractingJsonPathValue("text").isEqualTo("expected text");
        jsonContentAssert.extractingJsonPathNumberValue("createdAt").isEqualTo(1000);
    }

    @Test
    public void shouldNotSerializeFields() throws Exception {
        JsonContentAssert jsonContentAssert = json.write(mail).assertThat();

        jsonContentAssert.doesNotHaveJsonPathValue("id");
        jsonContentAssert.doesNotHaveJsonPathValue("source");
    }

    @Test
    public void shouldSerializeAttachmentsWithFalseValueWhenNoAttachmentsAvailable() throws Exception {
        JsonContentAssert jsonContentAssert = json.write(mail).assertThat();

        jsonContentAssert.extractingJsonPathValue("attachments").isEqualTo(false);
    }

    @Test
    public void shouldSerializeAttachmentsWithTrueValueWhenAttachmentsAvailable() throws Exception {
        mail.setAttachments(Collections.singletonList(new MailAttachment()));
        JsonContentAssert jsonContentAssert = json.write(mail).assertThat();

        jsonContentAssert.extractingJsonPathValue("attachments").isEqualTo(true);
    }

    @Test
    public void shouldSerializeHtmlBodyWithFullQualifiedUrls() throws Exception {
        mail = new Mail();
        mail.setSource(TestMails.mixed1());
        mail.setId(4L);

        JsonContentAssert jsonContentAssert = json.write(mail).assertThat();

        jsonContentAssert.extractingJsonPathValue("html")
                .isEqualTo("<img src=\"http://localhost/mailsink/mails/4/html/1234\">");
    }
}
