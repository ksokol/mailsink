package com.github.ksokol.mailsink.mime4j;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Kamill Sokol
 */
public class RFC2231DecoderTest {

    private RFC2231Decoder decoder;
    private String decoded;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        decoder = new RFC2231Decoder();
    }

    @Test
    public void shouldParseFilenameFromSimpleParameter() throws Exception {
        given("attachment; filename=\"example.pdf\"");

        assertThat(decoded, is("example.pdf"));
    }

    @Test
    public void shouldReturnFilenameAsIsWhenCharsetOrLanguageInformationIsMissingInSingleParameter() throws Exception {
        given("attachment; filename*=%75");

        assertThat(decoded, is("%75"));
    }

    @Test
    public void shouldReturnFilenameAsIsWhenCharsetOrLanguageInformationIsMissingInMultiplpeParameter() throws Exception {
        given("attachment; filename*0=%75%; filename*1=%2E%");

        assertThat(decoded, is("%75%%2E%"));
    }

    @Test
    public void shouldReturnDecodedFilenameWhenCharsetIsAvailable() throws Exception {
        given("attachment; filename*=\"ISO-8859-15''%75%6D%6C%61%75%74%20%E4%2E%70%6E%67\"");

        assertThat(decoded, is("umlaut ä.png"));
    }

    @Test
    public void shouldReturnDecodedFilenameWhenCharsetAndLanguageInformationIsAvailable() throws Exception {
        given("attachment; filename*=\"ISO-8859-15'de-de'%75%6D%6C%61%75%74%20%E4%2E%70%6E%67\"");

        assertThat(decoded, is("umlaut ä.png"));
    }

    @Test
    public void shouldReturnDecodedFilenameWhenCharsetAndCountryInformationIsAvailable() throws Exception {
        given("attachment; filename*=\"ISO-8859-15'de'%75%6D%6C%61%75%74%20%E4%2E%70%6E%67\"");

        assertThat(decoded, is("umlaut ä.png"));
    }

    @Test
    public void shouldReturnFilenameWithApostrophe() throws Exception {
        given("attachment; filename*=\"filename with '.png\"");

        assertThat(decoded, is("filename with '.png"));
    }

    @Test
    public void shouldParseFilenameSplitIntoMultipleParameters() throws Exception {
        given("attachment; " +
                "filename*0*=ISO-8859-15''%75%6D%6C%61%75%74%20%E4%20%76%65%65%65%65%65%65; " +
                "filename*1*=%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65; " +
                "filename*2*=%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65%65; " +
                "filename*3*=%65%65%65%65%65%65%65%65%65%65%65%65%65%72%79%20%6C%6F%6E%67; " +
                "filename*4*=%2E%70%6E%67");

        assertThat(decoded, is("umlaut ä veeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeery long.png"));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenContentDispositionIsInvalid() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("could not parse content disposition 'attachment; invalid content dispotition'");

        given("attachment; invalid content dispotition");
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenCharsetIsInvalid() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("unsupported encoding: 'ISO-1'");

        given("attachment; filename*=\"ISO-1''irrelevant\"");
    }

    private void given(String value) {
        decoded = decoder.parse(value);
    }
}
