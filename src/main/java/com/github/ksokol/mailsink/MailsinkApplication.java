package com.github.ksokol.mailsink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

/**
 * @author Kamill Sokol
 */
@SpringBootApplication
public class MailsinkApplication {

    static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(MailsinkApplication.class, args);
    }
}
