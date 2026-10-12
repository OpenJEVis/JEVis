package org.jevis.commons.ws.sql.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.EnumSet;

/**
 * Encrypts TOTP secrets with AES-256-GCM.
 *
 * <p>The key is stored in a file outside of the database ({@link AuthConfig#getMfaKeyFile()}), so a database dump
 * alone does not reveal the secrets. The file is created on first use. The user id is bound to the ciphertext as
 * additional authenticated data, so encrypted secrets can not be copied between users.
 *
 * <p>AES-256 needs the unlimited crypto policy, which is the default since Java 8u161.
 */
public final class MfaSecretCipher {

    private static final Logger logger = LogManager.getLogger(MfaSecretCipher.class);
    private static final int KEY_LENGTH = 32;
    private static final int IV_LENGTH = 12;
    private static final int TAG_BITS = 128;
    private static volatile SecretKeySpec key;

    private MfaSecretCipher() {
    }

    public static byte[] encrypt(long userId, byte[] secret) {
        try {
            byte[] iv = AuthCrypto.randomBytes(IV_LENGTH);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, getKey(), new GCMParameterSpec(TAG_BITS, iv));
            cipher.updateAAD(aad(userId));
            byte[] encrypted = cipher.doFinal(secret);
            return ByteBuffer.allocate(iv.length + encrypted.length).put(iv).put(encrypted).array();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not encrypt 2FA secret", e);
        }
    }

    public static byte[] decrypt(long userId, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, getKey(), new GCMParameterSpec(TAG_BITS, data, 0, IV_LENGTH));
            cipher.updateAAD(aad(userId));
            return cipher.doFinal(data, IV_LENGTH, data.length - IV_LENGTH);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Could not decrypt 2FA secret, wrong key file?", e);
        }
    }

    private static byte[] aad(long userId) {
        return ("jevis-totp:" + userId).getBytes(StandardCharsets.UTF_8);
    }

    private static SecretKeySpec getKey() throws GeneralSecurityException {
        SecretKeySpec k = key;
        if (k == null) {
            synchronized (MfaSecretCipher.class) {
                k = key;
                if (k == null) {
                    if (Cipher.getMaxAllowedKeyLength("AES") < 256) {
                        throw new GeneralSecurityException("AES-256 is not allowed by this Java runtime. Use Java 8u161 or newer.");
                    }
                    k = new SecretKeySpec(loadOrCreateKey(new File(AuthConfig.getMfaKeyFile())), "AES");
                    key = k;
                }
            }
        }
        return k;
    }

    private static byte[] loadOrCreateKey(File file) throws GeneralSecurityException {
        try {
            if (file.exists()) {
                byte[] decoded = Base64.getDecoder().decode(new String(Files.readAllBytes(file.toPath()), StandardCharsets.US_ASCII).trim());
                if (decoded.length != KEY_LENGTH) {
                    throw new GeneralSecurityException("Invalid 2FA key file: " + file.getAbsolutePath());
                }
                return decoded;
            }
            logger.warn("Creating new 2FA key file: {}. Keep it together with your database backups.", file.getAbsolutePath());
            File parent = file.getAbsoluteFile().getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create directory " + parent);
            }
            byte[] newKey = AuthCrypto.randomBytes(KEY_LENGTH);
            Files.write(file.toPath(), Base64.getEncoder().encode(newKey));
            try {
                Files.setPosixFilePermissions(file.toPath(), EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
            } catch (UnsupportedOperationException ex) {
                logger.warn("Could not restrict permissions of {} (no POSIX file system)", file.getAbsolutePath());
            }
            return newKey;
        } catch (IOException e) {
            throw new GeneralSecurityException("Could not read or create 2FA key file: " + file.getAbsolutePath(), e);
        }
    }
}
