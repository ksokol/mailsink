package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.converter.InputStreamToMailConverter;
import com.github.ksokol.mailsink.sse.IncomingEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.InputStream;

import static java.util.Objects.requireNonNull;

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
    public void deliver(String from, String recipient, InputStream body) {
        var mail = converter.convert(body);
        publisher.publishEvent(new IncomingEvent(mail));
    }
}
