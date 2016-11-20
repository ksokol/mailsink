package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@WebMvcTest(MailsinkController.class)
public class MailsinkControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MailRepository mailRepository;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    public void shouldPurgeAllMailsFromMailRepository() throws Exception {
        mvc.perform(post("/purge"))
                .andExpect(status().isNoContent());

        verify(mailRepository).deleteAll();
    }

    @Test
    public void shouldCreateAndSendDemoMail() throws Exception {
        mvc.perform(post("/createMail"))
                .andExpect(status().isNoContent());

        verify(javaMailSender).send(Matchers.<SimpleMailMessage>argThat(
                allOf(
                    hasProperty("from", is("root@localhost")),
                    hasProperty("to", is(new String[] {"root@localhost"})),
                    hasProperty("subject", is("Subject")),
                    hasProperty("text", is("mail body"))
                    )
                )
            );
    }

    @Test
    public void shouldAnswerWith404WhenMailForGivenIdNotFound() throws Exception {
        given(mailRepository.findOne(1L)).willReturn(null);

        mvc.perform(get("/mails/1/html"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldAnswerWithHtmlBodyWhenMailForGivenIdFound() throws Exception {
        Mail mail = new Mail();
        mail.setHtml("html");
        given(mailRepository.findOne(1L)).willReturn(mail);

        mvc.perform(get("/mails/1/html"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string("html"));
    }

    @Test
    public void shouldAnswerWitMailSourceWhenMailForGivenIdFound() throws Exception {
        Mail mail = new Mail();
        mail.setSource("source");
        given(mailRepository.findOne(1L)).willReturn(mail);

        mvc.perform(get("/mails/1/source"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("source"));
    }
}
