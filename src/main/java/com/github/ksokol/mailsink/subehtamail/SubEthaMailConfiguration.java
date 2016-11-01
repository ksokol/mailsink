package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.repository.MailRepository;
import org.springframework.boot.autoconfigure.mail.MailProperties;
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
    private final MailProperties mailProperties;

    public SubEthaMailConfiguration(MailRepository mailRepository, MailConverter mailConverter, MailProperties mailProperties) {
        this.mailRepository = mailRepository;
        this.mailConverter = mailConverter;
        this.mailProperties = mailProperties;
    }

    @Bean
    public SMTPServer smtpServer() {
        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageListener()));
        smtpServer.setPort(mailProperties.getPort());
        return smtpServer;
    }

    private MessageListener messageListener() {
        return new MessageListener(mailRepository, mailConverter);
    }

}
