package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.MailAttachment;
import com.github.ksokol.mailsink.repository.MailAttachmentRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * @author Kamill Sokol
 */
@RestController
@RequestMapping("/mailAttachments")
public class MailAttachmentController {

    private final MailAttachmentRepository mailAttachmentRepository;

    public MailAttachmentController(MailAttachmentRepository mailAttachmentRepository) {
        this.mailAttachmentRepository = requireNonNull(mailAttachmentRepository, "mailAttachmentRepository is null");
    }

    @GetMapping("/{id}/data")
    public ResponseEntity<byte[]> data(@PathVariable("id") Long id) {
        MailAttachment attachment = mailAttachmentRepository.findById(id).orElseThrow(NotFoundException::new);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(CONTENT_TYPE, attachment.getMimeType());
        httpHeaders.add(CONTENT_DISPOSITION, format("%s; filename=\"%s\"", attachment.getDispositionType(), attachment.getFilename()));

        return new ResponseEntity<>(attachment.getData(), httpHeaders, HttpStatus.OK);
    }
}
