package com.github.ksokol.mailsink.converter;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.mime4j.Mime4jMessage;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static java.lang.String.format;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Kamill Sokol
 */
public class MailToMime4jMessageConverterTest {

    private MailToMime4jMessageConverter converter = new MailToMime4jMessageConverter();

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenMailIsNull() throws Exception {
        converter.convert(null);
    }

    @Test
    public void shouldConvertMail() throws Exception {
        Mail mail = givenMessage("plain1");
        Mime4jMessage actual = converter.convert(mail);

        assertThat(actual.getSubject(), is("Subject"));
    }

    private Mail givenMessage(String fileName) throws Exception {
        Mail mail = new Mail();
        InputStream inputStream = new ClassPathResource(format("mime4j/%s.eml", fileName)).getInputStream();
        mail.setSource(IOUtils.toString(inputStream));
        return mail;
    }
}
