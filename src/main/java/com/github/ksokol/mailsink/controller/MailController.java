package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * @author Kamill Sokol
 */
@RestController
@RequestMapping("/mails")
public class MailController {

    private final MailRepository mailRepository;

    public MailController(MailRepository mailRepository) {
        this.mailRepository = requireNonNull(mailRepository, "mailRepository is null");
    }

    @PostMapping(value = "/{id}/html/query", produces = APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> htmlQuery(@PathVariable Long id, @RequestBody HtmlBodyQuery htmlBodyQuery) {
        Mail mail = mailRepository.findById(id).orElseThrow(NotFoundException::new);
        return htmlBodyQuery.query(mail);
    }

    @GetMapping(value = "/{id}/source", produces = TEXT_PLAIN_VALUE)
    public String source(@PathVariable Long id) {
        return mailRepository.findById(id).orElseThrow(NotFoundException::new).getSource();
    }

    @ResponseStatus(NO_CONTENT)
    @PostMapping("/purge")
    public void delete() {
        mailRepository.deleteAll();
    }
}
