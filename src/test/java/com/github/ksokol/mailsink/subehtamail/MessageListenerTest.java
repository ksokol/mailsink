package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.websocket.IncomingEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import static com.github.ksokol.mailsink.TestMails.emlAsStream;
import static com.github.ksokol.mailsink.TestMails.mixed1;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageListenerTest {

    @InjectMocks
    private MessageListener messageListener;

    @Mock
    private ApplicationEventPublisher publisher;

    @Captor
    private ArgumentCaptor<ApplicationEvent> captor;

    @Test
    public void shouldAcceptEveryMessage() {
        assertThat(messageListener.accept("irrelevant", "irrelevant")).isTrue();
    }

    @Test
    public void shouldPublishIncomingMail() throws Exception {
        messageListener.deliver("irrelevant", "irrelevant", emlAsStream("mixed1"));

        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getSource()).hasFieldOrPropertyWithValue("source", mixed1());
    }

    @Test
    public void shouldPublishIncomingEvent() throws Exception {
        messageListener.deliver("irrelevant", "irrelevant", emlAsStream("mixed1"));

        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(IncomingEvent.class);
    }
}
