package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.subehtamail.SmtpServerWrapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.NO_CONTENT;

/**
 * @author Kamill Sokol
 */
@RestController
@RequestMapping("smtpServer")
public class SmtpServerController {

    private final SmtpServerWrapper smtpServerWrapper;
    private final JavaMailSender javaMailSender;

    public SmtpServerController(SmtpServerWrapper smtpServerWrapper, JavaMailSender javaMailSender) {
        this.smtpServerWrapper = requireNonNull(smtpServerWrapper, "smtpServerWrapper is null");
        this.javaMailSender = requireNonNull(javaMailSender, "javaMailSender is null");
    }

    @GetMapping("status")
    public Map<String, Object> status() {
        return statusResponse();
    }

    @PostMapping("status/toggle")
    public Map<String, Object> toggle() {
        if(smtpServerWrapper.isRunning()) {
            smtpServerWrapper.stop();
        } else {
            smtpServerWrapper.start();
        }
        return statusResponse();
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

    private Map<String, Object> statusResponse() {
        return Collections.singletonMap("isRunning", smtpServerWrapper.isRunning());
    }
}
