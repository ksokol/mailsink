package com.github.ksokol.mailsink.mime4j;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.entity.MailAttachment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Kamill Sokol
 */
public class ContentIdSanitizer implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    public String sanitize(Mail mail, UriComponentsBuilder uriComponentsBuilder) {
        uriComponentsBuilder.pathSegment("mailAttachments");
        return sanitizeContentId(uriComponentsBuilder, mail);
    }

    private static String sanitizeContentId(UriComponentsBuilder uriComponentsBuilder, Mail mail) {
        String htmlBody = StringUtils.defaultString(mail.getHtml());
        if(CollectionUtils.isEmpty(mail.getAttachments())) {
            return htmlBody;
        }
        for (MailAttachment attachment : mail.getAttachments()) {
            htmlBody = sanitizeHtml(htmlBody, attachment, uriComponentsBuilder.cloneBuilder());
        }
        return htmlBody;
    }

    private static String sanitizeHtml(String htmlBody, MailAttachment attachment, UriComponentsBuilder uriComponentsBuilder) {
        String contentUrl = uriComponentsBuilder.pathSegment(attachment.getId().toString()).pathSegment("data").build().toUriString();
        String replacedCid = htmlBody.replaceAll("cid:" + attachment.getContentId(), contentUrl);
        return replacedCid.replaceAll("mid:" + attachment.getContentId(), contentUrl);
    }
}
