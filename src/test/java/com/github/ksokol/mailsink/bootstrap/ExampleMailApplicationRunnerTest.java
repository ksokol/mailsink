package com.github.ksokol.mailsink.bootstrap;

import com.github.ksokol.mailsink.repository.MailRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.BEFORE_CLASS;

/**
 * @author Kamill Sokol
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext(classMode = BEFORE_CLASS)
public class ExampleMailApplicationRunnerTest {

    @Autowired
    private MailRepository mailRepository;

    @Test
    public void shouldImportExampleMailsAfterStartup() {
        assertThat(mailRepository.findAll(),
                hasItems(
                    hasProperty("subject", is("mail1")),
                    hasProperty("subject", is("mail2"))
                )
        );
    }

}
