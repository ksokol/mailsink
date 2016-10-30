package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.entity.Mail;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author Kamill Sokol
 */
public class MailConverterTest {

    private MailConverter converter = new MailConverter();

    private Mail mail;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldConvertIOExceptionToRuntimeException() throws Exception {
        InputStream body = mock(InputStream.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                throw new IOException("expected exception");
            }
        });

        expectedException.expect(RuntimeException.class);
        expectedException.expectMessage("expected exception");

        converter.convert(body);
    }

    @Test
    public void shouldExtractMessageIdFromPlainTextMail() throws Exception {
        givenMail("plain1.txt");
        assertThat(mail.getMessageId(), is("<208544674.1.1477820621771.JavaMail.localhost@localhost>"));
    }

    @Test
    public void shouldExtractSenderAndDisplayNameFromPlainTextMail() throws Exception {
        givenMail("plain1.txt");
        assertThat(mail.getSender(), is("Display Name <sender@localhost>"));
    }

    @Test
    public void shouldExtractSenderFromPlainTextMail() throws Exception {
        givenMail("plain2.txt");
        assertThat(mail.getSender(), is("sender@localhost"));
    }

    @Test
    public void shouldExtractOneRecipientFromPlainTextMail() throws Exception {
        givenMail("plain1.txt");
        assertThat(mail.getRecipient(), is("recipient@localhost"));
    }

    @Test
    public void shouldIgnoreSecondRecipientInPlainTextMail() throws Exception {
        givenMail("plain2.txt");
        assertThat(mail.getRecipient(), is("recipient1@localhost"));
    }

    @Test
    public void shouldExtractSubjectFromPlainTextMail() throws Exception {
        givenMail("plain1.txt");
        assertThat(mail.getSubject(), is("Subject"));
    }

    @Test
    public void shouldNotExtractHtmlBodyFromHtmlMail() throws Exception {
        givenMail("plain2.txt");
        assertThat(mail.getBody(), nullValue());
    }

    @Test
    public void shouldExtractBodyFromPlainTextMail() throws Exception {
        givenMail("plain1.txt");
        assertThat(mail.getBody(), is("Mail body\r\n new line\r\n another line\r\n"));
    }

    @Test
    public void shouldExtractDateFromPlainTextMail() throws Exception {
        givenMail("plain1.txt");
        assertThat(mail.getCreatedAt(), is(Date.from(LocalDateTime.of(2016,10,30,10,10,10).toInstant(ZoneOffset.UTC))));
    }

    private void givenMail(String fileName) throws IOException {
        mail = converter.convert(new ClassPathResource(fileName).getInputStream());
    }
}
