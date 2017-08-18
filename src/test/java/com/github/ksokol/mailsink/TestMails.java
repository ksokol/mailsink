package com.github.ksokol.mailsink;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * @author Kamill Sokol
 */
public class TestMails {

    public static String mixed1() throws IOException {
        return IOUtils.toString(new ClassPathResource("mime4j/mixed1.eml").getInputStream());
    }
}
