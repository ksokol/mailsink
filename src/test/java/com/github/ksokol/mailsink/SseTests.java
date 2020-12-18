package com.github.ksokol.mailsink;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;

@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.mail.port=" + SseTests.SMTP_PORT})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SseTests {

  static final int SMTP_PORT = 12500;
  private static final String TOPIC_INCOMING_MAIL = "mails/stream";
  private static final String TOPIC_SMTP_LOG = "smtp/stream";

  @LocalServerPort
  private int localServerPort;

  @Test
  void shouldSendIncomingMailWhenMailReceived() {
    ParameterizedTypeReference<Map<String, Long>> type = new ParameterizedTypeReference<>() {};
    List<Map<String, Long>> messages = new ArrayList<>();

    connectToWebSse(TOPIC_INCOMING_MAIL)
      .bodyToFlux(type)
      .subscribe(messages::add);

    await().pollDelay(Duration.ofSeconds(1)).until(this::sendMail);

    await().atMost(Duration.ofSeconds(10))
      .pollDelay(Duration.ofMillis(100))
      .untilAsserted(() ->
        assertThat(messages)
          .hasSize(1)
          .contains(Map.of("id", 2L))
      );
  }

  @Test
  void shouldSendSmtpLogWhenMailReceived() {
    ParameterizedTypeReference<Map<String, String>> type = new ParameterizedTypeReference<>() {};
    List<Map<String, String>> messages = new ArrayList<>();

    connectToWebSse(TOPIC_SMTP_LOG)
      .bodyToFlux(type)
      .subscribe(messages::add);

    LocalDateTime timeJustBeforeSendingTheMail = LocalDateTime.now(ZoneId.of("UTC"));
    await().pollDelay(Duration.ofSeconds(1)).until(this::sendMail);

    await().atMost(Duration.ofSeconds(10))
      .pollDelay(Duration.ofMillis(100))
      .untilAsserted(() ->
        assertThat(messages)
          .hasSize(13)
          .allMatch(line -> timeJustBeforeSendingTheMail.isBefore(LocalDateTime.parse(line.get("time"))))
          .extracting("number", "line")
          .contains(tuple("5", "Client: MAIL FROM:<expectedFrom>"))
          .contains(tuple("13", "Server: 221 Bye"))
      );

  }

  private WebClient.ResponseSpec connectToWebSse(String uri) {
    return WebClient.builder()
      .baseUrl(String.format("http://localhost:%d", localServerPort))
      .build()
      .get()
      .uri(uri)
      .retrieve();
  }

  private boolean sendMail() {
    var mailSender = new JavaMailSenderImpl();
    mailSender.setHost("localhost");
    mailSender.setPort(SMTP_PORT);

    var message = new SimpleMailMessage();
    message.setFrom("expectedFrom");
    message.setTo("to");
    message.setText("text");

    mailSender.send(message);
    return true;
  }
}
