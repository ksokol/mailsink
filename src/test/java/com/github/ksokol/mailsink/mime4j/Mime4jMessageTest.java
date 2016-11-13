package com.github.ksokol.mailsink.mime4j;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.message.MessageBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * @author Kamill Sokol
 */
public class Mime4jMessageTest {

    private Mime4jMessage message;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldReturnMessageId() throws Exception {
        givenMessage("plain1");

        assertThat(message.getMessageId(), is("<208544674.1.1477820621771.JavaMail.localhost@localhost>"));
    }

    @Test
    public void shouldReturnSenderAddressWithDisplayName() throws Exception {
        givenMessage("plain1");

        assertThat(message.getSender(), is("Display Name <sender@localhost>"));
    }

    @Test
    public void shouldReturnSenderAddress() throws Exception {
        givenMessage("html1");

        assertThat(message.getSender(), is("sender@localhost"));
    }

    @Test
    public void shouldReturnRecipientAddress() throws Exception {
        givenMessage("plain1");

        assertThat(message.getRecipient(), is("recipient@localhost"));
    }

    @Test
    public void shouldReturnSubject() throws Exception {
        givenMessage("plain1");

        assertThat(message.getSubject(), is("Subject"));
    }

    @Test
    public void shouldReturnDate() throws Exception {
        givenMessage("plain1");

        LocalDateTime localDateTime = LocalDateTime.of(2016, 10, 30, 11, 10, 10);
        Date expectedDate = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

        assertThat(message.getDate(), is(expectedDate));
    }

    @Test
    public void shouldReturnPlainTextPart() throws Exception {
        givenMessage("plain1");

        assertThat(message.getPlainTextPart(), is(format("Mail body%nnew line%n")));
    }

    @Test
    public void shouldReturnHtmlTextPart() throws Exception {
        givenMessage("html1");

        assertThat(message.getHtmlTextPart(), is(format("<html>%n<body>%n<p>html mail</p>%n</body>%n</html>%n")));
    }

    @Test
    public void shouldReturnAttachments() throws Exception {
        givenMessage("plain1_attachment");

        assertThat(message.getAttachments(), hasSize(greaterThan(0)));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenGetPlainTextPartCalled() throws Exception {
        givenFaultyMessage();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("expected message");

        message.getPlainTextPart();
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenGetHtmlTextPartCalled() throws Exception {
        givenFaultyMessage();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("expected message");

        message.getHtmlTextPart();
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenGetAttachmentsCalled() throws Exception {
        givenFaultyMessage();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("expected message");

        message.getAttachments();
    }

    private void givenMessage(String fileName) throws Exception {
        InputStream inputStream = new ClassPathResource(format("mime4j/%s.eml", fileName)).getInputStream();
        message = new Mime4jMessage(new MessageBuilder().parse(inputStream).build());
    }

    private void givenFaultyMessage() {
        Message mockMessage = mock(Message.class);

        given(mockMessage.getMimeType()).willThrow(new RuntimeException("expected message"));
        given(mockMessage.isMultipart()).willThrow(new RuntimeException("expected message"));

        message = new Mime4jMessage(mockMessage);
    }
}
