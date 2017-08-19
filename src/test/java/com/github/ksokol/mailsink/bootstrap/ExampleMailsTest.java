package com.github.ksokol.mailsink.bootstrap;

import com.github.ksokol.mailsink.entity.Mail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Kamill Sokol
 */
public class ExampleMailsTest {

    private ExampleMails exampleMails = new ExampleMails();

    @Before
    public void setUp() throws Exception {
        int fileCount = new PathMatchingResourcePatternResolver().getResources(String.format("/%s/**", "example")).length;
        assertThat(fileCount, is(3));
    }

    @Test
    public void shouldImportEmlFiles() throws Exception {
        List<Mail> mails = exampleMails.listExampleMails();

        assertThat(mails, contains(
                hasProperty("subject", is("mail1")),
                hasProperty("subject", is("mail2"))
        ));
    }
}
