package com.github.ksokol.mailsink.resource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class StaticResourceTest {

    private static final Logger log = LoggerFactory.getLogger(StaticResourceTest.class);

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mvc;

    @Before
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }

    @Test
    public void shouldForwardToIndexHtml() throws Exception {
        mvc.perform(get("/")
            .header(ACCEPT, TEXT_HTML))
            .andExpect(forwardedUrl("index.html"));
    }

    @Test
    public void shouldHaveAtLeastOneJavascriptResourceFile() throws Exception {
        Elements scriptTags = indexHtml().head().getElementsByTag("script");

        assertThat(scriptTags).size().isGreaterThan(0);
    }

    @Test
    public void shouldResolveJavascriptResourceFiles() throws Exception {
        Elements scriptTags = indexHtml().head().getElementsByTag("script");

        for (Element scriptTag : scriptTags) {
            String srcAttribute = "/" + scriptTag.attr("src");

            mvc.perform(get(srcAttribute)
                .header(ACCEPT, TEXT_HTML))
                .andDo(result -> log.info(String.format("asserting %s exists", srcAttribute)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/javascript"));
        }
    }

    @Test
    public void shouldHaveAtLeastOneCssResourceFile() throws Exception {
        Elements linkTags = indexHtml().head().select("link[rel=\"stylesheet\"]");

        assertThat(linkTags).size().isGreaterThan(0);
    }

    @Test
    public void shouldResolveCssResourceFiles() throws Exception {
        Elements linkTags = indexHtml().head().select("link[rel=\"stylesheet\"]");

        for (Element linkTag : linkTags) {
            String hrefAttribute = "/" + linkTag.attr("href");

            mvc.perform(get(hrefAttribute)
                    .header(ACCEPT, TEXT_HTML))
                    .andDo(result -> log.info(String.format("asserting %s exists", hrefAttribute)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/css"));
        }
    }

    private Document indexHtml() throws IOException {
        return Jsoup.parse(new ClassPathResource("static/index.html").getFile(), UTF_8.name());
    }
}
