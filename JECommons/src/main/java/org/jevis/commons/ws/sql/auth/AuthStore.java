package org.jevis.commons.ws.sql.auth;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Database access for the authentication tables created by migration {@code V10__Create_Auth_Tables.sql}:
 * {@code user_credential}, {@code auth_token}, {@code api_key}, {@code user_mfa} and {@code user_recovery_code}.
 *
 * <p>All timestamps are written and compared with the clock of the webservice (never SQL {@code NOW()}), so a
 * different time zone of the database server does not matter.
 */
public class AuthStore {

    public static final String TOKEN_ACCESS = "access";
    public static final String TOKEN_REFRESH = "refresh";
    public static final String TOKEN_MFA = "mfa";
    public static final String TOKEN_HANDOFF = "handoff";
    private static final Logger logger = LogManager.getLogger(AuthStore.class);
    private static final String[] TABLES = {"user_credential", "auth_token", "api_key", "user_mfa", "user_recovery_code"};
    private static final long SCHEMA_RECHECK_MS = 60_000;
    private static volatile boolean schemaAvailable = false;
    private static volatile long schemaCheckedAt = 0;
    private final Connection connection;

    public AuthStore(Connection connection) {
        this.connection = connection;
    }

    private static Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    private static Long toMillis(Timestamp ts) {
        return ts == null ? null : ts.getTime();
    }

    /* ------------------------------------------------------------------ user_credential */

    /**
     * Returns {@code true} if the auth tables exist. If the migration was not applied yet the webservice keeps
     * working with the legacy password samples and Basic auth only.
     */
    public boolean isSchemaAvailable() {
        if (schemaAvailable) return true;
        long nowMs = System.currentTimeMillis();
        if (nowMs - schemaCheckedAt < SCHEMA_RECHECK_MS) return false;
        schemaCheckedAt = nowMs;

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name IN (");
        for (int i = 0; i < TABLES.length; i++) {
            sql.append(i == 0 ? "?" : ",?");
        }
        sql.append(")");
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < TABLES.length; i++) {
                ps.setString(i + 1, TABLES[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                schemaAvailable = rs.next() && rs.getInt(1) == TABLES.length;
            }
        } catch (SQLException ex) {
            logger.error("Could not check auth tables", ex);
            schemaAvailable = false;
        }
        if (!schemaAvailable) {
            logger.error("Authentication tables are missing. Apply the database migration V10__Create_Auth_Tables.sql "
                    + "(mysqldb: mvn flyway:migrate). Token login, API keys and 2FA are disabled until then.");
        }
        return schemaAvailable;
    }

    public Credential getCredential(long userId) throws SQLException {
        String sql = "SELECT user_id, password_hash, hash_algo, failed_attempts, locked_until, must_change FROM user_credential WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Credential c = new Credential();
                c.userId = rs.getLong("user_id");
                c.passwordHash = rs.getString("password_hash");
                c.hashAlgo = rs.getString("hash_algo");
                c.failedAttempts = rs.getInt("failed_attempts");
                c.lockedUntil = rs.getTimestamp("locked_until");
                c.mustChange = rs.getBoolean("must_change");
                return c;
            }
        }
    }

    public void setPassword(long userId, String passwordHash, String algo, boolean mustChange) throws SQLException {
        String sql = "INSERT INTO user_credential (user_id, password_hash, hash_algo, password_changed, failed_attempts, locked_until, must_change) "
                + "VALUES (?,?,?,?,0,NULL,?) ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash), hash_algo=VALUES(hash_algo), "
                + "password_changed=VALUES(password_changed), failed_attempts=0, locked_until=NULL, must_change=VALUES(must_change)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, passwordHash);
            ps.setString(3, algo);
            ps.setTimestamp(4, now());
            ps.setBoolean(5, mustChange);
            ps.executeUpdate();
        }
    }

    /**
     * Replaces the hash without touching the change date, used for transparent rehashing.
     */
    public void updateHash(long userId, String passwordHash, String algo) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE user_credential SET password_hash=?, hash_algo=? WHERE user_id=?")) {
            ps.setString(1, passwordHash);
            ps.setString(2, algo);
            ps.setLong(3, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Counts a failed login and locks the account once the threshold is reached. The lock time doubles with every
     * further failure, capped at 24 hours.
     */
    public void recordFailure(long userId, int threshold, int lockMinutes) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE user_credential SET failed_attempts = failed_attempts + 1 WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
        Credential c = getCredential(userId);
        if (c != null && c.failedAttempts >= threshold) {
            int exponent = Math.min(c.failedAttempts - threshold, 10);
            long minutes = Math.min((long) lockMinutes << exponent, 24 * 60);
            try (PreparedStatement ps = connection.prepareStatement("UPDATE user_credential SET locked_until = ? WHERE user_id = ?")) {
                ps.setTimestamp(1, new Timestamp(System.currentTimeMillis() + minutes * 60_000L));
                ps.setLong(2, userId);
                ps.executeUpdate();
            }
            logger.warn("User {} is locked for {} minutes after {} failed logins", userId, minutes, c.failedAttempts);
        }
    }

    /* ------------------------------------------------------------------ auth_token */

    public void resetFailures(long userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE user_credential SET failed_attempts = 0, locked_until = NULL WHERE user_id = ? AND (failed_attempts > 0 OR locked_until IS NOT NULL)")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    public void insertToken(String tokenHash, long userId, String type, long expiresMs, String clientInfo) throws SQLException {
        String sql = "INSERT INTO auth_token (token_hash, user_id, type, created, last_used, expires, client_info, revoked) VALUES (?,?,?,?,?,?,?,0)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            Timestamp now = now();
            ps.setString(1, tokenHash);
            ps.setLong(2, userId);
            ps.setString(3, type);
            ps.setTimestamp(4, now);
            ps.setTimestamp(5, now);
            ps.setTimestamp(6, new Timestamp(expiresMs));
            ps.setString(7, clientInfo == null ? null : (clientInfo.length() > 255 ? clientInfo.substring(0, 255) : clientInfo));
            ps.executeUpdate();
        }
    }

    public TokenRow findToken(String tokenHash) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT token_hash, user_id, type, created, expires, revoked FROM auth_token WHERE token_hash = ?")) {
            ps.setString(1, tokenHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                TokenRow t = new TokenRow();
                t.tokenHash = rs.getString("token_hash");
                t.userId = rs.getLong("user_id");
                t.type = rs.getString("type");
                t.created = rs.getTimestamp("created").getTime();
                t.expires = rs.getTimestamp("expires").getTime();
                t.revoked = rs.getBoolean("revoked");
                return t;
            }
        }
    }

    /**
     * Updates the last use and moves the expiry date (sliding expiration).
     */
    public void touchToken(String tokenHash, long newExpiresMs) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE auth_token SET last_used = ?, expires = ? WHERE token_hash = ? AND revoked = 0")) {
            ps.setTimestamp(1, now());
            ps.setTimestamp(2, new Timestamp(newExpiresMs));
            ps.setString(3, tokenHash);
            ps.executeUpdate();
        }
    }

    /**
     * Atomically marks a single-use token as used.
     *
     * @return {@code true} if the token was valid and is now consumed, {@code false} if it was unknown, expired,
     * of another type or already used
     */
    public boolean consumeToken(String tokenHash, String type) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE auth_token SET revoked = 1, last_used = ? WHERE token_hash = ? AND type = ? AND revoked = 0 AND expires > ?")) {
            Timestamp now = now();
            ps.setTimestamp(1, now);
            ps.setString(2, tokenHash);
            ps.setString(3, type);
            ps.setTimestamp(4, now);
            return ps.executeUpdate() == 1;
        }
    }

    public void revokeToken(String tokenHash) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE auth_token SET revoked = 1 WHERE token_hash = ?")) {
            ps.setString(1, tokenHash);
            ps.executeUpdate();
        }
    }

    public void revokeAllTokens(long userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE auth_token SET revoked = 1 WHERE user_id = ? AND revoked = 0")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    /* ------------------------------------------------------------------ api_key */

    public int deleteExpiredTokens() throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM auth_token WHERE expires < ? OR (revoked = 1 AND last_used < ?)")) {
            long nowMs = System.currentTimeMillis();
            ps.setTimestamp(1, new Timestamp(nowMs));
            ps.setTimestamp(2, new Timestamp(nowMs - 24L * 3600_000L));
            return ps.executeUpdate();
        }
    }

    public void insertApiKey(String keyId, String secretHash, long userId, String name, Long createdBy, Long expiresMs) throws SQLException {
        String sql = "INSERT INTO api_key (key_id, secret_hash, user_id, name, created, created_by, expires, revoked) VALUES (?,?,?,?,?,?,?,0)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, keyId);
            ps.setString(2, secretHash);
            ps.setLong(3, userId);
            ps.setString(4, name);
            ps.setTimestamp(5, now());
            if (createdBy == null) ps.setNull(6, java.sql.Types.BIGINT);
            else ps.setLong(6, createdBy);
            ps.setTimestamp(7, expiresMs == null ? null : new Timestamp(expiresMs));
            ps.executeUpdate();
        }
    }

    public ApiKeyRow findApiKey(String keyId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM api_key WHERE key_id = ?")) {
            ps.setString(1, keyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? readApiKey(rs) : null;
            }
        }
    }

    public List<ApiKeyRow> listApiKeys() throws SQLException {
        List<ApiKeyRow> keys = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM api_key ORDER BY created");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                keys.add(readApiKey(rs));
            }
        }
        return keys;
    }

    public boolean revokeApiKey(String keyId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE api_key SET revoked = 1 WHERE key_id = ?")) {
            ps.setString(1, keyId);
            return ps.executeUpdate() == 1;
        }
    }

    public void touchApiKey(String keyId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE api_key SET last_used = ? WHERE key_id = ?")) {
            ps.setTimestamp(1, now());
            ps.setString(2, keyId);
            ps.executeUpdate();
        }
    }

    private ApiKeyRow readApiKey(ResultSet rs) throws SQLException {
        ApiKeyRow k = new ApiKeyRow();
        k.keyId = rs.getString("key_id");
        k.secretHash = rs.getString("secret_hash");
        k.userId = rs.getLong("user_id");
        k.name = rs.getString("name");
        k.created = toMillis(rs.getTimestamp("created"));
        long createdBy = rs.getLong("created_by");
        k.createdBy = rs.wasNull() ? null : createdBy;
        k.expires = toMillis(rs.getTimestamp("expires"));
        k.lastUsed = toMillis(rs.getTimestamp("last_used"));
        k.revoked = rs.getBoolean("revoked");
        return k;
    }

    /* ------------------------------------------------------------------ user_mfa / user_recovery_code */

    public MfaRow getTotp(long userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT user_id, secret_enc, confirmed, last_used_step FROM user_mfa WHERE user_id = ? AND type = 'totp'")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                MfaRow m = new MfaRow();
                m.userId = rs.getLong("user_id");
                m.secretEnc = rs.getBytes("secret_enc");
                m.confirmed = rs.getBoolean("confirmed");
                m.lastUsedStep = rs.getLong("last_used_step");
                return m;
            }
        }
    }

    /**
     * Stores a new, not yet confirmed TOTP secret. An existing confirmed secret is not replaced.
     *
     * @return {@code false} if the user already has confirmed 2FA
     */
    public boolean saveUnconfirmedTotp(long userId, byte[] secretEnc) throws SQLException {
        try (PreparedStatement del = connection.prepareStatement("DELETE FROM user_mfa WHERE user_id = ? AND type = 'totp' AND confirmed = 0")) {
            del.setLong(1, userId);
            del.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement("INSERT IGNORE INTO user_mfa (user_id, type, secret_enc, confirmed, created, last_used_step) VALUES (?, 'totp', ?, 0, ?, 0)")) {
            ps.setLong(1, userId);
            ps.setBytes(2, secretEnc);
            ps.setTimestamp(3, now());
            return ps.executeUpdate() == 1;
        }
    }

    public void confirmTotp(long userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE user_mfa SET confirmed = 1 WHERE user_id = ? AND type = 'totp'")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    /**
     * Atomically stores the time step of a used TOTP code, which prevents the same code from being used twice.
     *
     * @return {@code false} if a code of this or a later time step was already used
     */
    public boolean advanceTotpStep(long userId, long step) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE user_mfa SET last_used_step = ? WHERE user_id = ? AND type = 'totp' AND last_used_step < ?")) {
            ps.setLong(1, step);
            ps.setLong(2, userId);
            ps.setLong(3, step);
            return ps.executeUpdate() == 1;
        }
    }

    public void deleteMfa(long userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM user_mfa WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM user_recovery_code WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
    }

    public void replaceRecoveryCodes(long userId, List<String> codeHashes) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM user_recovery_code WHERE user_id = ?")) {
            ps.setLong(1, userId);
            ps.executeUpdate();
        }
        try (PreparedStatement ps = connection.prepareStatement("INSERT INTO user_recovery_code (user_id, code_hash, used) VALUES (?, ?, NULL)")) {
            for (String hash : codeHashes) {
                ps.setLong(1, userId);
                ps.setString(2, hash);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Atomically marks a recovery code as used.
     *
     * @return {@code true} if the code existed and was not used before
     */
    public boolean useRecoveryCode(long userId, String codeHash) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("UPDATE user_recovery_code SET used = ? WHERE user_id = ? AND code_hash = ? AND used IS NULL")) {
            ps.setTimestamp(1, now());
            ps.setLong(2, userId);
            ps.setString(3, codeHash);
            return ps.executeUpdate() == 1;
        }
    }

    public int countUnusedRecoveryCodes(long userId) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM user_recovery_code WHERE user_id = ? AND used IS NULL")) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /* ------------------------------------------------------------------ rows */

    public static class Credential {
        public long userId;
        public String passwordHash;
        public String hashAlgo;
        public int failedAttempts;
        public Timestamp lockedUntil;
        public boolean mustChange;

        public boolean isLocked() {
            return lockedUntil != null && lockedUntil.getTime() > System.currentTimeMillis();
        }
    }

    public static class TokenRow {
        public String tokenHash;
        public long userId;
        public String type;
        public long created;
        public long expires;
        public boolean revoked;

        public boolean isValid(String expectedType) {
            return !revoked && expectedType.equals(type) && expires > System.currentTimeMillis();
        }
    }

    public static class ApiKeyRow {
        public String keyId;
        public String secretHash;
        public long userId;
        public String name;
        public Long created;
        public Long createdBy;
        public Long expires;
        public Long lastUsed;
        public boolean revoked;
    }

    public static class MfaRow {
        public long userId;
        public byte[] secretEnc;
        public boolean confirmed;
        public long lastUsedStep;
    }
}
