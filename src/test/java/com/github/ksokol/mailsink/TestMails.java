package com.github.ksokol.mailsink;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * @author Kamill Sokol
 */
public final class TestMails {

    TestMails() {
        // prevent instantiation
    }

    public static String mixed1() throws IOException {
        return eml("mixed1");
    }

    public static String alternative1() throws IOException {
        return eml("alternative1");
    }

    private static String eml(String filename) throws IOException {
        return IOUtils.toString(new ClassPathResource(String.format("mime4j/%s.eml", filename)).getInputStream());
    }
}
