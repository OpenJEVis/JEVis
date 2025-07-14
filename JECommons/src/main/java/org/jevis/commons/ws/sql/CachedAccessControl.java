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

public class CachedAccessControl {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CachedAccessControl.class);
    private static CachedAccessControl fastUserManager = null;
    private final Map<Long, List<JsonRelationship>> groupMemberships = new ConcurrentHashMap();
    private final AtomicBoolean needUpdate = new AtomicBoolean(false);
    private Map<String, JEVisUserNew> users;
    private Cache<String, Session> sessions;
    private SQLDataSource ds;

    public CachedAccessControl() throws Exception {
        sessions = CacheBuilder.newBuilder()
                .expireAfterWrite(Config.sessiontimeout, TimeUnit.MINUTES)
                .expireAfterAccess(Config.sessiontimeout, TimeUnit.MINUTES)
                .build();
    }

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


    public synchronized void updateUser(SQLDataSource dataSource) throws JEVisException {
        users = dataSource.getLoginTable().getAllUser();
    }

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
     * Debug helper
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
                // System.out.println(jsonRelationship);
            });
            System.out.println("ReadGIDs:");
            ds.getUserManager().getReadGIDS().forEach(aLong -> {
                System.out.println(aLong);
            });
        });


    }

    /**
     * Check if a new ObjectRelationship type is a Membership
     */
    public void checkForChanges(int relationshipType) {
        try {
            switch (relationshipType) {
                case JEVisConstants.ObjectRelationship.MEMBER_READ:
                case JEVisConstants.ObjectRelationship.MEMBER_DELETE:
                case JEVisConstants.ObjectRelationship.MEMBER_WRITE:
                case JEVisConstants.ObjectRelationship.MEMBER_EXECUTE:
                case JEVisConstants.ObjectRelationship.MEMBER_CREATE:
                    //updateCache();
                    needUpdate.set(true);
                    break;
                default:
                    break;

            }
        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    public void checkForChanges(JsonRelationship relationship) {
        try {
            checkForChanges(relationship.getType());
        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    public void checkForChanges(JsonObject object, String attribute, Change change) {
        try {
            if (attribute.equals("Password") && object.getJevisClass().equals("User")) {
                //updateCache();
                needUpdate.set(true);
            }

        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    public void checkForChanges(JsonObject object, Change change) {
        try {
            if (object.getName().equals("User") && (change == Change.DELETE || change == Change.ADD || change == Change.CHANGE)) {
                //updateCache();
                needUpdate.set(true);
            } else if (object.getName().equals("Group") && (change == Change.DELETE || change == Change.ADD)) {
                //updateCache();
                needUpdate.set(true);
            }

        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }


    public List<JsonRelationship> getUserMemberships(long userID) {
        return groupMemberships.getOrDefault(userID, new ArrayList<>());
    }


    public JEVisUserNew getUser(String userName) {
        try {
            return users.get(userName.toLowerCase(Locale.ROOT));
        } catch (NullPointerException ex) {
            logger.error("User does not exist: '{}'", userName);
            logger.debug(ex, ex);
            return null;
        }
    }

    public Map<String, JEVisUserNew> getUsers() {
        return users;
    }

    public boolean validLogin(String userName, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        JEVisUserNew user = users.get(userName.toLowerCase(Locale.ROOT));
        if (user != null && PasswordHash.validatePassword(password, user.getPassword())) {
            return true;
        } else {
            logger.error("Wrong PW for: '{}", userName);
            return false;
        }

    }

    public boolean needUpdate() {
        return needUpdate.get();
    }

    public Cache<String, Session> getSessions() {
        return sessions;
    }

    public enum Change {
        ADD, DELETE, CHANGE
    }
}
