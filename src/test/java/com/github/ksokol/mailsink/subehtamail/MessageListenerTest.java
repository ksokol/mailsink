package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.websocket.IncomingEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;

import static com.github.ksokol.mailsink.TestMails.emlAsStream;
import static com.github.ksokol.mailsink.TestMails.mixed1;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
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

    @Test
    public void shouldAcceptEveryMessage() throws Exception {
        assertThat(messageListener.accept("irrelevant", "irrelevant"), is(true));
    }

    @Test
    public void shouldPublishIncomingMail() throws Exception {
        messageListener.deliver("irrelevant", "irrelevant", emlAsStream("mixed1"));

        verify(publisher).publishEvent(argThat(hasProperty("source", hasProperty("source", is(mixed1())))));
    }

    @Test
    public void shouldPublishIncomingEvent() throws Exception {
        messageListener.deliver("irrelevant", "irrelevant", emlAsStream("mixed1"));

        verify(publisher).publishEvent(argThat(instanceOf(IncomingEvent.class)));
    }
}
