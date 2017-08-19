package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.util.List;

import static com.github.ksokol.mailsink.controller.SmtpControllerTest.SMTP_PORT;
import static org.awaitility.Awaitility.await;
import static org.awaitility.Duration.TWO_SECONDS;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.mail.port = " + SMTP_PORT})
public class SmtpControllerTest {

    static final int SMTP_PORT = 12501;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MailRepository mailRepository;

    @Test
    public void shouldConnectToSmtpServerWhenRunningIsTrue() throws Exception {
        mvc.perform(get("/smtp/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isRunning", is(true)));

        probeSmtpConnection();
    }

    @Test(expected = ConnectException.class)
    public void shouldNotConnectToSmtpServerWhenRunningIsFalse() throws Exception {
        mvc.perform(post("/smtp/status/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isRunning", is(false)));

        probeSmtpConnection();
    }

    @Test
    public void shouldConnectToSmtpServerAgainWhenRunningIsTrue() throws Exception {
        mvc.perform(post("/smtp/status/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isRunning", is(false)));

        mvc.perform(post("/smtp/status/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isRunning", is(true)));

        probeSmtpConnection();
    }

    @Test
    public void shouldCreateAndSendDemoMail() throws Exception {
        mvc.perform(post("/smtp/createMail"))
                .andExpect(status().isNoContent());

        await().atMost(TWO_SECONDS).until(() -> mailRepository.findByRecipient("root@localhost") != null);

        List<Mail> mails = mailRepository.findByRecipient("root@localhost");
        assertThat(mails, hasSize(1));

        Mail mail = mails.get(0);
        assertThat(mail.getSender(), is("root@localhost"));
        assertThat(mail.getRecipient(), is("root@localhost"));
        assertThat(mail.getSubject(), is("Subject"));
        assertThat(mail.getText(), equalToIgnoringWhiteSpace("mail body"));
    }

    public void probeSmtpConnection() throws IOException {
        new Socket("localhost", SMTP_PORT);
    }
}
