package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.websocket.IncomingEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.convert.ConversionService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class MessageListenerTest {

    private static final InputStream SOME_INPUT_STREAM = new ByteArrayInputStream(new byte[]{});
    private final Mail someMail = new Mail();

    @InjectMocks
    private MessageListener messageListener;

    @Mock
    private ConversionService conversionService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Test
    public void shouldAcceptEveryMessage() throws Exception {
        assertThat(messageListener.accept("irrelevant", "irrelevant"), is(true));
    }

    @Test
    public void shouldCallMailConverter() throws Exception {
        given(conversionService.convert(any(), any())).willReturn(new Mail());

        messageListener.deliver("irrelevant", "irrelevant", SOME_INPUT_STREAM);

        verify(conversionService).convert(SOME_INPUT_STREAM, Mail.class);
    }

    @Test
    public void shouldPublishIncomingMail() throws Exception {
        given(conversionService.convert(any(), any())).willReturn(someMail);

        messageListener.deliver("irrelevant", "irrelevant", SOME_INPUT_STREAM);

        verify(publisher).publishEvent(argThat(hasProperty("source", is(someMail))));
    }

    @Test
    public void shouldConvertIncomingMessageBeforePublishingEvent() throws Exception {
        given(conversionService.convert(any(), any())).willReturn(someMail);

        messageListener.deliver("irrelevant", "irrelevant", SOME_INPUT_STREAM);

        InOrder callOrder = inOrder(conversionService, publisher);

        callOrder.verify(conversionService).convert(any(), any());
        callOrder.verify(publisher).publishEvent(argThat(hasProperty("source", instanceOf(Mail.class))));
    }

    @Test
    public void shouldPublishIncomingEvent() throws Exception {
        given(conversionService.convert(any(), any())).willReturn(someMail);

        messageListener.deliver("irrelevant", "irrelevant", SOME_INPUT_STREAM);

        verify(publisher).publishEvent(argThat(instanceOf(IncomingEvent.class)));
    }
}
