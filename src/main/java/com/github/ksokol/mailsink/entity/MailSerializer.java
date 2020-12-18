package com.github.ksokol.mailsink.entity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.ksokol.mailsink.mime4j.ContentIdSanitizer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

public class MailSerializer extends JsonSerializer<Mail> implements java.io.Serializable {

  private static final long serialVersionUID = 3L;

  private static final ContentIdSanitizer contentIdSanitizer = new ContentIdSanitizer();

  @Override
  public void serialize(Mail value, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStringField("messageId", value.getMessageId());
    gen.writeStringField("recipient", value.getRecipient());
    gen.writeStringField("sender", value.getSender());
    gen.writeStringField("subject", value.getSubject());
    gen.writeStringField("text", value.getText());
    gen.writeObjectField("attachments", !CollectionUtils.isEmpty(value.getAttachments()));
    gen.writeObjectField("createdAt", value.getCreatedAt());
    writeHtmlField(value, gen);
  }

  private void writeHtmlField(Mail mail, JsonGenerator gen) throws IOException {
    var uriComponentsBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
    var sanitizedHtml = contentIdSanitizer.sanitize(mail, uriComponentsBuilder);

    if (StringUtils.isNotBlank(sanitizedHtml)) {
      gen.writeStringField("html", sanitizedHtml);
    } else {
      gen.writeNullField("html");
    }
  }
}
