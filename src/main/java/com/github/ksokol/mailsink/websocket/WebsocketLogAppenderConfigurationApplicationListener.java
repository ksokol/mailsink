package com.github.ksokol.mailsink.websocket;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * @author Kamill Sokol
 */
@Component
public class WebsocketLogAppenderConfigurationApplicationListener implements ApplicationListener<ApplicationReadyEvent> {

    private final List<String> loggerOfInterest = Arrays.asList(
            "org.subethamail.smtp.server.Session",
            "org.subethamail.smtp.server.ServerThread"
    );

    private final SimpMessagingTemplate template;

    public WebsocketLogAppenderConfigurationApplicationListener(SimpMessagingTemplate template) {
        this.template = requireNonNull(template, "template is null");
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        LoggerContext context = getLoggerContext();
        Appender<ILoggingEvent> websocketAppender = createWebsocketLogAppender(context);
        appendLogAppender(context, websocketAppender);

        websocketAppender.start();
    }

    private LoggerContext getLoggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    private Appender<ILoggingEvent> createWebsocketLogAppender(LoggerContext context) {
        Appender<ILoggingEvent> websocketAppender = new WebsocketLogAppender(template);
        websocketAppender.setContext(context);
        return websocketAppender;
    }

    private void appendLogAppender(LoggerContext context, Appender<ILoggingEvent> appender) {
        loggerOfInterest.forEach(loggerName -> {
            Logger logger = context.getLogger(loggerName);
            logger.addAppender(appender);
        });
    }
}
