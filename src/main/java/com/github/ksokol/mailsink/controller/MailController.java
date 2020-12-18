package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.repository.MailRepository;
import com.github.ksokol.mailsink.sse.SseEmitterHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

import static com.github.ksokol.mailsink.MailsinkConstants.CACHE_CONTROL_NO_TRANSFORM;
import static com.github.ksokol.mailsink.MailsinkConstants.TOPIC_INCOMING_MAIL;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

@RestController
@RequestMapping("/mails")
public class MailController {

  private final MailRepository mailRepository;
  private final SseEmitterHolder sseEmitterHolder;

  public MailController(MailRepository mailRepository, SseEmitterHolder sseEmitterHolder) {
    this.mailRepository = requireNonNull(mailRepository, "mailRepository is null");
    this.sseEmitterHolder = requireNonNull(sseEmitterHolder, "sseEmitterHolder is null");
  }

  @PostMapping(value = "/{id}/html/query", produces = APPLICATION_JSON_VALUE)
  public List<Map<String, Object>> htmlQuery(@PathVariable Long id, @RequestBody HtmlBodyQuery htmlBodyQuery) {
    var mail = mailRepository.findById(id).orElseThrow(NotFoundException::new);
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

  @GetMapping("stream")
  public ResponseEntity<SseEmitter> smtpLog() {
    var sseEmitter = new SseEmitter();
    sseEmitterHolder.add(TOPIC_INCOMING_MAIL, sseEmitter);
    return ResponseEntity.ok().header(CACHE_CONTROL, CACHE_CONTROL_NO_TRANSFORM).body(sseEmitter);
  }

}
