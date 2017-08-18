package com.github.ksokol.mailsink.mime4j;

import io.vavr.control.Try;
import org.apache.commons.io.IOUtils;
import org.apache.james.mime4j.codec.DecodeMonitor;
import org.apache.james.mime4j.codec.DecoderUtil;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.SingleBody;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.stream.Field;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<Mime4jAttachment> getInlineAttachments() throws IOException, MimeTypeException {
        return getAllAttachments(message).stream().filter(Mime4jAttachment::isInlineAttachment).collect(Collectors.toList());
    }

    public List<Mime4jAttachment> getAttachments() throws IOException, MimeTypeException {
        return new ArrayList<>(getAllAttachments(message));
    }

    private static List<Mime4jAttachment> getAllAttachments(Message message) throws IOException, MimeTypeException {
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

    private static List<Mime4jAttachment> extractFromBodyPart(BodyPart bodyPart) {
        if(bodyPart.getBody() instanceof SingleBody) {
            return extractFromSingleBody(bodyPart).map(Collections::singletonList).orElseGet(Collections::emptyList);
        }
        return extractFromMessage((Message) bodyPart.getBody());
    }

    private static List<BodyPart> getBodyPartsFromMultipart(Multipart multipart) throws IOException, MimeTypeException {
        List<BodyPart> attachments = new ArrayList<>(multipart.getBodyParts().size());
        for (Entity entity : multipart.getBodyParts()) {
            BodyPart part = (BodyPart) entity;
            if ("attachment".equalsIgnoreCase(part.getDispositionType())) {
                attachments.add(part);
            }
            if (part.isMultipart()) {
                attachments.addAll(getBodyPartsFromMultipart((Multipart) part.getBody()));
            }
            if("message/rfc822".equals(part.getMimeType())) {
                attachments.add(part);
            }
            if("inline".equals(part.getDispositionType())) {
                attachments.add(part);
            }
        }
        return attachments;
    }

    private static Optional<Mime4jAttachment> extractFromSingleBody(BodyPart bodyPart) {
        SingleBody source = (SingleBody) bodyPart.getBody();
        String mimeType = bodyPart.getMimeType();
        String filename = getFileName(bodyPart);
        String contentId = extractContentId(bodyPart);

        if(filename == null && contentId == null) {
            // TODO Add support for text or html body with Content-Disposition inline
            return Optional.empty();
        }

        return Try.of(() -> Optional.of(
                new Mime4jAttachment(filename, contentId, bodyPart.getDispositionType(), mimeType, source.getInputStream()))
        ).get();
    }

    private static List<Mime4jAttachment> extractFromMessage(Message message) {
        if(message.getBody() instanceof TextBody) {
            return Collections.singletonList(extractFromTextBody(message));
        }
        //TODO Add support for nested messages
        return Collections.emptyList();
    }

    private static Mime4jAttachment extractFromTextBody(Message message) {
        TextBody textBody = (TextBody) message.getBody();
        MimeType mimeType = Try.of(() -> MIME_TYPES.forName(message.getMimeType())).get();
        String fileName = (message.getSubject() == null ? message.getMessageId() : message.getSubject()) + mimeType.getExtension();
        InputStream inputStream = Try.of(textBody::getInputStream).get();
        return Try.of(() -> new Mime4jAttachment(fileName, message.getMimeType(), inputStream)).get();
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
            return IOUtils.toString(part.getInputStream(), part.getMimeCharset());
        }
        return "";
    }

    private static String extractContentId(BodyPart bodyPart) {
        Field field = bodyPart.getHeader().getField("content-id");
        if(field != null) {
            String contentId = field.getBody();
            contentId = contentId.startsWith("<") ? contentId.substring(1) : contentId;
            contentId = contentId.endsWith(">") ? contentId.substring(0, contentId.length() - 1) : contentId;
            return contentId;
        }
        return null;
    }

    private static String getFileName(BodyPart bodyPart) {
        if (bodyPart.getFilename() != null) {
            return DecoderUtil.decodeEncodedWords(bodyPart.getFilename(), (DecodeMonitor) null);
        } else {
            //TODO https://issues.apache.org/jira/browse/MIME4J-109
            Field field = bodyPart.getHeader().getField("Content-Disposition");
            return new RFC2231Decoder().parse(field.getBody());
        }
    }
}
