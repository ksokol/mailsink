package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.repository.MailRepository;
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

    public MailsinkController(MailRepository mailRepository) {
        this.mailRepository = mailRepository;
    }

    @ResponseStatus(NO_CONTENT)
    @PostMapping("purge")
    public void purgeMails() {
        mailRepository.deleteAll();
    }
}
