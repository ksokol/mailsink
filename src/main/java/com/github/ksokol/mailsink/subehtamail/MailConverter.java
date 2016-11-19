package com.github.ksokol.mailsink.subehtamail;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.entity.MailAttachment;
import com.github.ksokol.mailsink.mime4j.Mime4jAttachment;
import com.github.ksokol.mailsink.mime4j.Mime4jMessage;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.message.MessageBuilder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kamill Sokol
 */
@Component
public class MailConverter implements Converter<InputStream, Mail> {

    @Override
    public Mail convert(InputStream source) {
        try {
            Message message = new MessageBuilder().parse(source).build();
            return convertInternal(new Mime4jMessage(message));
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    private Mail convertInternal(Mime4jMessage source) throws IOException {
        Mail target = new Mail();

        target.setMessageId(source.getMessageId());
        target.setSender(source.getSender());
        target.setRecipient(source.getRecipient());
        target.setSubject(source.getSubject());
        target.setText(source.getPlainTextPart());
        target.setCreatedAt(source.getDate());
        target.setAttachments(convertAttachments(source, target));

        return target;
    }

    private List<MailAttachment> convertAttachments(Mime4jMessage source, Mail target) {
        List<Mime4jAttachment> mime4jAttachments = source.getAttachments();
        List<MailAttachment> mailAttachments = new ArrayList<>(mime4jAttachments.size());

        for (Mime4jAttachment attachment : mime4jAttachments) {
            MailAttachment mailAttachment = new MailAttachment();
            mailAttachment.setFilename(attachment.getFilename());
            mailAttachment.setMimeType(attachment.getMimeType());
            mailAttachment.setData(attachment.getData());
            mailAttachment.setMail(target);
            mailAttachments.add(mailAttachment);
        }

        target.setAttachments(mailAttachments);

        return mailAttachments;
    }
}
