package com.github.ksokol.mailsink.subehtamail;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.stereotype.Component;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author Kamill Sokol
 */
@Component
public class SmtpServerWrapper {

    private final MessageListener messageListener;
    private final MailProperties mailProperties;

    private SMTPServer smtpServer;

    public SmtpServerWrapper(MessageListener messageListener, MailProperties mailProperties) {
        this.messageListener = messageListener;
        this.mailProperties = mailProperties;
    }

    public boolean isRunning() {
        return smtpServer.isRunning();
    }

    @PreDestroy
    public void stop() {
        smtpServer.stop();
    }

    @PostConstruct
    public void start() {
        smtpServer = create();
        smtpServer.setPort(mailProperties.getPort());
        smtpServer.start();
    }

    protected SMTPServer create() {
        return new SMTPServer(new SimpleMessageListenerAdapter(messageListener));
    }
}
