package com.infoclinika.mssharing.web.downloader;

import java.io.UnsupportedEncodingException;

/**
 * @author Alexander Orlov
 */
public class AttachmentsDownloadHelper {

    private static final String UTF8 = "UTF-8";

    private static final byte[] UNKNOWN_BYTES = {'?'};

    /**
     * Inspired by https://github.com/nuxeo/nuxeo-common/blob/7c58a2e031b95af3d02cdf2a84dd32540ed7301d/src/main/java/org/nuxeo/common/utils/RFC2231.java
     * <p>
     * Does a simple %-escaping of the UTF-8 bytes of the value. Keep only some
     * know safe characters.
     *
     * @param buf   the buffer to which escaped chars are appended
     * @param value the value to escape
     */
    private static void percentEscape(StringBuilder buf, String value) {
        byte[] bytes;
        try {
            bytes = value.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            // cannot happen with UTF-8
            bytes = UNKNOWN_BYTES;
        }
        for (byte b : bytes) {
            if (b < '+' || b == ';' || b == ',' || b == '\\' || b > 'z') {
                buf.append('%');
                String s = Integer.toHexString(b & 0xff).toUpperCase();
                if (s.length() < 2) {
                    buf.append('0');
                }
                buf.append(s);
            } else {
                buf.append((char) b);
            }
        }
    }

    /**
     * Encodes a {@code Content-Disposition} header. For some user agents the
     * full RFC-2231 encoding won't be performed as they don't understand it.
     *
     * @param filename  the filename
     * @param userAgent the userAgent
     * @return a full string to set as value of a {@code Content-Disposition}
     * header
     */
    public static String encodeContentDisposition(String filename, String userAgent) {
        StringBuilder buf = new StringBuilder("attachment; ");
        if (userAgent == null) {
            userAgent = "";
        }
        if (userAgent.contains("Firefox") || userAgent.contains("Chrome")) {
            // proper RFC2231
            buf.append("filename*=UTF-8''");
            percentEscape(buf, filename);
        } else {
            buf.append("filename=");
            if (userAgent.contains("MSIE")) {
                // MSIE understands straight %-encoding
                percentEscape(buf, filename);
            } else {
                // Safari (maybe others) expects direct UTF-8 encoded strings
                buf.append(filename);
            }
        }
        return buf.append(';').toString();
    }
}
