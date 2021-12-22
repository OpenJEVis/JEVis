package org.jevis.commons.ws.sql;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisConstants;
import org.jevis.commons.utils.Benchmark;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CachedAccessControl {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CachedAccessControl.class);
    private static CachedAccessControl fastUserManager = null;
    private Map<String, JEVisUserNew> users;
    private Map<Long, List<JsonRelationship>> groupMemberships = new ConcurrentHashMap();
    private SQLDataSource dataSource;

    public enum Change {
        ADD, DELETE, CHANGE
    }

    public CachedAccessControl(SQLDataSource dataSource) throws Exception {
        this.dataSource = dataSource;
    }

    public synchronized void updateCache() throws Exception {
        logger.error("Update Access Control Cache");
        Benchmark benchmark = new Benchmark();
        users = dataSource.getLoginTable().getAllUser();

        //dataSource.getAttributeTable().getDataPorozessorTodoList();

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

        logger.error("Finished user cache:");
        users.forEach((s, jeVisUserNew) -> {
            try {
                logger.debug("User: '{}', memberships: {}", jeVisUserNew.getAccountName(), groupMemberships.get(jeVisUserNew.getUserID()).size());
                /*
                groupMemberships.get(jeVisUserNew.getUserID()).forEach(jsonRelationship -> {
                    logger.trace("-- {}", jsonRelationship.getTo());
                });
                 */
            } catch (Exception ex) {
                logger.error(ex);
            }
        });
        benchmark.printBechmark("Finished Access Control Cache update");

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
                    updateCache();
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
                updateCache();
            }

        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }

    public void checkForChanges(JsonObject object, Change change) {
        try {
            if (object.getName().equals("User") && (change == Change.DELETE || change == Change.ADD || change == Change.CHANGE)) {
                updateCache();
            } else if (object.getName().equals("Group") && (change == Change.DELETE || change == Change.ADD)) {
                updateCache();
            }

        } catch (Exception ex) {
            logger.error(ex, ex);
        }
    }


    public List<JsonRelationship> getUserMemberships(long userID) {
        return groupMemberships.getOrDefault(userID, new ArrayList<>());
    }


    public JEVisUserNew getUser(String userName) {
        return users.get(userName.toLowerCase(Locale.ROOT));
    }

    public boolean validLogin(String userName, String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
        JEVisUserNew user = users.get(userName.toLowerCase(Locale.ROOT));
        if (user != null && PasswordHash.validatePassword(password, user.getPassword())) {
            return true;
        } else {
            return false;
        }

    }

    public static CachedAccessControl getInstance(SQLDataSource dataSource) throws Exception {
        if (fastUserManager != null) {
            return fastUserManager;
        } else {
            fastUserManager = new CachedAccessControl(dataSource);
            fastUserManager.updateCache();
            return fastUserManager;
        }


    }
}
