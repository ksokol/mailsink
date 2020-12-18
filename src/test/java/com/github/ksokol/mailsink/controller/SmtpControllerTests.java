package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.time.Duration;
import java.util.List;

import static com.github.ksokol.mailsink.controller.SmtpControllerTests.SMTP_PORT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.mail.port = " + SMTP_PORT})
class SmtpControllerTests {

  static final int SMTP_PORT = 12501;

  @Autowired
  private MockMvc mvc;

  @Autowired
  private MailRepository mailRepository;

  @Test
  void shouldConnectToSmtpServerWhenRunningIsTrue() throws Exception {
    mvc.perform(get("/smtp/status"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("isRunning", is(true)));

    probeSmtpConnection();
  }

  @Test
  void shouldNotConnectToSmtpServerWhenRunningIsFalse() throws Exception {
    mvc.perform(post("/smtp/status"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("isRunning", is(false)));

    assertThrows(ConnectException.class, this::probeSmtpConnection);
  }

  @Test
  void shouldConnectToSmtpServerAgainWhenRunningIsTrue() throws Exception {
    mvc.perform(post("/smtp/status"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("isRunning", is(false)));

    mvc.perform(post("/smtp/status"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("isRunning", is(true)));

    probeSmtpConnection();
  }

  @Test
  void shouldCreateAndSendDemoMail() throws Exception {
    mvc.perform(post("/smtp"))
      .andExpect(status().isNoContent());

    await().atMost(Duration.ofSeconds(10)).until(() -> mailRepository.findByRecipient("root@localhost") != null);

    List<Mail> mails = mailRepository.findByRecipient("root@localhost");

    assertThat(mails).hasSize(1);

    assertThat(mails.get(0))
      .hasFieldOrPropertyWithValue("sender", "root@localhost")
      .hasFieldOrPropertyWithValue("recipient", "root@localhost")
      .hasFieldOrPropertyWithValue("subject", "Subject")
      .extracting(Mail::getText).asString().isEqualToIgnoringNewLines("mail body");
  }

  private void probeSmtpConnection() throws IOException {
    new Socket("localhost", SMTP_PORT);
  }
}
