/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.user;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author fs
 */
public class UserRightManager {

    private static final Logger logger = LogManager.getLogger(UserRightManager.class);
    private final JEVisUser user;
    private List<JEVisRelationship> permissions = new ArrayList<>();
    private final JEVisDataSource ds;
    private List<Long> readGIDS;
    private List<Long> createGIDS;
    private List<Long> deleteGIDS;
    private List<Long> executeGIDS;
    private List<Long> writeGIDS;
    private List<JEVisObject> objects;

    //    public UserRightManager(JEVisDataSource ds, JEVisUser user, List<JEVisRelationship> permissions) {
    public UserRightManager(JEVisDataSource ds, JEVisUser user) {
        logger.trace("Init UserRightManager for user: {}:{}", user.getAccountName(), user.getUserID());
        this.user = user;
        this.ds = ds;

    }

    public boolean isSysAdmin() {
        return user.isSysAdmin();
    }

    /**
     * Check for exeptions for the common rules
     *
     * @param objectID
     * @return
     */
    private boolean isRuleException(long objectID) {
        try {
            if (canRead(objectID)) {
                return ds.getObject(objectID).getJEVisClassName().equals("Data Notes");
            }
            return false;


        } catch (Exception ex) {
            return false;
        }
    }

    public boolean canRead(long objectID) {
        return checkMembershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_READ);
    }

    public boolean canWrite(long objectID) {
        if (isRuleException(objectID)) return true;
        return checkMembershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_WRITE);
    }

    public boolean canCreate(long objectID) {
        return checkMembershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_CREATE);
    }


    public boolean canExecute(long objectID) {
        return checkMembershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
    }

    public boolean canDelete(long objectID) {
        return checkMembershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_DELETE);
    }

    public boolean canDeleteClass(String jevisClass) {
        return user.isSysAdmin();
    }

    public List<Long> getGroupsRead() {
        try {
            permissions = ds.getRelationships();
        } catch (Exception e) {
            logger.error("Error getting group reads", e);
        }
        getGroupPermissions();
        return readGIDS;
    }

    public void setObjects(List<JEVisObject> objs) {
        this.objects = objs;
    }

    public List<JEVisObject> getAllObjects() {
        return objects;
    }

    public List<Long> getAllObjectID(List<JEVisRelationship> rels) {
        List<Long> ids = new ArrayList<>();
        for (JEVisRelationship rel : rels) {
            if (!ids.contains(rel.getEndID())) {
                ids.add(rel.getEndID());
            }

            if (!ids.contains(rel.getStartID())) {
                ids.add(rel.getStartID());
            }
        }

        return ids;
    }

    public List<JEVisRelationship> filterRelationships() {
        Long start = new Date().getTime();
        logger.trace("filterRelationships() total: {}", permissions.size());
        List<JEVisRelationship> filtered = new ArrayList<JEVisRelationship>();
        List<Long> owned = new ArrayList<Long>();

        getGroupPermissions();
        logger.trace("Groups: {}", Arrays.toString(getGroupsRead().toArray()));

        //find all the object for all Groups(where the user is Memeber)
        for (JEVisRelationship rel : permissions) {
            try {
                if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER
                        && getGroupsRead().contains(rel.getEndID())) {
                    owned.add(rel.getStartID());
                }
            } catch (Exception ex) {

            }
        }
        logger.trace("Owned Objects: {}", owned.size());

        //Find all Relationships which are oned by the user(Goups)
        for (JEVisRelationship rel : permissions) {
            try {
                if (owned.contains(rel.getStartID())
                        || owned.contains(rel.getEndID())) {
                    filtered.add(rel);
                } else if (rel.getType() >= JEVisConstants.ObjectRelationship.MEMBER_READ
                        && rel.getType() <= JEVisConstants.ObjectRelationship.MEMBER_DELETE
                        && getGroupsRead().contains(rel.getEndID())) {
                    //for the local UserRightsManager to work we need also the membership Relationships
                    filtered.add(rel);
//                    logger.info("---> add Membership Relationships: {}", rel);
                }
            } catch (Exception ex) {
                logger.error("Error filtering relationships", ex);
            }
        }
        logger.trace("after filter {}", filtered.size());

        logger.trace("Time for Filter: {} ms", ((new Date().getTime()) - start));

        return filtered;
    }

    public void reload() {
        try {
            logger.trace("----------------------------- Reload");
            permissions = ds.getRelationships();
            readGIDS = null;
        } catch (Exception ex) {
            logger.error("Error while reloading relationships", ex);
        }
    }


    private void getGroupPermissions() {
        if (readGIDS == null) {
            logger.trace("reload user rights");
            readGIDS = new ArrayList<Long>();
            createGIDS = new ArrayList<Long>();
            writeGIDS = new ArrayList<Long>();
            deleteGIDS = new ArrayList<Long>();
            executeGIDS = new ArrayList<Long>();

            logger.trace("UserID: {}", user.getUserID());
            for (JEVisRelationship or : permissions) {

                try {
                    if (or.getType() < JEVisConstants.ObjectRelationship.MEMBER_READ) {
                        or.getStartID();
                        user.getUserID();
                    }

                    //from user to group
                    if (or.getStartID() == user.getUserID()) {


                        switch (or.getType()) {
                            case JEVisConstants.ObjectRelationship.MEMBER_READ:
                                readGIDS.add(or.getEndID());
                                break;
                            case JEVisConstants.ObjectRelationship.MEMBER_WRITE:
                                writeGIDS.add(or.getEndID());
                                break;
                            case JEVisConstants.ObjectRelationship.MEMBER_DELETE:
                                deleteGIDS.add(or.getEndID());
                                break;
                            case JEVisConstants.ObjectRelationship.MEMBER_EXECUTE:
                                executeGIDS.add(or.getEndID());
                                break;
                            case JEVisConstants.ObjectRelationship.MEMBER_CREATE:
                                createGIDS.add(or.getEndID());
                                break;
                        }

                    }
                } catch (Exception ex) {
                    logger.error("Error while getting group permissions", ex);
                }
            }

            logger.trace("Groups (Read,Write,Del,Exe,Cre): {},{},{},{},{}", readGIDS, writeGIDS, deleteGIDS, executeGIDS, createGIDS);
        }

    }

    /**
     * @param object Object to check the permission for
     * @param type   type of the membership(read,write,exe...)
     * @return
     */
    private boolean checkMembershipForType(long object, int type) {
        logger.trace("CheckMembership user:{} object:{} type:{}", user.getUserID(), object, type);
        if (isSysAdmin()) {
            //Sys Admins can do everything without questions
            return true;
        }

        try {
            permissions = ds.getRelationships();
            getGroupPermissions();
            boolean can = false;

            //User Object is a special case
            // - User can Read him self
            // - User can edit all attributes except "Sys Admin" and Enabled are protectet
            // - User cannot rename the object for now
            // - What is with the right to create relationships?!
            if (user.getUserID() == object) {
                if (type == JEVisConstants.ObjectRelationship.MEMBER_READ || type == JEVisConstants.ObjectRelationship.MEMBER_WRITE) {
//                    logger.info("----> is user object: o:{} u:{}", object, user);
                    return true;
                }
            }

            //Common user rights
            for (JEVisRelationship or : permissions) {
                try {
//                    logger.debug("---------------> PerRel: {}", or);

                    if (or.getType() == JEVisConstants.ObjectRelationship.OWNER && or.getStartID() == object) {
                        logger.trace("----> Ownership: {}", or);

                        switch (type) {
                            case JEVisConstants.ObjectRelationship.MEMBER_READ:
                                if (readGIDS.contains(or.getEndID())) {
                                    can = true;
                                }
                                break;
                            case JEVisConstants.ObjectRelationship.MEMBER_DELETE:
                                if (deleteGIDS.contains(or.getEndID())) {
                                    can = true;
                                }
                                break;
                            case JEVisConstants.ObjectRelationship.MEMBER_EXECUTE:
                                if (executeGIDS.contains(or.getEndID())) {
                                    can = true;
                                }
                                break;
                            case JEVisConstants.ObjectRelationship.MEMBER_WRITE:
                                if (writeGIDS.contains(or.getEndID())) {
                                    can = true;
                                }
                                break;
                            case JEVisConstants.ObjectRelationship.MEMBER_CREATE:
                                if (createGIDS.contains(or.getEndID())) {
                                    can = true;
                                }
                                break;
                        }

                        if (can) {//end loop if the user can
                            return can;
                        }

                    }
                } catch (Exception ex) {
                    logger.error("Error getting common user rights", ex);
                }

            }
            return can;
        } catch (Exception ne) {
            logger.error("Error while checking Memberships", ne);//ToDO there is some error here
        }
        return false;
    }

}
