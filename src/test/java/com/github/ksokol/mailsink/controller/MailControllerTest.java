package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.TestMails;
import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.mime4j.ContentIdSanitizer;
import com.github.ksokol.mailsink.mime4j.Mime4jMessage;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

    @MockBean(name = "mailsinkConversionService")
    private ConversionService conversionService;

    @MockBean
    private ContentIdSanitizer contentIdSanitizer;

    @Test
    public void shouldAnswerWith404WhenMailForGivenIdNotFound() throws Exception {
        given(mailRepository.findById(1L)).willReturn(Optional.empty());

        mvc.perform(get("/mails/1/html"))
                .andExpect(status().isNotFound());
    }

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
    public void shouldReturn404WhenContentIdIsUnknown() throws Exception {
        given(mailRepository.findById(1L)).willReturn(Optional.of(new Mail()));
        given(conversionService.convert(any(), eq(Mime4jMessage.class))).willReturn(mail());

        mvc.perform(get("/mails/1/html/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnContentForGivenContentId() throws Exception {
        Mail mail = new Mail();
        mail.setSource("source");

        given(mailRepository.findById(1L)).willReturn(Optional.of(mail));
        given(conversionService.convert(eq(mail), eq(Mime4jMessage.class))).willReturn(mail());
        given(contentIdSanitizer.sanitize(eq(mail), any(UriComponentsBuilder.class))).willReturn("source");

        mvc.perform(get("/mails/1/html/1367760625.51865ef16e3f6@swift.generated"))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, is("image/png")))
                .andExpect(header().string(CONTENT_DISPOSITION, is("bg1.png")))
                .andExpect(content().string("a"));
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
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
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
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
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
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(content().json("[{'text':'p inner text'}]"));
    }

    private Mime4jMessage mail() throws Exception {
        return new Mime4jMessage(TestMails.alternative1());
    }
}
