package com.github.ksokol.mailsink.subehtamail;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

/**
 * @author Kamill Sokol
 */
@Configuration
public class SubEthaMailConfiguration {

    private final MailConverter mailConverter;
    private final MailProperties mailProperties;
    private final ApplicationEventPublisher publisher;

    public SubEthaMailConfiguration(MailConverter mailConverter, MailProperties mailProperties, ApplicationEventPublisher publisher) {
        this.mailConverter = mailConverter;
        this.mailProperties = mailProperties;
        this.publisher = publisher;
    }

    @Bean
    public SMTPServer smtpServer() {
        SMTPServer smtpServer = new SMTPServer(new SimpleMessageListenerAdapter(messageListener()));
        smtpServer.setPort(mailProperties.getPort());
        return smtpServer;
    }

    private MessageListener messageListener() {
        return new MessageListener(mailConverter, publisher);
    }

}
