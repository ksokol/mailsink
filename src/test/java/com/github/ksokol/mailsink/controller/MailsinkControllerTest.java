package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.mime4j.ContentIdSanitizer;
import com.github.ksokol.mailsink.mime4j.Mime4jMessage;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.apache.james.mime4j.message.MessageBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.InputStream;

import static java.lang.String.format;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

    @MockBean
    private ContentIdSanitizer contentIdSanitizer;

    @MockBean(name = "mailsinkConversionService")
    private ConversionService conversionService;

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
        given(contentIdSanitizer.sanitize(eq(mail), any(UriComponentsBuilder.class))).willReturn("html");

        mvc.perform(get("/mails/1/html"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string("html"));
    }

    @Test
    public void shouldAnswerWith404WhenMailSourceForGivenIdNotFound() throws Exception {
        given(mailRepository.findOne(1L)).willReturn(null);

        mvc.perform(get("/mails/1/source"))
                .andExpect(status().isNotFound());
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

    @Test
    public void shouldReturn404WhenContentIdIsUnknown() throws Exception {
        given(conversionService.convert(any(), eq(Mime4jMessage.class))).willReturn(givenMessage("alternative1"));

        mvc.perform(get("/mails/1/html/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnContentForGivenContentId() throws Exception {
        Mail mail = new Mail();
        mail.setSource("source");

        given(mailRepository.findOne(1L)).willReturn(mail);
        given(conversionService.convert(eq(mail), eq(Mime4jMessage.class))).willReturn(givenMessage("alternative1"));
        given(contentIdSanitizer.sanitize(eq(mail), any(UriComponentsBuilder.class))).willReturn("source");

        mvc.perform(get("/mails/1/html/1367760625.51865ef16e3f6@swift.generated"))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, is("image/png")))
                .andExpect(header().string(CONTENT_DISPOSITION, is("bg1.png")))
                .andExpect(content().string("a"));
    }

    private Mime4jMessage givenMessage(String fileName) throws Exception {
        InputStream inputStream = new ClassPathResource(format("mime4j/%s.eml", fileName)).getInputStream();
        return new Mime4jMessage(new MessageBuilder().parse(inputStream).build());
    }
}
