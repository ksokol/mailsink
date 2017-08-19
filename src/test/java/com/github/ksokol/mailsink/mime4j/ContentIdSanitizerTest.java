package com.github.ksokol.mailsink.mime4j;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.entity.MailAttachment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Kamill Sokol
 */
public class ContentIdSanitizerTest {

    private ContentIdSanitizer sanitizer = new ContentIdSanitizer();

    private UriComponentsBuilder uriComponentsBuilder;

    @Before
    public void setUp() throws Exception {
        uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost:8080");
    }

    @Test
    public void shouldNotSanitizeHtmlWithoutInlineAttachments() throws Exception {
        Mail mail = new Mail();
        mail.setHtml("<img src=\"cid:1234\">");

        String actualHtmlBody = sanitizer.sanitize(mail, uriComponentsBuilder);

        assertThat(actualHtmlBody, is("<img src=\"cid:1234\">"));
    }

    @Test
    public void shouldSetAbsoluteUrlWithContentIdInHtml() throws Exception {
        MailAttachment inline1 = new MailAttachment();
        inline1.setId(100L);
        inline1.setContentId("1");

        MailAttachment inline2 = new MailAttachment();
        inline2.setId(102L);
        inline2.setContentId("2");

        Mail mail = new Mail();
        mail.setHtml("<img src=\"cid:1\"><link href=\"mid:2\"></link>");
        mail.setAttachments(Arrays.asList(inline1, inline2));

        String actualHtmlBody = sanitizer.sanitize(mail, uriComponentsBuilder);

        assertThat(actualHtmlBody, allOf(
                containsString("<img src=\"http://localhost:8080/mailAttachments/100/data\">"),
                containsString("<link href=\"http://localhost:8080/mailAttachments/102/data\"></link>")
        ));
    }
}
