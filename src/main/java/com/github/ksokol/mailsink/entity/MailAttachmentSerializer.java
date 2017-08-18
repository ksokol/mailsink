package com.github.ksokol.mailsink.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * @author Kamill Sokol
 */
public class MailAttachmentSerializer extends JsonSerializer<MailAttachment> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public void serialize(MailAttachment value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("filename", value.getFilename());
        gen.writeStringField("mimeType", value.getMimeType());
        gen.writeStringField("dispositionType", value.getDispositionType());
        gen.writeEndObject();
    }
}
