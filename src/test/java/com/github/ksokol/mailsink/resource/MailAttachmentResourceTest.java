package com.github.ksokol.mailsink.resource;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.entity.MailAttachment;
import com.github.ksokol.mailsink.repository.MailAttachmentRepository;
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
public class MailAttachmentResourceTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MailRepository mailRepository;

    @Autowired
    private MailAttachmentRepository mailAttachmentRepository;

    private MockMvc mvc;
    private MailAttachment mailAttachment;

    @Before
    public void setUp() throws Exception {
        mvc = webAppContextSetup(wac)
                .alwaysExpect(status().isOk())
                .alwaysExpect(header().string(CONTENT_TYPE, "application/hal+json;charset=UTF-8"))
                .build();

        Mail mail = new Mail();
        mailRepository.save(mail);

        mailAttachment = new MailAttachment();
        mailAttachment.setMail(mail);
        mailAttachment = mailAttachmentRepository.save(mailAttachment);
    }

    @Test
    public void shouldEncloseMailAttachmentWithContentProperty() throws Exception {
        mvc.perform(get("/mailAttachments"))
                .andExpect(jsonPath("_embedded.mailAttachments..content", not(emptyArray())));
    }

    @Test
    public void shouldAddCustomLinks() throws Exception {
        mvc.perform(get("/mailAttachments/{id}", mailAttachment.getId()))
                .andExpect(jsonPath("_links.download.href", is(format("http://localhost/mailAttachments/%d/download", mailAttachment.getId()))));
    }
}
