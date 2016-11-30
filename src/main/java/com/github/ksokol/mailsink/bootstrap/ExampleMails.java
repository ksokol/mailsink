package com.github.ksokol.mailsink.bootstrap;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.converter.MailConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Kamill Sokol
 */
@Component
class ExampleMails {

    private static final Logger log = LoggerFactory.getLogger(ExampleMailApplicationRunner.class);

    private static final String FOLDER = "example";
    private static final String FILE_EXTENSION = ".eml";

    private final MailConverter mailConverter;

    public ExampleMails(MailConverter mailConverter) {
        this.mailConverter = mailConverter;
    }

    public List<Mail> listExampleMails() throws IOException {
        List<Mail> exampleMails = new ArrayList<>();

        listEmlFiles().forEach(resource -> {
            try {
                log.info("importing example mail {}", resource.getFilename());
                exampleMails.add(mailConverter.convert(resource.getInputStream()));
            } catch (Exception exception) {
                log.warn(exception.getMessage(), exception);
            }
        });

        return exampleMails;
    }

    private Stream<Resource> listEmlFiles() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        return Arrays.stream(resolver.getResources(String.format("/%s/**", FOLDER)))
                .filter(resource -> resource.getFilename().endsWith(FILE_EXTENSION));

    }
}
