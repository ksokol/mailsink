package com.github.ksokol.mailsink;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

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

    public static InputStream emlAsStream(String filename) throws IOException {
        return new ClassPathResource(String.format("mime4j/%s.eml", filename)).getInputStream();
    }

    private static String eml(String filename) throws IOException {
        return IOUtils.toString(emlAsStream(filename));
    }
}
