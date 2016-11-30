package com.github.ksokol.mailsink.bootstrap;

import com.github.ksokol.mailsink.entity.Mail;
import org.apache.commons.io.IOUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.typeCompatibleWith;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

    @Mock(name = "mailsinkConversionService")
    private ConversionService conversionService;

    @Captor
    private ArgumentCaptor<Class<Mail>> classCaptor;

    @Captor
    private ArgumentCaptor<InputStream> inputStreamCaptor;

    @Before
    public void setUp() throws Exception {
        int fileCount = new PathMatchingResourcePatternResolver().getResources(String.format("/%s/**", "example")).length;
        assertThat(fileCount, is(3));
    }

    @Test
    public void shouldConvertTwoFiles() throws Exception {
        exampleMails.listExampleMails();

        verify(conversionService, times(2)).convert(any(), any());
    }

    @Test
    public void shouldReturnBothMails() throws Exception {
        Mail mail1 = new Mail();
        Mail mail2 = new Mail();

        when(conversionService.convert(any(), eq(Mail.class))).thenReturn(mail1).thenReturn(mail2);

        List<Mail> expected = exampleMails.listExampleMails();

        assertThat(expected, is(asList(mail1, mail2)));
    }

    @Test
    public void shouldDetectFilesWithEmlFileExtension() throws Exception {
        when(conversionService.convert(inputStreamCaptor.capture(), classCaptor.capture())).thenReturn(any(Mail.class)).thenReturn(any(Mail.class));

        exampleMails.listExampleMails();

        assertThat(classCaptor.getAllValues().get(0), typeCompatibleWith(Mail.class));
        assertThat(classCaptor.getAllValues().get(1), typeCompatibleWith(Mail.class));
        assertThat(inputStreamCaptor.getAllValues().get(0), containsString("1"));
        assertThat(inputStreamCaptor.getAllValues().get(1), containsString("2"));
    }

    @Test
    public void shouldContinueWhenExceptionThrown() throws Exception {
        when(conversionService.convert(any(InputStream.class), eq(Mail.class))).thenThrow(new IllegalArgumentException()).thenReturn(new Mail());

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
