package com.github.ksokol.mailsink.mime4j;

import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.message.MessageBuilder;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

import static java.lang.String.format;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author Kamill Sokol
 */
public class Mime4jMessageBodyTest {

    private static final byte[] BODY = new byte[] { (byte) 'a' };

    private Mime4jMessageBody body;

    @Test
    public void shouldExtractPlainTextPart() throws Exception {
        givenMessage("plain1");

        assertThat(body.getPlainTextPart(), is(format("Mail body%nnew line%n")));
        assertThat(body.getHtmlTextPart(), is(""));
    }

    @Test
    public void shouldExtractHtmlTextPart() throws Exception {
        givenMessage("html1");

        assertThat(body.getHtmlTextPart(), is(format("<html>%n<body>%n<p>html mail</p>%n</body>%n</html>%n")));
        assertThat(body.getPlainTextPart(), is(""));
    }

    @Test
    public void shouldNotContainAttachments() throws Exception {
        givenMessage("plain1");

        assertThat(body.getAttachments(), hasSize(0));
    }

    @Test
    public void shouldContainPlainTextPartAndPdfAttachment() throws Exception {
        givenMessage("plain1_attachment");

        assertThat(body.getPlainTextPart(), is(format("This is a plain text message with a PDF attachment.%n")));
        assertThat(body.getAttachments(), hasSize(1));

        Mime4jAttachment attachment = body.getAttachments().get(0);

        assertThat(attachment.getFilename(), is("example.pdf"));
        assertThat(attachment.getContentId(), nullValue());
        assertThat(attachment.getMimeType(), is("application/pdf"));
        assertThat(attachment.getData(), equalTo(BODY));
    }

    @Test
    public void shouldContainHtmlTextPartAndPdfAttachment() throws Exception {
        givenMessage("html1_attachment");

        assertThat(body.getHtmlTextPart(), is(format("<html>%n<body>%n<p>html mail</p>%n</body>%n</html>%n")));
        assertThat(body.getAttachments(), hasSize(1));

        Mime4jAttachment attachment = body.getAttachments().get(0);

        assertThat(attachment.getFilename(), is("example.pdf"));
        assertThat(attachment.getContentId(), nullValue());
        assertThat(attachment.getMimeType(), is("application/pdf"));
        assertThat(attachment.getData(), equalTo(BODY));
    }

    @Test
    public void shouldContainPlainTextPartAndPlainTextBodyOfNestedMessage() throws Exception {
        givenMessage("nested1_mail");

        assertThat(body.getPlainTextPart(), is("nested message"));
        assertThat(body.getHtmlTextPart(), is(""));

        assertThat(body.getAttachments(), hasSize(1));
        Mime4jAttachment attachment = body.getAttachments().get(0);

        assertThat(attachment.getFilename(), is("Disposition Notification Test.txt"));
        assertThat(attachment.getContentId(), nullValue());
        assertThat(attachment.getMimeType(), is("text/plain"));
        assertThat(attachment.getData(), equalTo(BODY));
    }

    @Test
    public void shouldContainPlainTextPartAndHtmlTextBodyOfNestedMessage() throws Exception {
        givenMessage("nested2_mail");

        assertThat(body.getPlainTextPart(), is("nested message"));
        assertThat(body.getHtmlTextPart(), is(""));

        assertThat(body.getAttachments(), hasSize(1));
        Mime4jAttachment attachment = body.getAttachments().get(0);

        assertThat(attachment.getFilename(), is("<53FEB7F0.3030501@localhost>.html"));
        assertThat(attachment.getContentId(), nullValue());
        assertThat(attachment.getMimeType(), is("text/html"));
        assertThat(attachment.getData(), equalTo(BODY));
    }

    @Test
    public void shouldDecodeFilenameAccordingToRFC2231() throws Exception {
        givenMessage("plain2_attachment");

        assertThat(body.getAttachments(), hasSize(1));
        Mime4jAttachment attachment = body.getAttachments().get(0);

        assertThat(attachment.getFilename(), is("example.pdf"));
    }

    @Test
    public void shouldContainPlainTextPartAndHtmlTextPartAndAllAttachments() throws Exception {
        givenMessage("alternative1");

        assertThat(body.getHtmlTextPart(), is("<html><body><p>html mail</p></body></html>"));
        assertThat(body.getPlainTextPart(), is("Mail body"));
        assertThat(body.getAttachments(), hasSize(3));
    }

    @Test
    public void shouldContainInlineAttachments() throws Exception {
        givenMessage("alternative1");

        assertThat(body.getInlineAttachments(), hasSize(3));

        Mime4jAttachment firstInline = body.getInlineAttachments().get(0);
        assertThat(firstInline.getContentId(), is("1367760625.51865ef16cc8c@swift.generated"));
        assertThat(firstInline.getMimeType(), is("image/png"));
        assertThat(firstInline.getFilename(), is("logo.png"));
        assertThat(firstInline.getData(), is(BODY));

        Mime4jAttachment secondInline = body.getInlineAttachments().get(1);
        assertThat(secondInline.getContentId(), is("1367760625.51865ef16f798@swift.generated"));
        assertThat(secondInline.getMimeType(), is("image/png"));
        assertThat(secondInline.getFilename(), is("bg2.png"));
        assertThat(secondInline.getData(), is(BODY));

        Mime4jAttachment thirdInline = body.getInlineAttachments().get(2);
        assertThat(thirdInline.getContentId(), is("1367760625.51865ef16e3f6@swift.generated"));
        assertThat(thirdInline.getMimeType(), is("image/png"));
        assertThat(thirdInline.getFilename(), is("bg1.png"));
        assertThat(thirdInline.getData(), is(BODY));
    }

    @Test
    public void shouldContainPlainTextPartAndNoNestedMultipartMessage() throws Exception {
        givenMessage("nested3_mail");

        assertThat(body.getHtmlTextPart(), is(""));
        assertThat(body.getPlainTextPart(), is("nested message"));
        assertThat(body.getAttachments(), hasSize(0));
    }

    @Test
    public void shouldContainHtmlTextPartAndAttachmentInMultipartMessage() throws Exception {
        givenMessage("multipart1_html_attachment");

        assertThat(body.getHtmlTextPart(), equalToIgnoringWhiteSpace("<html><body>html body</body></html>"));
        assertThat(body.getAttachments(), hasSize(1));
    }

    @Test
    public void shouldContainMixedContent() throws Exception {
        givenMessage("mixed1");

        assertThat(body.getPlainTextPart(), is("A text message"));
        assertThat(body.getHtmlTextPart(), is("<img src=\"cid:1234\">"));

        assertThat(body.getAttachments(), hasSize(3));
        assertThat(body.getInlineAttachments(), hasSize(2));

        Mime4jAttachment firstInline = body.getInlineAttachments().get(0);
        assertThat(firstInline.getContentId(), is("1234"));
        assertThat(firstInline.getDispositionType(), is("inline"));
        assertThat(firstInline.getMimeType(), is("image/png"));
        assertThat(firstInline.getFilename(), is("favicon.png"));
        assertThat(firstInline.getData(), is(BODY));

        Mime4jAttachment secondInline = body.getInlineAttachments().get(1);
        assertThat(secondInline.getContentId(), nullValue());
        assertThat(secondInline.getDispositionType(), is("inline"));
        assertThat(secondInline.getMimeType(), is("text/plain"));
        assertThat(secondInline.getFilename(), is("data.txt"));
        assertThat(secondInline.getData(), is(BODY));

        Mime4jAttachment firstAttachment = body.getAttachments().get(0);
        assertThat(firstAttachment.getContentId(), is("1234"));
        assertThat(firstAttachment.getDispositionType(), is("inline"));
        assertThat(firstAttachment.getMimeType(), is("image/png"));
        assertThat(firstAttachment.getFilename(), is("favicon.png"));
        assertThat(firstAttachment.getData(), is(BODY));

        Mime4jAttachment secondAttachment = body.getAttachments().get(1);
        assertThat(secondAttachment.getContentId(), nullValue());
        assertThat(secondAttachment.getDispositionType(), is("attachment"));
        assertThat(secondAttachment.getMimeType(), is("image/png"));
        assertThat(secondAttachment.getFilename(), is("image.png"));
        assertThat(secondAttachment.getData(), is(BODY));

        Mime4jAttachment thirdAttachment = body.getAttachments().get(2);
        assertThat(thirdAttachment.getContentId(), nullValue());
        assertThat(thirdAttachment.getDispositionType(), is("inline"));
        assertThat(thirdAttachment.getMimeType(), is("text/plain"));
        assertThat(thirdAttachment.getFilename(), is("data.txt"));
        assertThat(thirdAttachment.getData(), is(BODY));
    }

    @Test
    public void shouldIgnoreAttachmentsWhenFilenameOrContentIdNotPresent() throws Exception {
        givenMessage("plain3_attachment");

        assertThat(body.getAttachments(), empty());
        assertThat(body.getInlineAttachments(), empty());
    }

    private void givenMessage(String fileName) throws Exception {
        InputStream inputStream = new ClassPathResource(format("mime4j/%s.eml", fileName)).getInputStream();
        Message message = new MessageBuilder().parse(inputStream).build();
        body = new Mime4jMessageBody(message);
    }

}
