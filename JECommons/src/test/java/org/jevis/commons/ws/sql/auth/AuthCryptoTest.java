package org.jevis.commons.ws.sql.auth;

import org.jevis.commons.ws.sql.PasswordHash;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Tests the crypto building blocks of the token login: password hashing (Argon2id and legacy PBKDF2), TOTP
 * (RFC 6238 test vectors), Base32 and the AES-GCM encryption of TOTP secrets.
 */
public class AuthCryptoTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void argon2HashVerifiesOnlyTheRightPassword() {
        String hash = PasswordHasher.hash("correct horse: battery staple");
        assertTrue(hash.startsWith("$argon2id$v=19$m=19456,t=2,p=1$"));
        assertEquals(PasswordHasher.ALGO_ARGON2ID, PasswordHasher.detectAlgorithm(hash));
        assertTrue(PasswordHasher.verify("correct horse: battery staple", hash));
        assertFalse(PasswordHasher.verify("correct horse: battery stapl", hash));
        assertFalse(PasswordHasher.needsRehash(hash));
    }

    @Test
    public void argon2HashesAreSalted() {
        assertNotEquals(PasswordHasher.hash("secret"), PasswordHasher.hash("secret"));
    }

    @Test
    public void legacyPbkdf2HashIsVerifiedAndMarkedForRehash() throws Exception {
        String legacy = PasswordHash.createHash("jevis");
        assertEquals(PasswordHasher.ALGO_PBKDF2_SHA1, PasswordHasher.detectAlgorithm(legacy));
        assertTrue(PasswordHasher.verify("jevis", legacy));
        assertFalse(PasswordHasher.verify("wrong", legacy));
        assertTrue(PasswordHasher.needsRehash(legacy));
    }

    @Test
    public void malformedHashesAreRejected() {
        assertFalse(PasswordHasher.verify("x", null));
        assertFalse(PasswordHasher.verify("x", ""));
        assertFalse(PasswordHasher.verify("x", "********"));
        assertFalse(PasswordHasher.verify("x", "$argon2id$v=19$broken"));
    }

    /**
     * RFC 6238 appendix B, SHA1 with 8 digits.
     */
    @Test
    public void totpMatchesRfc6238TestVectors() {
        byte[] secret = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
        long[][] vectors = {
                {59L, 94287082L},
                {1111111109L, 7081804L},
                {1111111111L, 14050471L},
                {1234567890L, 89005924L},
                {2000000000L, 69279037L},
                {20000000000L, 65353130L},
        };
        for (long[] vector : vectors) {
            long step = vector[0] / TotpService.STEP_SECONDS;
            assertEquals(String.format("%08d", vector[1]), TotpService.generateCode(secret, step, 8));
        }
    }

    @Test
    public void totpAcceptsDriftWindowAndRejectsReplay() {
        byte[] secret = TotpService.generateSecret();
        long now = 1_000_000L;
        String current = TotpService.generateCode(secret, now);
        String previous = TotpService.generateCode(secret, now - 1);
        String tooOld = TotpService.generateCode(secret, now - 2);

        assertEquals(now, TotpService.verify(secret, current, 0, now));
        assertEquals(now - 1, TotpService.verify(secret, previous, 0, now));
        assertEquals(-1, TotpService.verify(secret, tooOld, 0, now));
        /* a code of an already used step is rejected */
        assertEquals(-1, TotpService.verify(secret, current, now, now));
        assertEquals(-1, TotpService.verify(secret, "12345", 0, now));
        assertEquals(-1, TotpService.verify(secret, "abcdef", 0, now));
        assertEquals(-1, TotpService.verify(secret, null, 0, now));
    }

    @Test
    public void base32RoundTripAndRfc4648Vector() {
        assertEquals("MZXW6YTBOI", AuthCrypto.base32Encode("foobar".getBytes(StandardCharsets.US_ASCII)));
        assertArrayEquals("foobar".getBytes(StandardCharsets.US_ASCII), AuthCrypto.base32Decode("mzxw 6ytb-oi======"));
        byte[] random = AuthCrypto.randomBytes(20);
        assertArrayEquals(random, AuthCrypto.base32Decode(AuthCrypto.base32Encode(random)));
    }

    @Test
    public void otpAuthUriContainsSecretAndIssuer() {
        byte[] secret = "foobar".getBytes(StandardCharsets.US_ASCII);
        String uri = TotpService.buildOtpAuthUri("JEVis", "Sys Admin", secret);
        assertEquals("otpauth://totp/JEVis%3ASys%20Admin?secret=MZXW6YTBOI&issuer=JEVis&algorithm=SHA1&digits=6&period=30", uri);
    }

    @Test
    public void tokensAreRandomAndHashed() {
        String a = AuthCrypto.randomToken(AuthService.PREFIX_ACCESS);
        String b = AuthCrypto.randomToken(AuthService.PREFIX_ACCESS);
        assertTrue(a.startsWith("jvs_"));
        assertEquals(4 + 43, a.length());
        assertNotEquals(a, b);
        assertEquals(64, AuthCrypto.sha256Hex(a).length());
        assertTrue(AuthCrypto.constantTimeEquals(a, a));
        assertFalse(AuthCrypto.constantTimeEquals(a, b));
        assertFalse(AuthCrypto.constantTimeEquals(a, null));
    }

    @Test
    public void mfaSecretEncryptionIsBoundToUserAndKeyFile() throws Exception {
        File keyFile = new File(folder.getRoot(), "etc/mfa.key");
        String oldKeyFile = AuthConfig.getMfaKeyFile();
        AuthConfig.setMfaKeyFile(keyFile.getAbsolutePath());
        try {
            byte[] secret = TotpService.generateSecret();
            byte[] encrypted = MfaSecretCipher.encrypt(42, secret);
            assertTrue(keyFile.exists());
            assertFalse(Arrays.equals(secret, Arrays.copyOfRange(encrypted, 12, 12 + secret.length)));
            assertArrayEquals(secret, MfaSecretCipher.decrypt(42, encrypted));
            try {
                MfaSecretCipher.decrypt(43, encrypted);
                fail("Secret of user 42 must not decrypt for user 43");
            } catch (IllegalStateException expected) {
                // ok
            }
        } finally {
            AuthConfig.setMfaKeyFile(oldKeyFile);
        }
    }
}
