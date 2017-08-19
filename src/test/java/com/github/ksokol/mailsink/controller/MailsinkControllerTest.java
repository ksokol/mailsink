package com.github.ksokol.mailsink.controller;

import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Test
    public void shouldPurgeAllMailsFromMailRepository() throws Exception {
        mvc.perform(post("/purge"))
                .andExpect(status().isNoContent());

        verify(mailRepository).deleteAll();
    }
}
