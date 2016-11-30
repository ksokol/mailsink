package com.github.ksokol.mailsink.bootstrap;

import com.github.ksokol.mailsink.entity.Mail;
import com.github.ksokol.mailsink.converter.MailConverter;
import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Kamill Sokol
 */
@RunWith(MockitoJUnitRunner.class)
public class ExampleMailsTest {

    @InjectMocks
    private ExampleMails exampleMails;

    @Mock
    private MailConverter mailConverter;

    @Before
    public void setUp() throws Exception {
        int fileCount = new PathMatchingResourcePatternResolver().getResources(String.format("/%s/**", "example")).length;
        assertThat(fileCount, is(3));
    }

    @Test
    public void shouldConvertTwoFiles() throws Exception {
        exampleMails.listExampleMails();

        verify(mailConverter, times(2)).convert(any());
    }

    @Test
    public void shouldReturnBothMails() throws Exception {
        Mail mail1 = new Mail();
        Mail mail2 = new Mail();

        when(mailConverter.convert(any())).thenReturn(mail1).thenReturn(mail2);

        List<Mail> expected = exampleMails.listExampleMails();

        assertThat(expected, is(asList(mail1, mail2)));
    }

    @Test
    public void shouldDetectFilesWithEmlFileExtension() throws Exception {
        ArgumentCaptor<InputStream> mailCaptor = ArgumentCaptor.forClass(InputStream.class);
        when(mailConverter.convert(mailCaptor.capture())).thenReturn(any(Mail.class));

        exampleMails.listExampleMails();

        assertThat(mailCaptor.getAllValues().get(0), containsString("1"));
        assertThat(mailCaptor.getAllValues().get(1), containsString("2"));
    }

    @Test
    public void shouldContinueWhenExceptionThrown() throws Exception {
        when(mailConverter.convert(any(InputStream.class))).thenThrow(new IllegalArgumentException()).thenReturn(new Mail());

        List<Mail> expected = exampleMails.listExampleMails();

        assertThat(expected, hasSize(1));
    }

    public static Matcher<InputStream> containsString(String expected) {
        return new BaseMatcher<InputStream>() {

            private String actual;

            @Override
            public boolean matches(Object object) {
                try {
                    actual = IOUtils.toString((InputStream) object).trim();
                    return actual.contains(expected);
                } catch (IOException exception) {
                    throw new IllegalArgumentException(exception.getMessage(), exception);
                }
            }

            public void describeMismatch(Object item, Description description) {
                description.appendText("was ").appendValue(actual);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue(expected);
            }
        };
    }
}
