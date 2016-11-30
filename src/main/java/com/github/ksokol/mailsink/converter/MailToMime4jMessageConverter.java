package com.github.ksokol.mailsink.converter;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.mime4j.Mime4jMessage;
import org.apache.james.mime4j.message.MessageBuilder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author Kamill Sokol
 */
@Component
public class MailToMime4jMessageConverter implements Converter<Mail, Mime4jMessage> {

    @Override
    public Mime4jMessage convert(Mail source) {
        try {
            InputStream stream = new ByteArrayInputStream(source.getSource().getBytes(StandardCharsets.UTF_8));
            return new Mime4jMessage(new MessageBuilder().parse(stream).build());
        } catch (Exception exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }
}
