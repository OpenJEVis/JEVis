package org.jevis.jeconfig.plugin.object.extension.role;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.jevis.api.JEVisConstants.Direction.BACKWARD;
import static org.jevis.api.JEVisConstants.Direction.FORWARD;
import static org.jevis.api.JEVisConstants.ObjectRelationship.*;

public class RoleManager {

    private static final Logger logger = LogManager.getLogger(RoleManager.class);
    final Role role;
    final JEVisObject roleObject;
    final JEVisDataSource ds;
    final List<Integer> REL_IDS = Arrays.asList(
            ROLE_READ, ROLE_WRITE,
            ROLE_EXECUTE, ROLE_CREATE,
            JEVisConstants.ObjectRelationship.ROLE_DELETE
    );
    private final Map<Long, Membership> allMemberships = new HashMap<>();
    private final Map<Long, User> allUsers = new HashMap<>();


    public RoleManager(JEVisObject roleObject) throws JEVisException {
        this.roleObject = roleObject;
        this.ds = roleObject.getDataSource();
        this.role = new Role(roleObject);

        getAllMemberships();
        getAllUser();
        parsRelationships(roleObject);
        this.role.getMemberShips().addAll(new ArrayList<>(allMemberships.values()));
        this.role.getUsers().addAll(new ArrayList<>(allUsers.values()));
    }


    public Role getRole() {
        return role;
    }


    private boolean[] checkUserMembership(User user, Membership membership) throws JEVisException {
        AtomicBoolean readExists = new AtomicBoolean(false);
        AtomicBoolean writeExists = new AtomicBoolean(false);
        AtomicBoolean executeExists = new AtomicBoolean(false);
        AtomicBoolean deleteExists = new AtomicBoolean(false);
        AtomicBoolean createExists = new AtomicBoolean(false);
        user.getUserObject().getRelationships().forEach(jeVisRelationship -> {
            if (jeVisRelationship.getEndID() == membership.getGroupObject().getID()) {

                try {
                    if (jeVisRelationship.isType(MEMBER_READ)) {
                        readExists.set(true);
                    }
                    if (jeVisRelationship.isType(MEMBER_WRITE)) {
                        writeExists.set(true);
                    }
                    if (jeVisRelationship.isType(MEMBER_EXECUTE)) {
                        executeExists.set(true);
                    }
                    if (jeVisRelationship.isType(MEMBER_CREATE)) {
                        createExists.set(true);
                    }
                    if (jeVisRelationship.isType(MEMBER_DELETE)) {
                        deleteExists.set(true);
                    }

                } catch (JEVisException e) {
                    e.printStackTrace();
                }

            }


        });

        return new boolean[]{readExists.get(), writeExists.get(), executeExists.get(), createExists.get(), deleteExists.get()};
    }


    private void deleteMembership(int type, User user, JEVisObject group) throws JEVisException {
        List<JEVisRelationship> toDelete = user.getUserObject().getRelationships(type, FORWARD);
        toDelete.forEach(jeVisRelationship -> {
            try {
                if (group.getID().equals(jeVisRelationship.getEndID())) {
                    logger.error("Delete Rel: {}", jeVisRelationship);
                    user.getUserObject().deleteRelationship(jeVisRelationship);
                    Thread.sleep(500);
                }

            } catch (JEVisException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateRoleMembership(Membership membership) throws JEVisException, InterruptedException {
        List<JEVisRelationship> toDelete = new ArrayList<>();
        AtomicBoolean readExists = new AtomicBoolean(false);
        AtomicBoolean writeExists = new AtomicBoolean(false);
        AtomicBoolean executeExists = new AtomicBoolean(false);
        AtomicBoolean deleteExists = new AtomicBoolean(false);
        AtomicBoolean createExists = new AtomicBoolean(false);

        membership.getGroupObject().getRelationships(ROLE_READ, BACKWARD).forEach(jeVisRelationship -> {
            if (jeVisRelationship.getStartID() == roleObject.getID() && jeVisRelationship.getEndID() == membership.getGroupObject().getID()) {
                readExists.set(true);
                if (!membership.isRead()) {
                    toDelete.add(jeVisRelationship);
                }
            }
        });
        membership.getGroupObject().getRelationships(ROLE_WRITE, BACKWARD).forEach(jeVisRelationship -> {
            if (jeVisRelationship.getStartID() == roleObject.getID() && jeVisRelationship.getEndID() == membership.getGroupObject().getID()) {
                writeExists.set(true);
                if (!membership.isWrite()) {
                    toDelete.add(jeVisRelationship);
                }
            }
        });
        membership.getGroupObject().getRelationships(ROLE_EXECUTE, BACKWARD).forEach(jeVisRelationship -> {
            if (jeVisRelationship.getStartID() == roleObject.getID() && jeVisRelationship.getEndID() == membership.getGroupObject().getID()) {
                executeExists.set(true);
                if (!membership.isExecute()) {
                    toDelete.add(jeVisRelationship);
                }
            }
        });
        membership.getGroupObject().getRelationships(ROLE_DELETE, BACKWARD).forEach(jeVisRelationship -> {
            if (jeVisRelationship.getStartID() == roleObject.getID() && jeVisRelationship.getEndID() == membership.getGroupObject().getID()) {
                deleteExists.set(true);
                if (!membership.isDelete()) {
                    toDelete.add(jeVisRelationship);
                }
            }
        });
        membership.getGroupObject().getRelationships(ROLE_CREATE, BACKWARD).forEach(jeVisRelationship -> {
            if (jeVisRelationship.getStartID() == roleObject.getID() && jeVisRelationship.getEndID() == membership.getGroupObject().getID()) {
                createExists.set(true);
                if (!membership.isCreate()) {
                    toDelete.add(jeVisRelationship);
                }
            }
        });


        if (!readExists.get() && membership.isRead()) {
            logger.error("Create Reade Rolemembership: {}", membership.getGroupName());
            membership.getGroupObject().buildRelationship(roleObject, ROLE_READ, BACKWARD);
            Thread.sleep(500);
        }
        if (!writeExists.get() && membership.isWrite()) {
            logger.error("Create Write Rolemembership: {}", membership.getGroupName());
            membership.getGroupObject().buildRelationship(roleObject, ROLE_WRITE, BACKWARD);
            Thread.sleep(500);
        }
        if (!executeExists.get() && membership.isExecute()) {
            logger.error("Create Exe Rolemembership: {}", membership.getGroupName());
            membership.getGroupObject().buildRelationship(roleObject, ROLE_EXECUTE, BACKWARD);
            Thread.sleep(500);
        }
        if (!deleteExists.get() && membership.isDelete()) {
            logger.error("Create Del Rolemembership: {}", membership.getGroupName());
            membership.getGroupObject().buildRelationship(roleObject, ROLE_DELETE, BACKWARD);
            Thread.sleep(500);
        }
        if (!createExists.get() && membership.isCreate()) {
            logger.error("Create create Rolemembership: {}", membership.getGroupName());
            membership.getGroupObject().buildRelationship(roleObject, ROLE_CREATE, BACKWARD);
            Thread.sleep(500);
        }


        toDelete.forEach(jeVisRelationship -> {
            logger.error("Delete Role Membership Rel: {}", jeVisRelationship);
            try {
                membership.getGroupObject().deleteRelationship(jeVisRelationship);
                Thread.sleep(500);
            } catch (Exception ex) {
                logger.error("Error while deleting relationship: {}: {}", jeVisRelationship, ex);
            }
        });

    }

    public void commit() {
        logger.info("Commit");

        role.getUsers().forEach(user -> {

            AtomicBoolean isMember = new AtomicBoolean(false);
            ObjectProperty<JEVisRelationship> memberRel = new SimpleObjectProperty<>();
            try {
                role.getRoleObject().getRelationships(ROLE_MEMBER, FORWARD).forEach(jeVisRelationship -> {
                    if (jeVisRelationship.getEndID() == user.getId()) {
                        memberRel.set(jeVisRelationship);
                        isMember.set(true);
                    }
                });
            } catch (JEVisException e) {
                logger.error(e);
            }

            if (isMember.get() != user.memberProperty().get()) {


                if (user.memberProperty().get()) {    /** create missing membership relationship **/
                    try {
                        logger.error("Create missing membership for group {}-{}", user.getId(), user.getUsername());
                        role.getRoleObject().buildRelationship(user.getUserObject(), ROLE_MEMBER, FORWARD);
                        Thread.sleep(500);
                    } catch (JEVisException | InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {   /** delete old membership **/
                    try {
                        logger.error("Delete old membership {}-{}->{}", user.getId(), user.getUsername(), memberRel.get());
                        user.getUserObject().deleteRelationship(memberRel.getValue());
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });


        role.getMemberShips().forEach(membership -> {
            try {
                /** Update User<->Role relationships **/
                updateRoleMembership(membership);
            } catch (JEVisException | InterruptedException e) {
                e.printStackTrace();
            }


            /** Update User<->Group relationships **/
            allUsers.entrySet().forEach(userEntry -> {
                try {
                    User user = userEntry.getValue();
                    if (user.memberProperty().get() || user.initMemberProperty().getValue()) {

                        boolean[] userMembership = checkUserMembership(user, membership);
                        logger.debug("Existing Memberships for user '{}' > {}", user.getUserObject().getName(), userMembership);

                        boolean isMember = user.memberProperty().get();

                        if (userMembership[0] != membership.isRead()) {
                            if (membership.isRead() && isMember) {
                                logger.error("Create missing read for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                JEVisRelationship newRel = user.getUserObject().buildRelationship(
                                        membership.getGroupObject(), MEMBER_READ, FORWARD);
                                Thread.sleep(500);
                                logger.error("New Rel: {}", newRel);
                            } else {
                                logger.error("Delete Existing read for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                deleteMembership(MEMBER_READ, user, membership.getGroupObject());
                            }
                        }

                        if (userMembership[1] != membership.isWrite()) {
                            if (membership.isWrite() && isMember) {
                                logger.error("Create missing write for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                JEVisRelationship newRel = user.getUserObject().buildRelationship(
                                        membership.getGroupObject(), MEMBER_WRITE, FORWARD);
                                Thread.sleep(500);
                                //newRel.commit();
                            } else {
                                logger.error("Delete Existing write for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                deleteMembership(MEMBER_WRITE, user, membership.getGroupObject());
                            }
                        }


                        if (userMembership[2] != membership.isExecute()) {
                            if (membership.isExecute() && isMember) {
                                logger.error("Create missing execute for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                JEVisRelationship newRel = user.getUserObject().buildRelationship(
                                        membership.getGroupObject(), MEMBER_EXECUTE, FORWARD);
                                Thread.sleep(500);
                                //newRel.commit();
                            } else {
                                logger.error("Delete Existing execute for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                deleteMembership(MEMBER_EXECUTE, user, membership.getGroupObject());
                            }
                        }

                        if (userMembership[3] != membership.isCreate()) {
                            if (membership.isCreate() && isMember) {
                                logger.error("Create missing create for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                JEVisRelationship newRel = user.getUserObject().buildRelationship(
                                        membership.getGroupObject(), MEMBER_CREATE, FORWARD);
                                Thread.sleep(500);
                                //newRel.commit();
                            } else {
                                logger.error("Delete Existing create for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                deleteMembership(MEMBER_CREATE, user, membership.getGroupObject());
                            }
                        }

                        if (userMembership[4] != membership.isDelete()) {
                            if (membership.isDelete() && isMember) {
                                logger.error("Create missing delete for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                JEVisRelationship newRel = user.getUserObject().buildRelationship(
                                        membership.getGroupObject(), MEMBER_DELETE, FORWARD);
                                Thread.sleep(500);
                                //newRel.commit();
                            } else {
                                logger.error("Delete Existing delete for group {}-{}", membership.getGroupid(), membership.getGroupName());
                                deleteMembership(MEMBER_DELETE, user, membership.getGroupObject());
                            }
                        }

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

            membership.createInitHash();
        });
    }


    private void getAllUser() throws JEVisException {
        List<JEVisObject> allUsers = ds.getObjects(ds.getJEVisClass("User"), true);

        for (JEVisObject userObj : allUsers) {
            User user = new User(userObj);
            this.allUsers.put(user.getId(), user);
        }
    }

    private void getAllMemberships() throws JEVisException {
        List<JEVisObject> allGroups = ds.getObjects(ds.getJEVisClass("Group"), true);
        for (JEVisObject group : allGroups) {
            Membership membership = new Membership(group, false, false, false, false, false);
            allMemberships.put(group.getID(), membership);
        }
    }

    private void parsRelationships(JEVisObject role) {
        try {

            role.getRelationships().forEach(jeVisRelationship -> {
                try {
                    if (REL_IDS.contains(jeVisRelationship.getType())) {
                        JEVisObject groupObj = jeVisRelationship.getEndObject();
                        Membership membership = allMemberships.getOrDefault(groupObj.getID(),
                                new Membership(groupObj, false, false, false, false, false));

                        switch (jeVisRelationship.getType()) {
                            case ROLE_READ:
                                membership.setRead(true);
                                break;
                            case ROLE_WRITE:
                                membership.setWrite(true);
                                break;
                            case ROLE_EXECUTE:
                                membership.setExecute(true);
                                break;
                            case ROLE_CREATE:
                                membership.setCreate(true);
                                break;
                            case ROLE_DELETE:
                                membership.setDelete(true);
                                break;
                        }
                        allMemberships.put(groupObj.getID(), membership);
                    } else if (ROLE_MEMBER == jeVisRelationship.getType()) {
                        JEVisObject userObj = jeVisRelationship.getEndObject();
                        User user = allUsers.getOrDefault(userObj.getID(), new User(userObj));
                        user.memberProperty().set(true);
                        user.initMemberProperty().set(true);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            allMemberships.forEach((aLong, membership) -> {
                membership.createInitHash();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
