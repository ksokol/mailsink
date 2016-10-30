package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.repository.MailRepository;
import com.github.ksokol.mailsink.subehtamail.MessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

/**
 * @author Kamill Sokol
 */
@Configuration
public class SubEthaMailConfiguration {

    private final MailRepository mailRepository;
    private final MailConverter mailConverter;

    public SubEthaMailConfiguration(MailRepository mailRepository, MailConverter mailConverter) {
        this.mailRepository = mailRepository;
        this.mailConverter = mailConverter;
    }

    @Bean
    public SMTPServer smtpServer() {
        return new SMTPServer(new SimpleMessageListenerAdapter(messageListener()));
    }

    private MessageListener messageListener() {
        return new MessageListener(mailRepository, mailConverter);
    }

}
