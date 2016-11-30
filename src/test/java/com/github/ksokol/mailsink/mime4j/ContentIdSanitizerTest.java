package com.github.ksokol.mailsink.mime4j;

import com.github.ksokol.mailsink.entity.Mail;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.message.MessageBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;

import static java.lang.String.format;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class ContentIdSanitizerTest {

    @Mock
    private ConversionService conversionService;

    @InjectMocks
    private ContentIdSanitizer sanitizer;

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
        InputStream inputStream = new ClassPathResource(format("mime4j/%s.eml", fileName)).getInputStream();
        Message message = new MessageBuilder().parse(inputStream).build();
        Mime4jMessage mime4jMessage = new Mime4jMessage(message);
        mail = new Mail();
        mail.setId(42L);
        mail.setHtml(mime4jMessage.getHtmlTextPart());
        when(conversionService.convert(any(Mail.class), eq(Mime4jMessage.class))).thenReturn(mime4jMessage);
    }
}
