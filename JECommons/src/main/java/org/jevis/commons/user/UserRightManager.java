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

    private final JEVisUser user;
    private List<JEVisRelationship> permissions = new ArrayList<>();
    private Logger logger = LogManager.getLogger(UserRightManager.class);
    private JEVisDataSource ds;
    private List<Long> readGIDS;
    private List<Long> createGIDS;
    private List<Long> deleteGIDS;
    private List<Long> exeGIDS;
    private List<Long> writeGIDS;
    private List<JEVisObject> objects;

    public UserRightManager(JEVisDataSource ds, JEVisUser user, List<JEVisRelationship> permissions) {
        logger.trace("Init UserRightManager for user: [{}]{} {}", user.getUserID());
        this.user = user;
        this.ds = ds;
        this.permissions = permissions;
    }

    public boolean isSysAdmin() {
        return user.isSysAdmin();
    }

    public boolean canRead(long objectID) {
        return checkMebershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_READ);
    }

    public boolean canWrite(long objectID) {
        return checkMebershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_WRITE);
    }

    public boolean canCreate(long objectID) {
        return checkMebershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_CREATE);
    }

    public boolean canExecute(long objectID) {
        return checkMebershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_EXECUTE);
    }

    public boolean canDelete(long objectID) {
        return checkMebershipForType(objectID, JEVisConstants.ObjectRelationship.MEMBER_DELETE);
    }

    public boolean canDeleteClass(String jclass) {
        return user.isSysAdmin();
    }

    public List<Long> getGroupsRead() {
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
        List<JEVisRelationship> filterd = new ArrayList<JEVisRelationship>();
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
                    filterd.add(rel);
                } else if (rel.getType() >= JEVisConstants.ObjectRelationship.MEMBER_READ
                        && rel.getType() <= JEVisConstants.ObjectRelationship.MEMBER_DELETE
                        && getGroupsRead().contains(rel.getEndID())) {
                    //for the local UserRightsManager to work we need also the membership Relationships
                    filterd.add(rel);
//                    logger.info("---> add Membership Relationships: {}", rel);
                }
            } catch (Exception ex) {

            }
        }
        logger.trace("after filter {}", filterd.size());

        logger.trace("Time for Filter: {}ms", ((new Date().getTime()) - start));

        return filterd;
    }

    public void reload() {
        try {
            logger.info("----------------------------- Reload");
            permissions = ds.getRelationships();
            readGIDS = null;
        } catch (Exception ex) {
            logger.catching(ex);
        }
    }

    private void getGroupPermissions() {
        if (readGIDS == null) {
            logger.debug("reload userrights");
            readGIDS = new ArrayList<Long>();
            createGIDS = new ArrayList<Long>();
            writeGIDS = new ArrayList<Long>();
            deleteGIDS = new ArrayList<Long>();
            exeGIDS = new ArrayList<Long>();

            logger.info("UserID: " + user.getUserID());
            for (JEVisRelationship or : permissions) {

                try {
                    logger.info("Type: " + or.getType());
                    if (or.getType() >= JEVisConstants.ObjectRelationship.MEMBER_READ || or.getStartID() == user.getUserID()) {
                        logger.info("Membership: " + or);
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
                                exeGIDS.add(or.getEndID());
                                break;
                            case JEVisConstants.ObjectRelationship.MEMBER_CREATE:
                                createGIDS.add(or.getEndID());
                                break;
                        }

                    }
                } catch (Exception ex) {

                }
            }

            logger.trace("Groups (Read,Write,Del,Exe,Cre): {},{},{},{},{}", readGIDS, writeGIDS, deleteGIDS, exeGIDS, createGIDS);
        }

    }

    /**
     * @param object Object to check the permission for
     * @param type   type of the membership(read,write,exe...)
     * @return
     */
    private boolean checkMebershipForType(long object, int type) {
        logger.trace("CheckMembership user:{} object:{} type:{}", user.getUserID(), object, type);
        if (isSysAdmin()) {
            //Sys Admins can do everything wihout questions
            return true;
        }

        try {
            getGroupPermissions();
            boolean can = false;

            //User Object is a special case
            // - User can Read him self
            // - User can edit all attributes except "Sys Admin" and Enabled are protectet
            // - User cannot remame the object for now
            // - What is with the right to create relationships?!
            if (user.getUserID() == object) {
                if (type == JEVisConstants.ObjectRelationship.MEMBER_READ || type == JEVisConstants.ObjectRelationship.MEMBER_WRITE) {
//                    logger.info("----> is userobject: o:{} u:{}", object, user);
                    return true;
                }
            }

            //Common userrights
            for (JEVisRelationship or : permissions) {
                try {
//                    logger.debug("---------------> PerRel: {}", or);

                    if (or.getType() == JEVisConstants.ObjectRelationship.OWNER && or.getStartID() == object) {
                        logger.info("----> Ownership: {}", or);

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
                                if (exeGIDS.contains(or.getEndID())) {
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
                    logger.catching(ex);
                }

            }
            return can;
        } catch (Exception ne) {
            logger.debug("Error while checking Memberships:  {}", ne);//ToDO there is some error here
        }
        return false;
    }

}
