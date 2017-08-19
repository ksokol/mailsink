package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.converter.InputStreamToMailConverter;
import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.websocket.IncomingEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.IOException;
import java.io.InputStream;

import static java.util.Objects.requireNonNull;

/**
 * @author Kamill Sokol
 */
@Component
class MessageListener implements SimpleMessageListener {

    private final ApplicationEventPublisher publisher;
    private final InputStreamToMailConverter converter = new InputStreamToMailConverter();

    MessageListener(ApplicationEventPublisher publisher) {
        this.publisher = requireNonNull(publisher, "publisher is null");
    }

    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream body) throws IOException {
        Mail mail = converter.convert(body);
        publisher.publishEvent(new IncomingEvent(mail));
    }
}
