package com.github.ksokol.mailsink.handler;

import com.github.ksokol.mailsink.entity.Mail;
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

    public MessageListener(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream body) throws IOException {
        Mail mail = new Mail();
        mail.setSender(from);
        mail.setRecipient(recipient);
        mailRepository.save(mail);
    }
}
