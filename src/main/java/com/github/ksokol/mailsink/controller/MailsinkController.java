package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

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

    @GetMapping(value = "mails/{id}/html", produces = TEXT_HTML_VALUE)
    public ResponseEntity<?> mailsHtml(@PathVariable Long id) {
        Mail mail = mailRepository.findOne(id);
        if(mail != null) {
            return ResponseEntity.ok(mail.getHtml());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "mails/{id}/source", produces = TEXT_PLAIN_VALUE)
    public ResponseEntity<?> mailsSource(@PathVariable Long id) {
        Mail mail = mailRepository.findOne(id);
        if(mail != null) {
            return ResponseEntity.ok(mail.getSource());
        }
        return ResponseEntity.notFound().build();
    }
}
