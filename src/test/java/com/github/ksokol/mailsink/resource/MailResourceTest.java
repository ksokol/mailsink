package com.github.ksokol.mailsink.resource;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.entity.MailAttachment;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class MailResourceTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MailRepository mailRepository;

    private MockMvc mvc;

    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void shouldReturnMailsFromCustomFinderMethod() throws Exception {
        mailRepository.save(new Mail());

        mvc.perform(get("/mails/search/findAllOrderByCreatedAtDesc"))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, "application/hal+json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mails", not(emptyArray())));
    }

    @Test
    public void shouldInlineAttachments() throws Exception {
        String filename = "expectedFilename";
        Mail mail = new Mail();
        MailAttachment mailAttachment = new MailAttachment();
        mailAttachment.setFilename(filename);
        mailAttachment.setMail(mail);
        mail.setAttachments(Collections.singletonList(mailAttachment));

        mailRepository.save(mail);

        mvc.perform(get("/mails/search/findAllOrderByCreatedAtDesc"))
                .andExpect(status().isOk())
                .andExpect(header().string(CONTENT_TYPE, "application/hal+json;charset=UTF-8"))
                .andExpect(jsonPath("_embedded.mails..attachments..filename", hasSize(1)))
                .andExpect(jsonPath("_embedded.mails..attachments..filename", everyItem(is(filename))));
    }

    @Test
    public void shouldNotExposeMailSourceAttribute() throws Exception {
        Mail mail = new Mail();
        mail.setSource("source");

        mailRepository.save(mail);

        mvc.perform(get("/mails/search/findAllOrderByCreatedAtDesc"))
                .andExpect(jsonPath("_embedded.mails..source", hasSize(0)));
    }

    @Test
    public void shouldReplaceCidOrMidInHtmlBodyWithAbsoluteUrl() throws Exception {
        Mail mail = new Mail();
        mail.setHtml("<img=\"cid:1234\">");
        mail.setSource(IOUtils.toString(new ClassPathResource("mime4j/mixed1.eml").getInputStream()));

        mailRepository.save(mail);

        String expectedHtmlBody = String.format("<img src=\"http://localhost/mails/%d/html/1234\">", mail.getId());

        mvc.perform(get("/mails/search/findAllOrderByCreatedAtDesc"))
                .andExpect(jsonPath(String.format("_embedded.mails..content[?(@.id=='%d')].html", mail.getId()), everyItem(is(expectedHtmlBody))));
    }
}
