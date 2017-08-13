package com.github.ksokol.mailsink.mime4j;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.message.MessageBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Kamill Sokol
 */
public class Mime4jMessage {

    private final Message message;
    private final Mime4jMessageBody body;

    public Mime4jMessage(Message message) {
        this.message = message;
        this.body = new Mime4jMessageBody(message);
    }

    public Mime4jMessage(String source) {
        Objects.requireNonNull(source, "source is null");
        try {
            InputStream inputStream = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
            this.message = new MessageBuilder().parse(inputStream).build();
            this.body = new Mime4jMessageBody(message);
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    public String getMessageId() {
        return message.getMessageId();
    }

    public String getSender() {
        Mailbox mailbox = message.getFrom().get(0);
        if(mailbox.getName() != null) {
            return String.format("%s <%s>", mailbox.getName(), mailbox.getAddress());
        }
        return mailbox.getAddress();
    }

    public String getRecipient() {
        //TODO add support for multiple recipients
        Address address = message.getTo().get(0);
        return address.toString();
    }

    public String getSubject() {
        return message.getSubject();
    }

    public Date getDate() {
        return message.getDate();
    }

    public String getPlainTextPart() {
        try {
            return body.getPlainTextPart();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getHtmlTextPart() {
        try {
            return body.getHtmlTextPart();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<Mime4jAttachment> getAttachments() {
        try {
            return body.getAttachments();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    public List<Mime4jAttachment> getInlineAttachments() {
        try {
            return body.getInlineAttachments();
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    public Optional<Mime4jAttachment> getInlineAttachment(String contentId) {
        try {
            List<Mime4jAttachment> inlineAttachments = body.getInlineAttachments();
            return inlineAttachments.stream().filter(mime4jAttachment -> contentId.equals(mime4jAttachment.getContentId())).findFirst();
        } catch (Exception exception) {
            return Optional.empty();
        }
    }
}
