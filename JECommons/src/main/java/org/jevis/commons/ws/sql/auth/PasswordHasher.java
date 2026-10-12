package org.jevis.commons.ws.sql.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.jevis.commons.ws.sql.PasswordHash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.Semaphore;

/**
 * Hashes and verifies user passwords.
 *
 * <p>New hashes use Argon2id (OWASP recommended parameters m=19 MiB, t=2, p=1) in the PHC string format
 * {@code $argon2id$v=19$m=19456,t=2,p=1$<salt>$<hash>}. Legacy hashes ({@code iterations:salt:hash},
 * PBKDF2-HMAC-SHA1) created by {@link PasswordHash} are still verified and should be replaced on the next
 * successful login, see {@link #needsRehash(String, String)}.
 *
 * <p>Argon2 is memory hard, the number of parallel hash computations is limited to keep the memory use of the
 * webservice bounded.
 */
public final class PasswordHasher {

    public static final String ALGO_ARGON2ID = "argon2id";
    public static final String ALGO_PBKDF2_SHA1 = "pbkdf2-sha1";

    private static final Logger logger = LogManager.getLogger(PasswordHasher.class);
    private static final int MEMORY_KB = 19456;
    private static final int ITERATIONS = 2;
    private static final int PARALLELISM = 1;
    private static final int SALT_LENGTH = 16;
    private static final int HASH_LENGTH = 32;
    private static final String PREFIX = "$argon2id$v=19$";
    private static final Semaphore PARALLEL_HASHES = new Semaphore(4, true);
    private static final Base64.Encoder B64_ENCODER = Base64.getEncoder().withoutPadding();
    private static final Base64.Decoder B64_DECODER = Base64.getDecoder();

    private PasswordHasher() {
    }

    /**
     * Detects the algorithm of a stored hash.
     */
    public static String detectAlgorithm(String storedHash) {
        if (storedHash != null && storedHash.startsWith("$argon2id$")) {
            return ALGO_ARGON2ID;
        }
        return ALGO_PBKDF2_SHA1;
    }

    /**
     * Creates a new Argon2id hash for the given password.
     */
    public static String hash(String password) {
        byte[] salt = AuthCrypto.randomBytes(SALT_LENGTH);
        byte[] hash = argon2(password, salt, MEMORY_KB, ITERATIONS, PARALLELISM, HASH_LENGTH);
        return PREFIX + "m=" + MEMORY_KB + ",t=" + ITERATIONS + ",p=" + PARALLELISM
                + "$" + B64_ENCODER.encodeToString(salt) + "$" + B64_ENCODER.encodeToString(hash);
    }

    /**
     * Verifies a password against a stored hash of any supported algorithm.
     *
     * @return {@code false} for wrong passwords and for malformed hashes
     */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isEmpty()) return false;
        try {
            if (ALGO_ARGON2ID.equals(detectAlgorithm(storedHash))) {
                return verifyArgon2(password, storedHash);
            }
            return PasswordHash.validatePassword(password, storedHash);
        } catch (Exception ex) {
            logger.warn("Could not verify password hash: {}", ex.getMessage());
            return false;
        }
    }

    /**
     * Returns {@code true} if the stored hash uses an outdated algorithm or parameters.
     */
    public static boolean needsRehash(String storedHash) {
        if (!ALGO_ARGON2ID.equals(detectAlgorithm(storedHash))) return true;
        return !storedHash.startsWith(PREFIX + "m=" + MEMORY_KB + ",t=" + ITERATIONS + ",p=" + PARALLELISM + "$");
    }

    private static boolean verifyArgon2(String password, String storedHash) {
        // $argon2id$v=19$m=19456,t=2,p=1$salt$hash
        String[] parts = storedHash.split("\\$");
        if (parts.length != 6 || !"v=19".equals(parts[2])) {
            throw new IllegalArgumentException("Unsupported argon2 hash format");
        }
        int memory = -1;
        int iterations = -1;
        int parallelism = -1;
        for (String param : parts[3].split(",")) {
            String[] kv = param.split("=", 2);
            if (kv.length != 2) continue;
            int value = Integer.parseInt(kv[1]);
            switch (kv[0]) {
                case "m":
                    memory = value;
                    break;
                case "t":
                    iterations = value;
                    break;
                case "p":
                    parallelism = value;
                    break;
                default:
                    break;
            }
        }
        if (memory <= 0 || iterations <= 0 || parallelism <= 0) {
            throw new IllegalArgumentException("Invalid argon2 parameters");
        }
        byte[] salt = B64_DECODER.decode(parts[4]);
        byte[] expected = B64_DECODER.decode(parts[5]);
        byte[] actual = argon2(password, salt, memory, iterations, parallelism, expected.length);
        return MessageDigest.isEqual(expected, actual);
    }

    private static byte[] argon2(String password, byte[] salt, int memoryKb, int iterations, int parallelism, int length) {
        Argon2Parameters params = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(memoryKb)
                .withIterations(iterations)
                .withParallelism(parallelism)
                .withSalt(salt)
                .build();
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(params);
        byte[] out = new byte[length];
        byte[] pw = password.getBytes(StandardCharsets.UTF_8);
        try {
            PARALLEL_HASHES.acquireUninterruptibly();
            generator.generateBytes(pw, out);
        } finally {
            PARALLEL_HASHES.release();
            Arrays.fill(pw, (byte) 0);
        }
        return out;
    }
}
