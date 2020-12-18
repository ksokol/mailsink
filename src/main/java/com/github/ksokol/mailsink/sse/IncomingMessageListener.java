package com.github.ksokol.mailsink.sse;

import com.github.ksokol.mailsink.repository.MailRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static com.github.ksokol.mailsink.MailsinkConstants.TOPIC_INCOMING_MAIL;
import static java.util.Collections.singletonMap;

@Component
class IncomingMessageListener {

  private final MailRepository mailRepository;
  private final SseEmitterHolder sseEmitterHolder;

  public IncomingMessageListener(MailRepository mailRepository, SseEmitterHolder sseEmitterHolder) {
    this.mailRepository = mailRepository;
    this.sseEmitterHolder = sseEmitterHolder;
  }

  @EventListener
  public void handleIncomingEvent(IncomingEvent event) {
    var incomingMail = event.getIncomingMail();
    mailRepository.save(incomingMail);
    sseEmitterHolder.publish(TOPIC_INCOMING_MAIL, singletonMap("id", incomingMail.getId()));
  }
}
