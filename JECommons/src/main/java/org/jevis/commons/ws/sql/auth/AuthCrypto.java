package org.jevis.commons.ws.sql.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Small crypto helpers shared by the authentication code (Java 8 compatible).
 *
 * <p>Tokens and API keys are 256 bit random values. Because of their entropy they can be stored as plain
 * SHA-256 hashes, a slow password hash is only needed for human chosen passwords.
 */
public final class AuthCrypto {

    /**
     * Do not use {@code SecureRandom.getInstanceStrong()}, on Linux with Java 8 it blocks on /dev/random.
     */
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] HEX = "0123456789abcdef".toCharArray();
    private static final char[] BASE32 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();

    private AuthCrypto() {
    }

    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    /**
     * Creates a new random token with 256 bit entropy, e.g. {@code jvs_3q2+...}.
     *
     * @param prefix token type prefix, see {@link AuthService}
     */
    public static String randomToken(String prefix) {
        return prefix + base64Url(randomBytes(32));
    }

    public static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /**
     * Compares two strings in constant time (with respect to their content).
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHex(byte[] bytes) {
        char[] out = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            out[i * 2] = HEX[v >>> 4];
            out[i * 2 + 1] = HEX[v & 0x0F];
        }
        return new String(out);
    }

    /**
     * RFC 4648 Base32 without padding, as used by authenticator apps for TOTP secrets.
     */
    public static String base32Encode(byte[] data) {
        StringBuilder sb = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                sb.append(BASE32[(buffer >> (bitsLeft - 5)) & 0x1F]);
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32[(buffer << (5 - bitsLeft)) & 0x1F]);
        }
        return sb.toString();
    }

    /**
     * Decodes RFC 4648 Base32, ignoring case, spaces, dashes and padding.
     *
     * @throws IllegalArgumentException if the input contains invalid characters
     */
    public static byte[] base32Decode(String encoded) {
        String clean = encoded.replace("=", "").replace(" ", "").replace("-", "").toUpperCase(java.util.Locale.ROOT);
        byte[] out = new byte[clean.length() * 5 / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int index = 0;
        for (char c : clean.toCharArray()) {
            int value;
            if (c >= 'A' && c <= 'Z') {
                value = c - 'A';
            } else if (c >= '2' && c <= '7') {
                value = c - '2' + 26;
            } else {
                throw new IllegalArgumentException("Invalid base32 character: " + c);
            }
            buffer = (buffer << 5) | value;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                out[index++] = (byte) (buffer >> (bitsLeft - 8));
                bitsLeft -= 8;
            }
        }
        return out;
    }
}
