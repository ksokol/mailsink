package com.github.ksokol.mailsink;

import com.github.ksokol.mailsink.handler.MessageListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.subethamail.smtp.helper.SimpleMessageListenerAdapter;
import org.subethamail.smtp.server.SMTPServer;

/**
 * @author Kamill Sokol
 */
@SpringBootApplication
public class MailsinkApplication {

    static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(MailsinkApplication.class, args);
    }

    @Bean
    public SMTPServer smtpServer(MessageListener messageListener) {
        return new SMTPServer(new SimpleMessageListenerAdapter(messageListener));
    }
}
