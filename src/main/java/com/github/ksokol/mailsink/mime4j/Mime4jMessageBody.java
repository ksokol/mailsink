package com.github.ksokol.mailsink.mime4j;

import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.apache.james.mime4j.dom.BinaryBody;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.stream.Field;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Kamill Sokol
 */
final class Mime4jMessageBody {

    private static final MimeTypes MIME_TYPES = MimeTypes.getDefaultMimeTypes();

    private final Message message;

    Mime4jMessageBody(Message message) {
        this.message = message;
    }

    public String getPlainTextPart() throws IOException {
        return extractByMimeType(message, "text/plain");
    }

    public String getHtmlTextPart() throws IOException {
        return extractByMimeType(message, "text/html");
    }

    public List<Mime4jAttachment> getAttachments() throws IOException, MimeTypeException {
        if (!message.isMultipart()) {
            return Collections.emptyList();
        }
        return extractFromMultipart((Multipart) message.getBody());
    }

    private static String extractByMimeType(Message message, String mimeType) throws IOException {
        if (mimeType.equals(message.getMimeType())) {
            return extractText((TextBody) message.getBody());
        } else if (message.isMultipart()) {
            return extractText((Multipart) message.getBody(), mimeType);
        }
        return "";
    }

    private static List<Mime4jAttachment> extractFromMultipart(Multipart multipart) throws IOException, MimeTypeException {
        List<BodyPart> bodyParts = getBodyPartsFromMultipart(multipart);
        List<Mime4jAttachment> attachments = new ArrayList<>(bodyParts.size());
        for (BodyPart bodyPart : bodyParts) {
            attachments.addAll(extractFromBodyPart(bodyPart));
        }
        return attachments;
    }

    private static List<BodyPart> getBodyPartsFromMultipart(Multipart multipart) {
        List<BodyPart> attachments = new ArrayList<>(multipart.getBodyParts().size());
        for (Entity entity : multipart.getBodyParts()) {
            BodyPart part = (BodyPart) entity;
            if ("attachment".equalsIgnoreCase(part.getDispositionType())) {
                attachments.add(part);
                break;
            }
            if (part.isMultipart()) {
                attachments.addAll(getBodyPartsFromMultipart((Multipart) part.getBody()));
                break;
            }
            if("message/rfc822".equals(part.getMimeType())) {
                attachments.add(part);
                break;
            }
        }
        return attachments;
    }

    private static List<Mime4jAttachment> extractFromBodyPart(BodyPart bodyPart) throws IOException, MimeTypeException {
        if(bodyPart.getBody() instanceof BinaryBody) {
            return Collections.singletonList(extractFromBinaryBody(bodyPart));
        }
        return extractFromMessage((Message) bodyPart.getBody());
    }

    private static List<Mime4jAttachment> extractFromMessage(Message message) throws IOException, MimeTypeException {
        if(message.getBody() instanceof TextBody) {
            return Collections.singletonList(extractFromTextBody(message));
        }
        //TODO Add support for nested messages
        return Collections.emptyList();
    }

    private static Mime4jAttachment extractFromBinaryBody(BodyPart bodyPart) throws IOException {
        BinaryBody binaryBody = (BinaryBody) bodyPart.getBody();
        return new Mime4jAttachment(extractFileName(bodyPart), bodyPart.getMimeType(), binaryBody.getInputStream());
    }

    private static Mime4jAttachment extractFromTextBody(Message message) throws IOException, MimeTypeException {
        TextBody textBody = (TextBody) message.getBody();
        MimeType mimeType = MIME_TYPES.forName(message.getMimeType());
        String fileName = (message.getSubject() == null ? message.getMessageId() : message.getSubject()) + mimeType.getExtension();
        return new Mime4jAttachment(fileName, message.getMimeType(), textBody.getInputStream());
    }

    private static String extractFileName(BodyPart bodyPart) {
        if(bodyPart.getFilename() != null) {
            return DecoderUtil.decodeEncodedWords(bodyPart.getFilename(), Charset.forName(bodyPart.getCharset()));
        } else {
            //TODO remove me after https://issues.apache.org/jira/browse/MIME4J-109 has been implemented
            Field field = bodyPart.getHeader().getField("Content-Disposition");
            return new RFC2231Decoder().parse(field.getBody());
        }
    }

    private static String extractText(Multipart multipart, String mimeType) throws IOException {
        return extractText(extractTextBodyPart(multipart, mimeType));
    }

    private static TextBody extractTextBodyPart(Multipart multipart, String mimeType) {
        for (Entity entity : multipart.getBodyParts()) {
            if (mimeType.equals(entity.getMimeType())) {
                return (TextBody) entity.getBody();
            }
            if (entity.isMultipart()) {
                return extractTextBodyPart((Multipart) entity.getBody(), mimeType);
            }
        }
        return null;
    }

    private static String extractText(TextBody part) throws IOException {
        if (part != null) {
            String mimeCharset = part.getMimeCharset() == null ? UTF_8.name() : part.getMimeCharset();
            return IOUtils.toString(part.getInputStream(), mimeCharset);
        }
        return "";
    }
}
