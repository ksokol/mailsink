package com.github.ksokol.mailsink.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.ksokol.mailsink.mime4j.ContentIdSanitizer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @author Kamill Sokol
 */
public class MailSerializer extends JsonSerializer<Mail> {

    private final ContentIdSanitizer contentIdSanitizer;

    public MailSerializer(ContentIdSanitizer contentIdSanitizer) {
        this.contentIdSanitizer = requireNonNull(contentIdSanitizer, "contentIdSanitizer is null");
    }

    @Override
    public void serialize(Mail value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", value.getId());
        gen.writeStringField("messageId", value.getMessageId());
        gen.writeStringField("recipient", value.getRecipient());
        gen.writeStringField("sender", value.getSender());
        gen.writeStringField("subject", value.getSubject());
        gen.writeStringField("text", value.getText());
        gen.writeObjectField("attachments", value.getAttachments());
        gen.writeObjectField("createdAt", value.getCreatedAt());
        writeHtmlField(value, gen);
        gen.writeEndObject();
    }

    private void writeHtmlField(Mail value, JsonGenerator gen) throws IOException {
        if (StringUtils.isNotBlank(value.getHtml())) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            UriComponentsBuilder uriComponentsBuilder = ServletUriComponentsBuilder.fromContextPath(requestAttributes.getRequest());
            String sanitizedHtml = contentIdSanitizer.sanitize(value, uriComponentsBuilder);
            gen.writeStringField("html", sanitizedHtml);
        } else {
            gen.writeNullField("html");
        }
    }
}
