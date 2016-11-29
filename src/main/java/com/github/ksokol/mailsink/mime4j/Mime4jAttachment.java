package com.github.ksokol.mailsink.mime4j;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * We do not support {@code extension-token} as defined in <a href="https://tools.ietf.org/html/rfc2183">RFC 2183</a>.
 *
 * @author Kamill Sokol
 */
public final class Mime4jAttachment {

    private final String filename;
    private final String contentId;
    private final String dispositionType;
    private final String mimeType;
    private final byte[] data;

    Mime4jAttachment(String filename, String mimeType, InputStream data) throws IOException {
        this(filename, null, "attachment", mimeType, data);
    }

    Mime4jAttachment(String filename, String contentId, String dispositionType, String mimeType, InputStream data) throws IOException {
        this.filename = filename;
        this.contentId = contentId;
        this.dispositionType = dispositionType;
        this.mimeType = mimeType;
        this.data = IOUtils.toByteArray(data);
    }

    public boolean isAttachment() {
        return "attachment".equals(dispositionType);
    }

    public boolean isInlineAttachment() {
       return "inline".equals(dispositionType);
    }

    public String getFilename() {
        return filename;
    }

    public String getContentId() {
        return contentId;
    }

    public String getMimeType() {
        return mimeType;
    }

    public byte[] getData() {
        return Arrays.copyOf(data, data.length);
    }
}
