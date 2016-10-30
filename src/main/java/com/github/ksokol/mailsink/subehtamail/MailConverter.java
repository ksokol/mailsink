package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.entity.Mail;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.dom.address.Address;
import org.apache.james.mime4j.dom.address.Mailbox;
import org.apache.james.mime4j.message.MessageBuilder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamill Sokol
 */
@Component
public class MailConverter implements Converter<InputStream, Mail> {

    @Override
    public Mail convert(InputStream source) {
        try {
            return convertInternal(source);
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    private Mail convertInternal(InputStream source) throws IOException {
        Message message = new MessageBuilder().parse(source).build();
        Mail target = new Mail();

        setMessageId(message, target);
        setSender(message, target);
        setRecipient(message, target);
        setSubject(message, target);
        setCreatedAt(message, target);
        setBody(message, target);

        return target;
    }

    private void setMessageId(Message source, Mail target) {
        target.setMessageId(source.getMessageId());
    }

    private void setSender(Message source, Mail target) {
        Mailbox mailbox = source.getFrom().get(0);
        if(mailbox.getName() != null) {
            target.setSender(String.format("%s <%s>", mailbox.getName(), mailbox.getAddress()));
        } else {
            target.setSender(mailbox.getAddress());
        }
    }

    private void setRecipient(Message source, Mail target) {
        //TODO add support for multiple recipients
        Address address = source.getTo().get(0);
        target.setRecipient(address.toString());
    }

    private void setSubject(Message source, Mail target) {
        target.setSubject(source.getSubject());
    }

    private void setBody(Message source, Mail target) throws IOException {
        //TODO add support for HTML
        if("text/plain".equals(source.getMimeType()) && source.getBody() instanceof TextBody) {
            TextBody textBody = (TextBody) source.getBody();
            target.setBody(IOUtils.toString(textBody.getReader()).trim());
        }
    }

    private void setCreatedAt(Message source, Mail target) {
        target.setCreatedAt(source.getDate());
    }
}
