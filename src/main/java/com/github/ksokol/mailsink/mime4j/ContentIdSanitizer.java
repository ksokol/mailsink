package com.github.ksokol.mailsink.mime4j;

import com.github.ksokol.mailsink.configuration.MailsinkConversionService;
import com.github.ksokol.mailsink.entity.Mail;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Kamill Sokol
 */
@Component
public class ContentIdSanitizer {

    private final ConversionService conversionService;

    public ContentIdSanitizer(@MailsinkConversionService ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public String sanitize(Mail mail, UriComponentsBuilder uriComponentsBuilder) {
        uriComponentsBuilder.pathSegment("mails", mail.getId().toString(), "html");
        Mime4jMessage mime4jMessage = conversionService.convert(mail, Mime4jMessage.class);
        return sanitizeContentId(mail, uriComponentsBuilder, mime4jMessage);
    }

    private static String sanitizeContentId(Mail mail, UriComponentsBuilder uriComponentsBuilder, Mime4jMessage mime4jMessage) {
        String htmlBody = mail.getHtml();
        for (Mime4jAttachment mime4jAttachment : mime4jMessage.getInlineAttachments()) {
            htmlBody = sanitizeHtml(htmlBody, mime4jAttachment.getContentId(), uriComponentsBuilder.cloneBuilder());
        }
        return htmlBody;
    }

    private static String sanitizeHtml(String htmlBody, String contentId, UriComponentsBuilder uriComponentsBuilder) {
        String contentUrl = uriComponentsBuilder.pathSegment(contentId).build().toUriString();
        htmlBody = htmlBody.replaceAll("cid:" + contentId, contentUrl);
        htmlBody = htmlBody.replaceAll("mid:" + contentId, contentUrl);
        return htmlBody;
    }
}
