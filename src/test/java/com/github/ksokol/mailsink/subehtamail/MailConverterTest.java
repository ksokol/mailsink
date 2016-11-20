package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.entity.MailAttachment;
import org.apache.commons.io.IOUtils;
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
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
        givenMail("plain1");
        assertThat(mail.getMessageId(), is("<208544674.1.1477820621771.JavaMail.localhost@localhost>"));
    }

    @Test
    public void shouldExtractSenderAndDisplayNameFromPlainTextMail() throws Exception {
        givenMail("plain1");
        assertThat(mail.getSender(), is("Display Name <sender@localhost>"));
    }

    @Test
    public void shouldExtractSenderFromPlainTextMail() throws Exception {
        givenMail("plain2");
        assertThat(mail.getSender(), is("sender@localhost"));
    }

    @Test
    public void shouldExtractOneRecipientFromPlainTextMail() throws Exception {
        givenMail("plain1");
        assertThat(mail.getRecipient(), is("recipient@localhost"));
    }

    @Test
    public void shouldIgnoreSecondRecipientInPlainTextMail() throws Exception {
        givenMail("plain2");
        assertThat(mail.getRecipient(), is("recipient1@localhost"));
    }

    @Test
    public void shouldExtractSubjectFromPlainTextMail() throws Exception {
        givenMail("plain1");
        assertThat(mail.getSubject(), is("Subject"));
    }

    @Test
    public void shouldNotExtractHtmlBodyFromPlainTextMail() throws Exception {
        givenMail("plain2");
        assertThat(mail.getText(), is(""));
    }

    @Test
    public void shouldExtractBodyFromPlainTextMail() throws Exception {
        givenMail("plain1");
        assertThat(mail.getText(), is(String.format("Mail body%nnew line%n")));
    }

    @Test
    public void shouldExtractBodyFromHtmlTextMail() throws Exception {
        givenMail("html1");
        assertThat(mail.getHtml(), is(String.format("<html>%n<body>%n<p>html mail</p>%n</body>%n</html>%n")));
    }

    @Test
    public void shouldNotExtractPlainBodyFromHtmlTextMail() throws Exception {
        givenMail("html1");
        assertThat(mail.getText(), is(""));
    }

    @Test
    public void shouldExtractDateFromPlainTextMail() throws Exception {
        givenMail("plain1");
        assertThat(mail.getCreatedAt(), is(Date.from(LocalDateTime.of(2016,10,30,10,10,10).toInstant(ZoneOffset.UTC))));
    }

    @Test
    public void shouldAttachMailSource() throws Exception {
        String source = "plain1";
        givenMail(source);

        assertThat(mail.getSource(), is(IOUtils.toString(fromSource(source))));
    }

    @Test
    public void shouldExtractAttachments() throws Exception {
        givenMail("plain1_attachment");

        List<MailAttachment> attachments = mail.getAttachments();

        assertThat(attachments, hasSize(1));
        assertThat(attachments.get(0).getFilename(), is("example.pdf"));
        assertThat(attachments.get(0).getMimeType(), is("application/pdf"));
        assertThat(attachments.get(0).getData(), is(new byte[] {97}));
        assertThat(attachments.get(0).getMimeType(), notNullValue());
    }

    private void givenMail(String name) throws IOException {
        mail = converter.convert(fromSource(name));
    }

    private static InputStream fromSource(String name) throws IOException {
        return new ClassPathResource("mime4j/" + name + ".eml").getInputStream();
    }
}
