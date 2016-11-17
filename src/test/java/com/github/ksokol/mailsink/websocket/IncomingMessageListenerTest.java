package com.github.ksokol.mailsink.websocket;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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

    private Mail mail;

    @Before
    public void setUp() throws Exception {
        mail = new Mail();
        listener.handleIncomingEvent(new IncomingEvent(mail));
    }

    @Test
    public void shouldSaveIncomingMail() throws Exception {
        verify(mailRepository).save(mail);
    }

    @Test
    public void shouldPublishMail() throws Exception {
        verify(template).convertAndSend(anyString(), eq(mail));
    }

    @Test
    public void shouldPublishInProperTopic() throws Exception {
        verify(template).convertAndSend(eq("/topic/incoming-mail"), any(Mail.class));
    }

    @Test
    public void shouldSaveMailBeforePublish() throws Exception {
        InOrder callOrder = inOrder(mailRepository, template);

        callOrder.verify(mailRepository).save(Mockito.<Mail>any());
        callOrder.verify(template).convertAndSend(anyString(), Mockito.<Mail>any());
    }
}
