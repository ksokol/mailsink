package com.github.ksokol.mailsink.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.ksokol.mailsink.mime4j.ContentIdSanitizer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * @author Kamill Sokol
 */
public class MailSerializer extends JsonSerializer<Mail> implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private final ContentIdSanitizer contentIdSanitizer;

    public MailSerializer(ContentIdSanitizer contentIdSanitizer) {
        this.contentIdSanitizer = requireNonNull(contentIdSanitizer, "contentIdSanitizer is null");
    }

    @Override
    public void serialize(Mail value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("messageId", value.getMessageId());
        gen.writeStringField("recipient", value.getRecipient());
        gen.writeStringField("sender", value.getSender());
        gen.writeStringField("subject", value.getSubject());
        gen.writeStringField("text", value.getText());
        gen.writeObjectField("attachments", !CollectionUtils.isEmpty(value.getAttachments()));
        gen.writeObjectField("createdAt", value.getCreatedAt());
        writeHtmlField(value, gen);
        gen.writeEndObject();
    }

    private void writeHtmlField(Mail mail, JsonGenerator gen) throws IOException {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        UriComponentsBuilder uriComponentsBuilder = ServletUriComponentsBuilder.fromContextPath(requestAttributes.getRequest());
        String sanitizedHtml = contentIdSanitizer.sanitize(mail, uriComponentsBuilder);

        if(StringUtils.isNotBlank(sanitizedHtml)) {
            gen.writeStringField("html", sanitizedHtml);
          } else {
            gen.writeNullField("html");
         }
    }
}
