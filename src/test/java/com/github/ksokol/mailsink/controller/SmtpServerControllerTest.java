package com.github.ksokol.mailsink.controller;

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

import static com.github.ksokol.mailsink.controller.SmtpServerControllerTest.SMTP_PORT;
import static org.hamcrest.Matchers.is;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.mail.port = " + SMTP_PORT
})
public class SmtpServerControllerTest {

    static final int SMTP_PORT = 12501;

    @Autowired
    private MockMvc mvc;

    @Test
    public void shouldConnectToSmtpServerWhenRunningIsTrue() throws Exception {
        mvc.perform(get("/smtpServer/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isRunning", is(true)));

        probeSmtpConnection();
    }

    @Test(expected = ConnectException.class)
    public void shouldNotConnectToSmtpServerWhenRunningIsFalse() throws Exception {
        mvc.perform(post("/smtpServer/status/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isRunning", is(false)));

        probeSmtpConnection();
    }

    @Test
    public void shouldConnectToSmtpServerAgainWhenRunningIsTrue() throws Exception {
        mvc.perform(post("/smtpServer/status/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isRunning", is(false)));

        mvc.perform(post("/smtpServer/status/toggle"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("isRunning", is(true)));

        probeSmtpConnection();
    }

    public void probeSmtpConnection() throws IOException {
        new Socket("localhost", SMTP_PORT);
    }
}
