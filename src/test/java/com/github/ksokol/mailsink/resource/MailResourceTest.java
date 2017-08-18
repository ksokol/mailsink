package com.github.ksokol.mailsink.resource;

import com.github.ksokol.mailsink.TestMails;
import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static java.lang.String.format;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

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
        mvc = webAppContextSetup(wac)
                .alwaysExpect(status().isOk())
                .alwaysExpect(header().string(CONTENT_TYPE, "application/hal+json;charset=UTF-8"))
                .build();
    }

    @Test
    public void shouldEncloseMailWithContentProperty() throws Exception {
        Mail mail = new Mail();
        mail.setSource(TestMails.mixed1());
        mailRepository.save(mail);

        mvc.perform(get("/mails"))
                .andExpect(jsonPath("_embedded.mails..content", not(emptyArray())));
    }

    @Test
    public void shouldAddCustomLinks() throws Exception {
        Mail mail = new Mail();
        mail.setSource(TestMails.mixed1());
        Mail saved = mailRepository.save(mail);

        mvc.perform(get("/mails/{id}", saved.getId()))
                .andExpect(jsonPath("_links.source.href", is(format("http://localhost/mails/%d/source", saved.getId()))))
                .andExpect(jsonPath("_links.attachments.href", is(format("http://localhost/mails/%d/attachments", saved.getId()))))
                .andExpect(jsonPath("_links.query").doesNotExist());
    }

    @Test
    public void shouldAddCustomLinkQueryWhenHtmlBodyAvailable() throws Exception {
        Mail mail = new Mail();
        mail.setHtml("html");
        mail.setSource(TestMails.mixed1());
        Mail saved = mailRepository.save(mail);

        mvc.perform(get("/mails/{id}", saved.getId()))
                .andExpect(jsonPath("_links.query.href", is(format("http://localhost/mails/%d/html/query", saved.getId()))));
    }

    @Test
    public void shouldReturnMailsFromCustomFinderMethodFindByRecipient() throws Exception {
        String expectedRecipient = "recipient@localhost";
        Mail mail = new Mail();
        mail.setRecipient(expectedRecipient);
        mail.setSource(TestMails.mixed1());
        mailRepository.save(mail);

        mvc.perform(get("/mails/search/findByRecipient?recipient={recipient}", expectedRecipient))
                .andExpect(jsonPath("_embedded.mails..recipient", everyItem(is(expectedRecipient))));
    }

    @Test
    public void shouldReturnMailsFromCustomFinderMethodFindAllOrderByCreatedAtDesc() throws Exception {
        Mail mail = new Mail();
        mail.setSource(TestMails.mixed1());
        mailRepository.save(mail);

        mvc.perform(get("/mails/search/findAllOrderByCreatedAtDesc"))
                .andExpect(jsonPath("_embedded.mails", not(emptyArray())));
    }
}
