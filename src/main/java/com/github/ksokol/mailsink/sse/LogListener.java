package com.github.ksokol.mailsink.sse;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Component
public class LogListener implements ApplicationListener<ApplicationReadyEvent> {

  private final List<String> loggerOfInterest = Arrays.asList(
    "org.subethamail.smtp.server.Session",
    "org.subethamail.smtp.server.ServerThread"
  );

  private final SseEmitterHolder sseEmitterHolder;

  public LogListener(SseEmitterHolder sseEmitterHolder) {
    this.sseEmitterHolder = requireNonNull(sseEmitterHolder, "sseEmitterHolder is null");
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    var context = getLoggerContext();
    var appender = createLogAppender(context);
    appendLogAppender(context, appender);

    appender.start();
  }

  private LoggerContext getLoggerContext() {
    return (LoggerContext) LoggerFactory.getILoggerFactory();
  }

  private Appender<ILoggingEvent> createLogAppender(LoggerContext context) {
    var websocketAppender = new LogAppender(sseEmitterHolder);
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
