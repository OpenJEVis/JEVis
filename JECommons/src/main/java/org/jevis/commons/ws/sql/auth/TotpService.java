package org.jevis.commons.ws.sql.auth;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

/**
 * Time-based one-time passwords according to RFC 6238 (HMAC-SHA1, 30 second steps, 6 digits), compatible with
 * the common authenticator apps.
 */
public final class TotpService {

    public static final int SECRET_LENGTH = 20;
    public static final int STEP_SECONDS = 30;
    public static final int DIGITS = 6;
    /**
     * Accepted clock drift in steps before and after the current one.
     */
    public static final int WINDOW = 1;
    private static final int[] POW10 = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};

    private TotpService() {
    }

    public static byte[] generateSecret() {
        return AuthCrypto.randomBytes(SECRET_LENGTH);
    }

    public static long currentStep() {
        return System.currentTimeMillis() / 1000L / STEP_SECONDS;
    }

    /**
     * Calculates the code for a time step (RFC 4226 dynamic truncation).
     */
    public static String generateCode(byte[] secret, long step) {
        return generateCode(secret, step, DIGITS);
    }

    static String generateCode(byte[] secret, long step, int digits) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(secret, "HmacSHA1"));
            byte[] hash = mac.doFinal(ByteBuffer.allocate(8).putLong(step).array());
            int offset = hash[hash.length - 1] & 0x0F;
            int binary = ((hash[offset] & 0x7F) << 24)
                    | ((hash[offset + 1] & 0xFF) << 16)
                    | ((hash[offset + 2] & 0xFF) << 8)
                    | (hash[offset + 3] & 0xFF);
            StringBuilder code = new StringBuilder(Integer.toString(binary % POW10[digits]));
            while (code.length() < digits) {
                code.insert(0, '0');
            }
            return code.toString();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HmacSHA1 not available", e);
        }
    }

    /**
     * Checks a code against the current time step and the accepted drift window.
     *
     * @param lastUsedStep the step of the last accepted code, codes of this or earlier steps are rejected
     * @return the matching time step, or {@code -1} if the code is not valid
     */
    public static long verify(byte[] secret, String code, long lastUsedStep, long currentStep) {
        if (code == null) return -1;
        String clean = code.replace(" ", "").trim();
        if (clean.length() != DIGITS) return -1;
        for (int i = 0; i < clean.length(); i++) {
            if (!Character.isDigit(clean.charAt(i))) return -1;
        }
        long match = -1;
        // check all steps of the window to keep the timing independent of the matching step
        for (long step = currentStep - WINDOW; step <= currentStep + WINDOW; step++) {
            if (step > lastUsedStep && AuthCrypto.constantTimeEquals(generateCode(secret, step), clean)) {
                match = step;
            }
        }
        return match;
    }

    /**
     * Builds the {@code otpauth://} URI that authenticator apps read from the QR code.
     */
    public static String buildOtpAuthUri(String issuer, String accountName, byte[] secret) {
        try {
            String label = URLEncoder.encode(issuer + ":" + accountName, "UTF-8").replace("+", "%20");
            return "otpauth://totp/" + label
                    + "?secret=" + AuthCrypto.base32Encode(secret)
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20")
                    + "&algorithm=SHA1&digits=" + DIGITS + "&period=" + STEP_SECONDS;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
