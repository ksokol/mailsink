package com.github.ksokol.mailsink.sse;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import static com.github.ksokol.mailsink.MailsinkConstants.TOPIC_SMTP_LOG;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;

class LogAppender extends AppenderBase<ILoggingEvent> {

  private static final String APPENDER_NAME = "sseAppender";
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
  private static final DateTimeFormatter formatterMillis = new DateTimeFormatterBuilder().appendFraction(NANO_OF_SECOND, 0, 3, false).toFormatter();

  private final AtomicLong lineNumber = new AtomicLong(0);
  private final SseEmitterHolder sseEmitterHolder;

  LogAppender(SseEmitterHolder sseEmitterHolder) {
    this.sseEmitterHolder = Objects.requireNonNull(sseEmitterHolder, "sseEmitterHolder is null");
    setName(APPENDER_NAME);
  }

  @Override
  protected void append(ILoggingEvent event) {
    Map<String, Object> map = new HashMap<>();

    map.put("number", lineNumber.incrementAndGet());
    map.put("line", event.getFormattedMessage());
    map.put("time", formatIsoTime(event));

    sseEmitterHolder.publish(TOPIC_SMTP_LOG, map);
  }

  private static String formatIsoTime(ILoggingEvent event) {
    var timeStamp = event.getTimeStamp();
    var localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timeStamp), ZoneId.of("UTC"));
    var millisWithPaddingRight = String.format("%-3s", localDateTime.format(formatterMillis)).replace(' ', '0');
    return localDateTime.format(formatter) + "." + millisWithPaddingRight;
  }
}
