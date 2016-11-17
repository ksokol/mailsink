package com.github.ksokol.mailsink.mime4j;

import org.apache.james.mime4j.field.contentdisposition.parser.ContentDispositionParser;

import javax.mail.internet.MimeUtility;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Encoder for <a href="http://tools.ietf.org/html/rfc2231">RFC2231</a> encoded parameters
 *
 * RFC2231 string are encoded as
 *
 *    charset'language'encoded-text
 *
 * and
 *
 *    encoded-text = *(char / hexchar)
 *
 * where
 *
 *    char is any ASCII character in the range 33-126, EXCEPT
 *    the characters "%" and " ".
 *
 *    hexchar is an ASCII "%" followed by two upper case
 *    hexadecimal digits.
 *
 *    TODO remove me after https://issues.apache.org/jira/browse/MIME4J-109 has been implemented
 */
final class RFC2231Decoder {

    private final Set<String> multisegmentNames = new HashSet<>();
    private final Map<String, String> parameters = new HashMap<>();

    /**
     * A map containing the segments for all not-yet-processed multi-segment
     * parameters. The map is indexed by "name*seg". The value object is either
     * a String or a Value object. The Value object is not decoded during the
     * initial parse because the segments may appear in any order and until the
     * first segment appears we don't know what charset to use to decode any
     * encoded segments. The segments are decoded in order in the
     * combineMultisegmentNames method.
     */
    private final Map<String, Object> segmentList = new HashMap<>();

    public String parse(String contentDisposition) {
        ContentDispositionParser parser = new ContentDispositionParser(new StringReader(contentDisposition));
        try {
            parser.parseAll();
        } catch (Exception exception) {
            throw new IllegalArgumentException(String.format("could not parse content disposition '%s'", contentDisposition), exception);
        }

        final String dispositionType = parser.getDispositionType();

        if (dispositionType != null) {
            List<String> paramNames = parser.getParamNames();
            List<String> paramValues = parser.getParamValues();

            if (paramNames != null && paramValues != null) {
                final int len = Math.min(paramNames.size(), paramValues.size());
                for (int i = 0; i < len; i++) {
                    String paramName = paramNames.get(i).toLowerCase(Locale.US);
                    String paramValue = paramValues.get(i);
                    putParameter(paramName, paramValue);
                }
                combineMultisegmentParameters();
            }
        }

        return parameters.get("filename");
    }

    /**
     * Decode RFC2231 parameter value with charset
     */
    private static RFC2231Value decodeRFC2231Value(String rawValue) {
        String value = rawValue;
        String charset;

        RFC2231Value v = new RFC2231Value();
        v.encodedValue = value;
        v.value = value; // in case we fail to decode it

        int charsetDelimiter = value.indexOf('\'');
        if (charsetDelimiter <= 0) {
            return v; // not encoded correctly? return as is.
        }

        charset = value.substring(0, charsetDelimiter);
        int langDelimiter = value.indexOf('\'', charsetDelimiter + 1);
        if (langDelimiter < 0) {
            return v; // not encoded correctly? return as is.
        }

        value = value.substring(langDelimiter + 1);
        v.charset = charset;
        v.value = decodeRFC2231Bytes(value, charset);
        return v;
    }

    /**
     * Decode RFC2231 parameter value without charset
     */
    private static String decodeRFC2231Value(String value, String charset) {
        return decodeRFC2231Bytes(value, charset);
    }

    /**
     * Decode the encoded bytes in RFC2231 value using the specified charset.
     */
    private static String decodeRFC2231Bytes(String value, final String charset) {
		/*
		 * Decode the ASCII characters in value into an array of bytes, and then
		 * convert the bytes to a String using the specified charset. We'll
		 * never need more bytes than encoded characters, so use that to size
		 * the array.
		 */
        byte[] bytes = new byte[value.length()];
        int idx;
        int bytesIdx;

        for (idx = 0, bytesIdx = 0; idx < value.length(); idx++) {
            char c = value.charAt(idx);
            if (c == '%') {
                String hex = value.substring(idx + 1, idx + 3);
                c = (char) Integer.parseInt(hex, 16);
                idx += 2;
            }
            bytes[bytesIdx++] = (byte) c;
        }
        try {
            return new String(bytes, 0, bytesIdx, MimeUtility.javaCharset(charset));
        } catch (UnsupportedEncodingException exception) {
            throw new IllegalArgumentException(String.format(" unsupported encoding: '%s'", exception.getMessage()), exception);
        }
    }

    /**
     * If the name is an encoded or multi-segment name (or both) handle it
     * appropriately, storing the appropriate String or Value object.
     * Multi-segment names are stored in the main parameter list as an emtpy
     * string as a placeholder, replaced later in combineMultisegmentNames with
     * a MultiValue object. This causes all pieces of the multi-segment
     * parameter to appear in the position of the first seen segment of the
     * parameter.
     */
    private void putParameter(String name, String value) {
        String key = name;
        int star = key.indexOf('*');
        if (star < 0) {
            // single parameter, unencoded value
            parameters.put(key, value);
        } else if (star == key.length() - 1) {
            // single parameter, encoded value
            key = key.substring(0, star);
            RFC2231Value v = decodeRFC2231Value(value);
            parameters.put(key, v.value);
        } else {
            // multiple segments
            String paramName = key.substring(0, star);
            multisegmentNames.add(paramName);
            parameters.put(paramName, "");

            if (key.endsWith("*")) {
                // encoded value
                RFC2231Value valObject = new RFC2231Value();
                valObject.encodedValue = value;
                valObject.value = value; // default; decoded later

                String segmentName = key.substring(0, key.length() - 1);
                segmentList.put(segmentName, valObject);
            } else {
                // plain value
                segmentList.put(key, value);
            }
        }
    }

    /**
     * Iterate through the saved set of names of multi-segment parameters, for
     * each parameter find all segments stored in the slist map, decode each
     * segment as needed, combine the segments together into a single decoded
     * value.
     */
    private void combineMultisegmentParameters() {
        for(String name : multisegmentNames) {
            StringBuilder paramValue = new StringBuilder();
            String charset = null;
            String segmentName;
            String segmentValue;

            // find and decode each segment
            int segment;
            for (segment = 0;; segment++) {
                segmentName = name + "*" + segment;
                Object v = segmentList.get(segmentName);

                if (v == null) // out of segments
                    break;

                if (v instanceof RFC2231Value) {
                    String encodedValue = ((RFC2231Value) v).encodedValue;

                    if (segment == 0) {
                        // the first segment specifies charset for all other encoded segments
                        RFC2231Value vnew = decodeRFC2231Value(encodedValue);
                        charset = vnew.charset;
                        segmentValue = vnew.value;
                    } else {
                        segmentValue = decodeRFC2231Value(encodedValue, charset);
                    }
                } else {
                    segmentValue = (String) v;
                }

                paramValue.append(segmentValue);
            }

            parameters.put(name, paramValue.toString());
        }
    }

    /**
     * A struct to hold an encoded value. A parsed encoded value is stored as
     * both the decoded value and the original encoded value (so that toString
     * will produce the same result). An encoded value that is set explicitly is
     * stored as the original value and the encoded value, to ensure that get
     * will return the same value that was set.
     */
    private static class RFC2231Value {
        public String value;
        public String charset;
        public String encodedValue;
    }
}
