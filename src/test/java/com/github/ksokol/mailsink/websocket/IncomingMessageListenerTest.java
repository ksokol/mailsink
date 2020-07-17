package com.github.ksokol.mailsink.websocket;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class IncomingMessageListenerTest {

    @Mock
    private MailRepository mailRepository;

    @Mock
    private SimpMessagingTemplate template;

    @InjectMocks
    private IncomingMessageListener listener;

    @Captor
    private ArgumentCaptor<String> destinationCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Long>> payloadCaptor;

    @Captor
    private ArgumentCaptor<Mail> mailCaptor;

    private Mail mail;

    @Before
    public void setUp() {
        mail = new Mail();
        mail.setId(999L);
        listener.handleIncomingEvent(new IncomingEvent(mail));
    }

    @Test
    public void shouldSaveIncomingMail() {
        verify(mailRepository).save(mailCaptor.capture());
        assertThat(mailCaptor.getValue()).isEqualTo(mail);
    }

    @Test
    public void shouldPublishMail() {
        verify(template).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).containsExactly(entry("id", 999L));
    }

    @Test
    public void shouldPublishInProperTopic() {
        verify(template).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());
        assertThat(destinationCaptor.getValue()).isEqualTo("/topic/incoming-mail");
    }

    @Test
    public void shouldSaveMailBeforePublish() {
        InOrder callOrder = inOrder(mailRepository, template);

        callOrder.verify(mailRepository).save(any(Mail.class));
        callOrder.verify(template).convertAndSend(anyString(), any(Map.class));
    }
}
