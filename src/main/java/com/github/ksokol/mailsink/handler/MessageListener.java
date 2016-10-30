package com.github.ksokol.mailsink.handler;

import com.github.ksokol.mailsink.repository.MailRepository;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamill Sokol
 */
@Component
public class MessageListener implements SimpleMessageListener {

    private final MailRepository mailRepository;
    private final MailConverter mailConverter;

    public MessageListener(MailRepository mailRepository, MailConverter mailConverter) {
        this.mailRepository = mailRepository;
        this.mailConverter = mailConverter;
    }

    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream body) throws IOException {
        mailRepository.save(mailConverter.convert(body));
    }
}
