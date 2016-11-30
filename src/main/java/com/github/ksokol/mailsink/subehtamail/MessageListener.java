package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.converter.MailConverter;
import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.websocket.IncomingEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamill Sokol
 */
@Component
class MessageListener implements SimpleMessageListener {

    private final MailConverter mailConverter;
    private final ApplicationEventPublisher publisher;

    MessageListener(MailConverter mailConverter, ApplicationEventPublisher publisher) {
        this.mailConverter = mailConverter;
        this.publisher = publisher;
    }

    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream body) throws IOException {
        Mail mail = mailConverter.convert(body);
        publisher.publishEvent(new IncomingEvent(mail));
    }
}
