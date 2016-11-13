package com.github.ksokol.mailsink.mime4j;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Kamill Sokol
 */
public final class Mime4jAttachment {

    private final String filename;
    private final String mimeType;
    private final byte[] data;

    Mime4jAttachment(String filename, String mimeType, InputStream data) throws IOException {
        this.filename = filename;
        this.mimeType = mimeType;
        this.data = IOUtils.toByteArray(data);
    }

    public String getFilename() {
        return filename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
