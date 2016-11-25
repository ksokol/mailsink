package com.github.ksokol.mailsink.websocket;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class WebsocketLogAppenderTest {

    @Mock
    private SimpMessagingTemplate template;

    @Mock
    private ILoggingEvent event;

    @InjectMocks
    private WebsocketLogAppender appender;

    @Test
    public void shouldEmitLogMessage() throws Exception {
        given(event.getFormattedMessage()).willReturn("formatted message");
        given(event.getTimeStamp()).willReturn(1020L);

        appender.append(event);

        Map<String, String> message = new HashMap<>();
        message.put("line", "formatted message");
        message.put("time", "1970-01-01T00:00:01.020");

        verify(template).convertAndSend("/topic/smtp-log", message);
    }
}
