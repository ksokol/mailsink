package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.configuration.MailsinkConversionService;
import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.websocket.IncomingEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamill Sokol
 */
@Component
public class MessageListener implements SimpleMessageListener {

    public ConversionService conversionService;
    public final ApplicationEventPublisher publisher;

    MessageListener(@MailsinkConversionService ConversionService conversionService, ApplicationEventPublisher publisher) {
        this.conversionService = conversionService;
        this.publisher = publisher;
    }

    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream body) throws IOException {
        Mail mail = conversionService.convert(body, Mail.class);
        publisher.publishEvent(new IncomingEvent(mail));
    }
}
