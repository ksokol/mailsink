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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * @author Kamill Sokol
 */
@RestController
@RequestMapping("/mails")
public class MailController {

    private final MailRepository mailRepository;
    private final ConversionService conversionService;

    public MailController(MailRepository mailRepository, @MailsinkConversionService ConversionService conversionService) {
        this.mailRepository = requireNonNull(mailRepository, "mailRepository is null");
        this.conversionService = requireNonNull(conversionService, "conversionService is null");
    }

    @GetMapping(value = "/{id}/html/{contentId:.*}")
    public ResponseEntity<byte[]> htmlContentId(@PathVariable Long id, @PathVariable String contentId) {
        Mail mail = mailRepository.findById(id).orElseThrow(NotFoundException::new);
        Mime4jMessage mime4jMessage = conversionService.convert(mail, Mime4jMessage.class);
        Mime4jAttachment inlineAttachment = mime4jMessage.getInlineAttachment(contentId).orElseThrow(NotFoundException::new);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CONTENT_TYPE, inlineAttachment.getMimeType());
        httpHeaders.add(CONTENT_DISPOSITION, inlineAttachment.getFilename());
        return new ResponseEntity<>(inlineAttachment.getData(), httpHeaders, HttpStatus.OK);
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
}
