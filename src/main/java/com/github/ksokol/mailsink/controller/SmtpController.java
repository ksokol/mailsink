package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.sse.SseEmitterHolder;
import com.github.ksokol.mailsink.subehtamail.SmtpServerWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.Map;

import static com.github.ksokol.mailsink.MailsinkConstants.CACHE_CONTROL_NO_TRANSFORM;
import static com.github.ksokol.mailsink.MailsinkConstants.TOPIC_SMTP_LOG;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@RestController
@RequestMapping("smtp")
public class SmtpController {

  private final SmtpServerWrapper smtpServerWrapper;
  private final JavaMailSender javaMailSender;
  private final SseEmitterHolder sseEmitterHolder;

  public SmtpController(SmtpServerWrapper smtpServerWrapper, JavaMailSender javaMailSender, SseEmitterHolder sseEmitterHolder) {
    this.smtpServerWrapper = requireNonNull(smtpServerWrapper, "smtpServerWrapper is null");
    this.javaMailSender = requireNonNull(javaMailSender, "javaMailSender is null");
    this.sseEmitterHolder = requireNonNull(sseEmitterHolder, "sseEmitterHolder is null");
  }

  @GetMapping("status")
  public Map<String, Object> statusGet() {
    return statusResponse();
  }

  @PostMapping("status")
  public Map<String, Object> statusPost() {
    if (smtpServerWrapper.isRunning()) {
      smtpServerWrapper.stop();
    } else {
      smtpServerWrapper.start();
    }
    return statusResponse();
  }

  @ResponseStatus(NO_CONTENT)
  @PostMapping
  public void smtpPost() {
    var message = new SimpleMailMessage();

    message.setFrom("root@localhost");
    message.setTo("root@localhost");
    message.setSubject("Subject");
    message.setText("mail body");

    javaMailSender.send(message);
  }

  @GetMapping("stream")
  public ResponseEntity<SseEmitter> smtpLog() {
    var sseEmitter = new SseEmitter();
    sseEmitterHolder.add(TOPIC_SMTP_LOG, sseEmitter);
    return ResponseEntity.ok().header(CACHE_CONTROL, CACHE_CONTROL_NO_TRANSFORM).body(sseEmitter);
  }

  private Map<String, Object> statusResponse() {
    return Collections.singletonMap("isRunning", smtpServerWrapper.isRunning());
  }
}
