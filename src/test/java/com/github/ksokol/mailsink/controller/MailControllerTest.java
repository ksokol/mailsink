package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@WebMvcTest(MailController.class)
public class MailControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MailRepository mailRepository;

    @Test
    public void shouldAnswerWith404WhenMailSourceForGivenIdNotFound() throws Exception {
        given(mailRepository.findById(1L)).willReturn(Optional.empty());

        mvc.perform(get("/mails/1/source"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldAnswerWitMailSourceWhenMailForGivenIdFound() throws Exception {
        Mail mail = new Mail();
        mail.setSource("source");
        given(mailRepository.findById(1L)).willReturn(Optional.of(mail));

        mvc.perform(get("/mails/1/source"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain;charset=UTF-8"))
                .andExpect(content().string("source"));
    }

    @Test
    public void shouldReturnBadRequestWhenQueryIsEmpty() throws Exception {
        given(mailRepository.findById(1L)).willReturn(Optional.of(new Mail()));

        mvc.perform(post("/mails/1/html/query")
                .contentType(APPLICATION_JSON)
                .content("{ \"xpath\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnEmptyQueryResultWhenHtmlBodyIsNull() throws Exception {
        given(mailRepository.findById(1L)).willReturn(Optional.of(new Mail()));

        mvc.perform(post("/mails/1/html/query")
                .contentType(APPLICATION_JSON)
                .content("{ \"xpath\": \"*\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().json("[]"));
    }

    @Test
    public void shouldReturnNotFoundWhenMailForGivenIdIsNotPresent() throws Exception {
        given(mailRepository.findById(1L)).willReturn(Optional.empty());

        mvc.perform(post("/mails/1/html/query")
                .contentType(APPLICATION_JSON)
                .content("{ \"xpath\": \"*\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnWholeHtmlBodyAsJsonWhenQueryingWithWildcard() throws Exception {
        Mail mail = new Mail();
        mail.setHtml("<div><p>p inner text");
        given(mailRepository.findById(1L)).willReturn(Optional.of(mail));

        mvc.perform(post("/mails/1/html/query")
                .contentType(APPLICATION_JSON)
                .content("{ \"xpath\": \"*\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().json("[{'html':{'children':[{'body':{'children':[{'div':{'children':[{'p':{'children':[{'text':'p inner text'}]}}]}}]}}]}}]"));
    }

    @Test
    public void shouldReturnInnerTextOfPTagAccordingToGivenXPathQuery() throws Exception {
        Mail mail = new Mail();
        mail.setHtml("<div><p>p inner text");
        given(mailRepository.findById(1L)).willReturn(Optional.of(mail));

        mvc.perform(post("/mails/1/html/query")
                .contentType(APPLICATION_JSON)
                .content("{ \"xpath\": \"//p/text()\"}"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_VALUE))
                .andExpect(content().json("[{'text':'p inner text'}]"));
    }

    @Test
    public void shouldDeleteAllMailsFromMailRepository() throws Exception {
        mvc.perform(post("/mails/purge"))
                .andExpect(status().isNoContent());

        verify(mailRepository).deleteAll();
    }
}
