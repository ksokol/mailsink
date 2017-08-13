package com.github.ksokol.mailsink.configuration;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.module.SimpleSerializers;
import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.entity.MailSerializer;
import com.github.ksokol.mailsink.mime4j.ContentIdSanitizer;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

/**
 * @author Kamill Sokol
 */
@Component
public class MailsinkSimpleModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    private final ContentIdSanitizer contentIdSanitizer = new ContentIdSanitizer();

    @Override
    public void setupModule(SetupContext context) {
        SimpleSerializers serializers = new SimpleSerializers();
        serializers.addSerializer(Mail.class, new MailSerializer(contentIdSanitizer));
        context.addSerializers(serializers);
    }
}
