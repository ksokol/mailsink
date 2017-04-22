package com.github.ksokol.mailsink.converter;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.entity.MailAttachment;
import com.github.ksokol.mailsink.mime4j.Mime4jAttachment;
import com.github.ksokol.mailsink.mime4j.Mime4jMessage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.message.MessageBuilder;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kamill Sokol
 */
@Component
public class InputStreamToMailConverter implements Converter<InputStream, Mail> {

    @Override
    public Mail convert(InputStream source) {
        try {
            InputStream inputStream = buffer(source);
            Message message = new MessageBuilder().parse(inputStream).build();
            return setSource(inputStream, message);
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception.getMessage(), exception);
        }
    }

    private Mail convertInternal(Mime4jMessage source) {
        Mail target = new Mail();

        target.setMessageId(source.getMessageId());
        target.setSender(source.getSender());
        target.setRecipient(source.getRecipient());
        target.setSubject(source.getSubject());
        target.setText(source.getPlainTextPart());
        target.setHtml(source.getHtmlTextPart());
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

    private Mail setSource(InputStream inputStream, Message message) throws IOException {
        Mail mail = convertInternal(new Mime4jMessage(message));
        inputStream.reset();
        mail.setSource(IOUtils.toString(inputStream));
        return mail;
    }

    private static InputStream buffer(InputStream source) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        IOUtils.copy(source, byteArrayOutputStream);
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }
}
