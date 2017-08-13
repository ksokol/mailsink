package com.github.ksokol.mailsink.mime4j;

import com.github.ksokol.mailsink.entity.Mail;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
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
    private Mail mail;

    @Before
    public void setUp() throws Exception {
        uriComponentsBuilder = UriComponentsBuilder.fromUriString("http://localhost:8080");
    }

    @Test
    public void shouldNotSanitizeHtmlWithoutInlineAttachments() throws Exception {
        givenMime4jMessage("html1");

        String actualHtmlBody = sanitizer.sanitize(mail, uriComponentsBuilder);

        assertThat(actualHtmlBody, is(format("<html>%n<body>%n<p>html mail</p>%n</body>%n</html>%n")));
    }

    @Test
    public void shouldSetAbsoluteUrlWithContentIdInHtml() throws Exception {
        givenMime4jMessage("alternative2");

        String actualHtmlBody = sanitizer.sanitize(mail, uriComponentsBuilder);

        assertThat(actualHtmlBody, allOf(
                containsString("http://localhost:8080/mails/42/html/1367760625.51865ef16e3f6@swift.generated"),
                containsString("http://localhost:8080/mails/42/html/1367760625.51865ef16cc8c@swift.generated"),
                containsString("http://localhost:8080/mails/42/html/1367760625.51865ef16f798@swift.generated)")
        ));
    }

    private void givenMime4jMessage(String fileName) throws IOException {
        String source = IOUtils.toString(new ClassPathResource(format("mime4j/%s.eml", fileName)).getInputStream(), UTF_8.name());
        mail = new Mail();
        mail.setId(42L);
        mail.setSource(source);
    }
}
