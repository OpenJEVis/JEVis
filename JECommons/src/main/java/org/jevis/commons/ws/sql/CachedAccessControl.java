package org.jevis.commons.ws.sql;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Application-scoped singleton that caches user credentials and group
 * membership relationships to avoid repeated database round-trips on every
 * request.
 *
 * <p>The cache is invalidated lazily: a flag ({@link #needUpdate}) is set
 * when a relevant change is detected (e.g., membership relationship created or
 * a user's password updated). The next request then calls {@link #updateCache}
 * before processing begins.
 *
 * <p>Sessions are cached using a Guava {@link Cache} with a configurable TTL
 * (see {@link Config#sessiontimeout}) that expires both after write and after
 * access.
 */
public class CachedAccessControl {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CachedAccessControl.class);
    private static CachedAccessControl fastUserManager = null;
    /**
     * Per-user group memberships, keyed by user ID.
     */
    private final Map<Long, List<JsonRelationship>> groupMemberships = new ConcurrentHashMap();
    /** Flag set when a structural change (membership/password) requires a cache refresh. */
    private final AtomicBoolean needUpdate = new AtomicBoolean(false);
    /** All known users, keyed by lowercase account name. */
    private Map<String, JEVisUserSQL> users;
    /** Active HTTP sessions. Entries expire after write and after access. */
    private final Cache<String, Session> sessions;
    private SQLDataSource ds;

    /**
     * Creates a new {@code CachedAccessControl} instance with a session cache
     * configured according to {@link Config#sessiontimeout}.
     *
     * @throws Exception if the Guava cache builder fails
     */
    public CachedAccessControl() throws Exception {
        sessions = CacheBuilder.newBuilder()
                .expireAfterWrite(Config.sessiontimeout, TimeUnit.MINUTES)
                .expireAfterAccess(Config.sessiontimeout, TimeUnit.MINUTES)
                .build();
    }

    /**
     * Returns the shared singleton instance, initializing and populating it
     * on the first call or refreshing it if the update flag is set.
     *
     * @param ds         the current request's data source (used for refresh queries)
     * @param supportSSO {@code true} if SSO token validation is enabled
     * @return the singleton instance
     * @throws Exception if cache initialization or refresh fails
     */
    public static CachedAccessControl getInstance(SQLDataSource ds, boolean supportSSO) throws Exception {
        if (fastUserManager != null) {
            if (fastUserManager.needUpdate()) fastUserManager.updateCache(ds);
            return fastUserManager;
        } else {
            fastUserManager = new CachedAccessControl();
            fastUserManager.updateCache(ds);
            fastUserManager.ds = ds;
            return fastUserManager;
        }


    }


    /**
     * Reloads only the user map from the database.
     *
     * @param dataSource the data source to query
     * @throws JEVisException if the query fails
     */
    public synchronized void updateUser(SQLDataSource dataSource) throws JEVisException {
        users = dataSource.getLoginTable().getAllUser();
    }

    /**
     * Fully refreshes the user map and group-membership cache from the database.
     * Clears the {@link #needUpdate} flag on completion.
     *
     * @param dataSource the data source to query
     * @throws Exception if the query fails
     */
    public synchronized void updateCache(SQLDataSource dataSource) throws Exception {
        logger.error("Update Access Control Cache");
        needUpdate.set(false);
        users = dataSource.getLoginTable().getAllUser();
        groupMemberships.clear();

        users.forEach((s, jeVisUserNew) -> {
            groupMemberships.put(jeVisUserNew.getUserID(), new ArrayList<>());
        });

        List<JsonRelationship> relationships = dataSource.getRelationshipTable().getAllMemberships();
        logger.debug("Group Memberships: {}", relationships.size());
        for (JsonRelationship rel : relationships) {
            List<JsonRelationship> userRelList = groupMemberships.getOrDefault(rel.getFrom(), new ArrayList<>());
            userRelList.add(rel);
            groupMemberships.put(rel.getFrom(), userRelList);
        }
    }

    /**
     * Prints the current session and user cache to stdout.
     * Intended for debugging only.
     */
    public void printCache() {
        System.out.println("Sessions: ");
        sessions.asMap().forEach((s, session) -> {
            System.out.println(s + ":" + session.getDisplayName());
        });
        System.out.println();

        System.out.println("User:");
        users.forEach((s, jeVisUserNew) -> {
            if (!s.equals("jsc@ecocharge")) return;
            System.out.println("--------------------");
            System.out.println(s + ":" + jeVisUserNew);
            System.out.println("Memberships:");
            getUserMemberships(jeVisUserNew.getUserID()).forEach(jsonRelationship -> {
                if (jsonRelationship.getType() == 101) {
                    System.out.println("Group: " + jsonRelationship.getTo());
                }
            });
            System.out.println("ReadGIDs:");
            ds.getUserManager().getReadGIDS().forEach(aLong -> {
                System.out.println(aLong);
            });
        });


    }

    /**
     * Checks whether the given relationship type affects membership data and,
     * if so, marks the cache as needing an update.
     *
     * @param relationshipType the relationship type constant to evaluate
     */
    public void checkForChanges(int relationshipType) {
        try {
            switch (relationshipType) {
                case JEVisConstants.ObjectRelationship.MEMBER_READ:
                case JEVisConstants.ObjectRelationship.MEMBER_DELETE:
                case JEVisConstants.ObjectRelationship.MEMBER_WRITE:
                case JEVisConstants.ObjectRelationship.MEMBER_EXECUTE:
                case JEVisConstants.ObjectRelationship.MEMBER_CREATE:
                    needUpdate.set(true);
                    break;
                default:
                    break;

            }
        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    /**
     * Convenience overload of {@link #checkForChanges(int)} that inspects the
     * type of the given relationship.
     *
     * @param relationship the relationship whose type is evaluated
     */
    public void checkForChanges(JsonRelationship relationship) {
        try {
            checkForChanges(relationship.getType());
        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    /**
     * Marks the cache as stale if the given attribute change affects
     * authentication data (e.g., a {@code User} object's {@code Password}
     * attribute).
     *
     * @param object    the changed object
     * @param attribute the attribute name that changed
     * @param change    the type of change
     */
    public void checkForChanges(JsonObject object, String attribute, Change change) {
        try {
            if (attribute.equals("Password") && object.getJevisClass().equals("User")) {
                needUpdate.set(true);
            }

        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    /**
     * Marks the cache as stale if the changed object is a {@code User} or
     * {@code Group} that has been added, deleted, or modified.
     *
     * @param object the changed object
     * @param change the type of change
     */
    public void checkForChanges(JsonObject object, Change change) {
        try {
            if (object.getName().equals("User") && (change == Change.DELETE || change == Change.ADD || change == Change.CHANGE)) {
                needUpdate.set(true);
            } else if (object.getName().equals("Group") && (change == Change.DELETE || change == Change.ADD)) {
                needUpdate.set(true);
            }

        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }


    /**
     * Returns all membership relationships for the given user ID.
     *
     * @param userID the user's object ID
     * @return a list of membership relationships; empty if none
     */
    public List<JsonRelationship> getUserMemberships(long userID) {
        return groupMemberships.getOrDefault(userID, new ArrayList<>());
    }


    /**
     * Returns the cached {@link JEVisUserSQL} for the given account name, or
     * {@code null} if the account does not exist.
     *
     * @param userName the account name (case-insensitive)
     * @return the user, or {@code null}
     */
    public JEVisUserSQL getUser(String userName) {
        try {
            return users.get(userName.toLowerCase(Locale.ROOT));
        } catch (NullPointerException ex) {
            logger.error("User does not exist: '{}'", userName);
            logger.debug(ex, ex);
            return null;
        }
    }

    /**
     * Returns the full user map, keyed by lowercase account name.
     *
     * @return an unmodifiable view of all cached users
     */
    public Map<String, JEVisUserSQL> getUsers() {
        return users;
    }

    /**
     * Validates the given plaintext password against the stored PBKDF2 hash
     * for the named user.
     *
     * @param userName the account name
     * @param password the plaintext password to validate
     * @return {@code true} if the password is correct
     * @throws NoSuchAlgorithmException if the hash algorithm is unavailable
     * @throws InvalidKeySpecException  if the stored hash is malformed
     */
    public boolean validLogin(String userName, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        JEVisUserSQL user = users.get(userName.toLowerCase(Locale.ROOT));
        if (user != null && PasswordHash.validatePassword(password, user.getPassword())) {
            return true;
        } else {
            logger.error("Wrong PW for: '{}", userName);
            return false;
        }

    }

    /**
     * Returns {@code true} if the cache requires a refresh before the next
     * request is processed.
     *
     * @return {@code true} if stale
     */
    public boolean needUpdate() {
        return needUpdate.get();
    }

    /**
     * Returns the Guava session cache. Sessions expire after both write and
     * access, with the TTL configured via {@link Config#sessiontimeout}.
     *
     * @return the session cache
     */
    public Cache<String, Session> getSessions() {
        return sessions;
    }

    /**
     * The type of structural change that triggered a cache staleness check.
     */
    public enum Change {
        /**
         * An object was added.
         */
        ADD,
        /**
         * An object was deleted.
         */
        DELETE,
        /**
         * An object was modified.
         */
        CHANGE
    }
}
