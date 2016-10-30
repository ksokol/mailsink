package com.github.ksokol.mailsink.resource;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
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
                .andExpect(jsonPath("_embedded.mails", hasSize(1)));
    }
}
