package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.mime4j.Mime4jMessage;
import org.apache.james.mime4j.dom.Message;
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
            Message message = new MessageBuilder().parse(source).build();
            return convertInternal(new Mime4jMessage(message));
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage(), exception);
        }
    }

    private Mail convertInternal(Mime4jMessage source) throws IOException {
        Mail target = new Mail();

        target.setMessageId(source.getMessageId());
        target.setSender(source.getSender());
        target.setRecipient(source.getRecipient());
        target.setSubject(source.getSubject());
        target.setBody(source.getPlainTextPart());
        target.setCreatedAt(source.getDate());

        return target;
    }
}
