package com.github.ksokol.mailsink.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.EXPIRES;
import static org.springframework.http.HttpHeaders.PRAGMA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {"spring.mail.port=13025"})
public class CacheHeaderFilterTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void name() throws Exception {
        mvc.perform(get("/mails/search/findAllOrderByCreatedAtDesc"))
                .andExpect(header().string(PRAGMA, "no-cache"))
                .andExpect(header().string(EXPIRES, "-1"))
                .andExpect(header().string(CACHE_CONTROL, "no-store"));
    }
}
