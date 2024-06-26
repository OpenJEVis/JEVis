package org.jevis.commons.ws.sql;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
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
import java.util.concurrent.atomic.AtomicBoolean;

public class CachedAccessControl {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CachedAccessControl.class);
    private static CachedAccessControl fastUserManager = null;
    private final Map<Long, List<JsonRelationship>> groupMemberships = new ConcurrentHashMap();
    private final AtomicBoolean needUpdate = new AtomicBoolean(false);
    private Map<String, JEVisUserNew> users;
    //private SQLDataSource dataSource;

    public CachedAccessControl() throws Exception {
        //this.dataSource = dataSource;
    }

    /*
        public static CachedAccessControl getInstance(SQLDataSource dataSource) throws Exception {
            if (fastUserManager != null) {
                return fastUserManager;
            } else {
                fastUserManager = new CachedAccessControl(dataSource);
                fastUserManager.updateCache();
                return fastUserManager;
            }


        }

     */
    public static CachedAccessControl getInstance(SQLDataSource ds) throws Exception {
        if (fastUserManager != null) {
            if (fastUserManager.needUpdate()) fastUserManager.updateCache(ds);
            return fastUserManager;
        } else {
            fastUserManager = new CachedAccessControl();
            fastUserManager.updateCache(ds);
            return fastUserManager;
        }


    }

    public synchronized void updateUser(SQLDataSource dataSource) throws JEVisException {
        users = dataSource.getLoginTable().getAllUser();
    }

    public synchronized void updateCache(SQLDataSource dataSource) throws Exception {
        logger.error("Update Access Control Cache");
        needUpdate.set(false);
        Benchmark benchmark = new Benchmark();
        users = dataSource.getLoginTable().getAllUser();

        //dataSource.getAttributeTable().getDataPorozessorTodoList();
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
/*
        users.forEach((s, jeVisUserNew) -> {
            try {
                logger.debug("User: '{}', memberships: {}", jeVisUserNew.getAccountName(), groupMemberships.get(jeVisUserNew.getUserID()).size());

                groupMemberships.get(jeVisUserNew.getUserID()).forEach(jsonRelationship -> {
                    logger.trace("-- {}", jsonRelationship.getTo());
                });

            } catch (Exception ex) {
                logger.error(ex);
            }
        });

 */
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

    public enum Change {
        ADD, DELETE, CHANGE
    }
}
