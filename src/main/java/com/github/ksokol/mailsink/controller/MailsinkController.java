package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.configuration.MailsinkConversionService;
import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.mime4j.Mime4jAttachment;
import com.github.ksokol.mailsink.mime4j.Mime4jMessage;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * @author Kamill Sokol
 */
@RestController
public class MailsinkController {

    private final MailRepository mailRepository;
    private final JavaMailSender javaMailSender;
    private final ConversionService conversionService;

    public MailsinkController(MailRepository mailRepository,
                              JavaMailSender javaMailSender,
                              @MailsinkConversionService ConversionService conversionService) {
        this.mailRepository = mailRepository;
        this.javaMailSender = javaMailSender;
        this.conversionService = conversionService;
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

    @GetMapping(value = "mails/{id}/html/{contentId:.*}")
    public ResponseEntity<byte[]> mailsHtmlContentId(@PathVariable Long id, @PathVariable String contentId) {
        Mail mail = mailRepository.findById(id).orElseThrow(NotFoundException::new);
        Mime4jMessage mime4jMessage = conversionService.convert(mail, Mime4jMessage.class);
        Mime4jAttachment inlineAttachment = mime4jMessage.getInlineAttachment(contentId).orElseThrow(NotFoundException::new);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CONTENT_TYPE, inlineAttachment.getMimeType());
        httpHeaders.add(CONTENT_DISPOSITION, inlineAttachment.getFilename());
        return new ResponseEntity<>(inlineAttachment.getData(), httpHeaders, HttpStatus.OK);
    }

    @PostMapping(value = "mails/{id}/html/query", produces = APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> mailsHtmlQuery(@PathVariable Long id, @RequestBody HtmlBodyQuery htmlBodyQuery) {
        Mail mail = mailRepository.findById(id).orElseThrow(NotFoundException::new);
        return htmlBodyQuery.query(mail);
    }

    @GetMapping(value = "mails/{id}/source", produces = TEXT_PLAIN_VALUE)
    public String mailsSource(@PathVariable Long id) {
        return mailRepository.findById(id).orElseThrow(NotFoundException::new).getSource();
    }
}
