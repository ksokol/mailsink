package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.repository.MailRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * @author Kamill Sokol
 */
@RestController
public class MailsinkController {

    private final MailRepository mailRepository;
    private final JavaMailSender javaMailSender;

    public MailsinkController(MailRepository mailRepository, JavaMailSender javaMailSender) {
        this.mailRepository = mailRepository;
        this.javaMailSender = javaMailSender;
    }

    @ResponseStatus(NO_CONTENT)
    @PostMapping("purge")
    public void purgeMails() {
        mailRepository.deleteAll();
    }

    @ResponseStatus(NO_CONTENT)
    @PostMapping("createMail")
    public void createMail() {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom("root@localhost");
        message.setTo("root@localhost");
        message.setSubject("Subject");
        message.setText("mail body");

        javaMailSender.send(message);
    }
}
