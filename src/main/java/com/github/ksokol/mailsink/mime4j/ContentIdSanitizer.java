package com.github.ksokol.mailsink.mime4j;

import com.github.ksokol.mailsink.entity.Mail;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Kamill Sokol
 */
public class ContentIdSanitizer implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public String sanitize(Mail mail, UriComponentsBuilder uriComponentsBuilder) {
        uriComponentsBuilder.pathSegment("mails", mail.getId().toString(), "html");
        Mime4jMessage mime4jMessage = new Mime4jMessage(mail.getSource());
        return sanitizeContentId(uriComponentsBuilder, mime4jMessage);
    }

    private static String sanitizeContentId(UriComponentsBuilder uriComponentsBuilder, Mime4jMessage mime4jMessage) {
        String htmlBody = mime4jMessage.getHtmlTextPart();
        for (Mime4jAttachment mime4jAttachment : mime4jMessage.getInlineAttachments()) {
            htmlBody = sanitizeHtml(htmlBody, mime4jAttachment.getContentId(), uriComponentsBuilder.cloneBuilder());
        }
        return htmlBody;
    }

    private static String sanitizeHtml(String htmlBody, String contentId, UriComponentsBuilder uriComponentsBuilder) {
        String contentUrl = uriComponentsBuilder.pathSegment(contentId).build().toUriString();
        String replacedCid = htmlBody.replaceAll("cid:" + contentId, contentUrl);
        return replacedCid.replaceAll("mid:" + contentId, contentUrl);
    }
}
