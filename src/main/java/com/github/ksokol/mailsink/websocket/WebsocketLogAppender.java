package com.github.ksokol.mailsink.websocket;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kamill Sokol
 */
@Component(value = "websocketAppender")
public class WebsocketLogAppender extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    private static final String TOPIC_SMTP_LOG = "/topic/smtp-log";

    private final SimpMessagingTemplate template;

    public WebsocketLogAppender(SimpMessagingTemplate template) {
        this.template = template;
    }

    @Override
    protected void append(ILoggingEvent event) {
        Map<Object, Object> map = new HashMap<>();

        map.put("line", event.getFormattedMessage());
        map.put("time", formatIsoTime(event));

        template.convertAndSend(TOPIC_SMTP_LOG, map);
    }

    private static String formatIsoTime(ILoggingEvent event) {
        long timeStamp = event.getTimeStamp();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.of("UTC"));
        return localDateTime.format(formatter);
    }
}
