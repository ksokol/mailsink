package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.repository.MailRepository;
import org.subethamail.smtp.helper.SimpleMessageListener;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Kamill Sokol
 */
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
