package com.github.ksokol.mailsink.subehtamail;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kamill Sokol
 */
@Configuration
public class SubEthaMailConfiguration {

    @Bean
    public MessageListener messageListener(MailConverter mailConverter, ApplicationEventPublisher publisher) {
        return new MessageListener(mailConverter, publisher);
    }
}
