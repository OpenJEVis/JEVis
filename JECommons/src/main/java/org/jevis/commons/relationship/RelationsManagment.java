/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI-SQL.
 * <p>
 * JEAPI-SQL is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI-SQL is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-SQL. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI-SQL is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.relationship;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.jevis.api.JEVisConstants.ObjectRelationship.*;

/**
 * This class helps handling the Relationship
 *
 * TODO: this class belongs into he JEAp or JECommons
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class RelationsManagment {

    private static final Logger logger = LogManager.getLogger(RelationsManagment.class);

    /**
     *
     * @param user
     * @param object
     * @return
     */
    public static boolean canRead(JEVisObject user, JEVisObject object) throws JEVisException {
        return checkMembershipForType(user, object, MEMBER_READ);
    }

    /**
     *
     * @param user
     * @param object
     * @return
     */
    public static boolean canWrite(JEVisObject user, JEVisObject object) throws JEVisException {
        return checkMembershipForType(user, object, MEMBER_WRITE);
    }

    /**
     *
     * @param user
     * @param object
     * @return
     */
    public static boolean canDelete(JEVisObject user, JEVisObject object) throws JEVisException {
        return checkMembershipForType(user, object, MEMBER_DELETE);
    }

    /**
     *
     * @param user
     * @param object
     * @return
     */
    public static boolean canCreate(JEVisObject user, JEVisObject object) throws JEVisException {
        return checkMembershipForType(user, object, MEMBER_CREATE);
    }

    /**
     *
     * @param user
     * @param object
     * @return
     */
    public static boolean canExcecude(JEVisObject user, JEVisObject object) throws JEVisException {
        return checkMembershipForType(user, object, MEMBER_EXECUTE);
    }

    /**
     *
     * @param user
     * @param object
     * @param type
     * @return
     */
    public static boolean checkMembershipForType(JEVisObject user, JEVisObject object, int type) throws JEVisException {
        logger.trace("CheckMembership {} {} {}", user.getID(), object.getID(), type);
        try {
            return user.getDataSource().getCurrentUser().isSysAdmin();
        } catch (Exception ex) {
            logger.info("Error while checking Sys Admin status:  ", ex);
            logger.error("Error while checking Sys Admin status:  {}", ex.getMessage());//ToDO there is some error here
        }

        logger.debug("checkMembershipForType: user: {} object: {} type: {}", user.getID(), object.getID(), type);
        try {
            List<JEVisRelationship> userMemberships = getMembershipsRel(user);

            for (JEVisRelationship or : object.getRelationships()) {
                for (JEVisRelationship ur : userMemberships) {

                    //is the Object Owner end the same as the user membership end
                    logger.debug("object.owner[{}]==user.membership[{}]", ur.getEndObject().getID(), or.getEndObject().getID());
                    if (ur.getEndObject().getID().equals(or.getEndObject().getID())) {
                        if (ur.isType(type)) {

                            return true;
                        }
                    }
                }
            }
        } catch (NullPointerException ne) {
            logger.debug("Error while checking Memberships:  {}", ne);//ToDO there is some error here
        }
        return false;
    }

    /**
     *
     * @param object
     * @return
     */
    public static List<JEVisRelationship> getRelationByType(JEVisObject object, int type) throws JEVisException {
        List<JEVisRelationship> memberships = new ArrayList<JEVisRelationship>();
        List<JEVisRelationship> objRel = object.getRelationships();
//        logger.debug("Relationship.size: {}", objRel.size());
        for (JEVisRelationship r : objRel) {
            if (r.isType(type)) {
                memberships.add(r);
            }
        }
        return memberships;
    }

    /**
     *
     * @param object
     * @return
     */
    public static List<JEVisRelationship> getMembershipsRel(JEVisObject object) throws JEVisException {
//        logger.debug("getMembershipsRelations for {}", object.getID());
        List<JEVisRelationship> memberships = new ArrayList<JEVisRelationship>();
        List<JEVisRelationship> objRel = object.getRelationships();
//        logger.debug("Relationship totals: {}", objRel.size());

        for (JEVisRelationship r : objRel) {
//            logger.debug("Checking relationship: {}->{} [{}]", r.getStartObject().getID(), r.getEndObject().getID(), r.getType());
            if (r.isType(MEMBER_READ)
                    || r.isType(MEMBER_WRITE)
                    || r.isType(MEMBER_EXECUTE)
                    || r.isType(MEMBER_CREATE)
                    || r.isType(MEMBER_DELETE)) {
                logger.debug("Found membership: {}", r);
                memberships.add(r);
            }
        }
        logger.debug("done searching");
        return memberships;
    }

//    /**
//     * Is this still in use? also implementation seems wrong
//     *
//     * @param user
//     * @return
//     * @throws JEVisException
//     */
//    public static boolean isSysAdmin(JEVisObject user) throws JEVisException {
//        logger.trace("isSysAdmin {} ", user.getID());
//
//        JEVisAttribute sysAdminAtt = user.getAttribute("Sys Admin");
//        logger.trace("sysAdmin getValue");
//        JEVisSample value = sysAdminAtt.getLatestSample();
//        if (value == null || value.getValueAsBoolean() == false) {
//            logger.trace("isSysAdmin {} {}", user.getID(), false);
//            return false;
//        } else {
//            logger.trace("isSysAdmin {} {}", user.getID(), true);
//            return value.getValueAsBoolean();
//        }
//
//    }

//    public static boolean isParentRelationship(JEVisClass parent, JEVisClass child) throws JEVisException {
//
//        if (child.getInheritance() != null) {
//            return isParentRelationship(parent, child.getInheritance());
//        }
//
//        for (JEVisClassRelationship rel : parent.getRelationships(OK_PARENT)) {
//            if (rel.getOtherClass(parent).equals(child)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public static boolean isNestedRelationship(JEVisClass parent, JEVisClass child) throws JEVisException {
//        if (child.getInheritance() != null) {
//            return isNestedRelationship(parent, child.getInheritance());
//        }
//
//        for (JEVisClassRelationship rel : parent.getRelationships(NESTED)) {
//            if (rel.getOtherClass(parent).equals(child)) {
//                return true;
//            }
//        }
//
//        return false;
//    }

    private static List<JEVisObject> getObjectOwner(JEVisObject obj, int type) {
        List<JEVisObject> owners = new ArrayList<JEVisObject>();

        try {
            for (JEVisRelationship objOwner : obj.getRelationships(JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD)) {
//                logger.info("objOwner: " + objOwner);
                try {
                    for (JEVisRelationship groupMemeber : objOwner.getEndObject().getRelationships(type, JEVisConstants.Direction.BACKWARD)) {
//                        logger.info("groupMember: " + groupMemeber);
                        owners.add(groupMemeber.getStartObject());
                    }
                } catch (JEVisException ex) {
                    logger.fatal(ex);
                }
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        return owners;

    }

    /**
     * Checks if an membership relationship can be deleted by the current user.
     *
     *
     * @param rel JEVisRelationship from the type Membership
     * @return true if the user can delete, throws an exception with the reason
     * when not. returns false if the type is not an membership
     */
    public static boolean canDeleteMembership(JEVisRelationship rel) {
        logger.info("canDeleteMembership: " + rel);
        try {
            JEVisUser juser = rel.getStartObject().getDataSource().getCurrentUser();


            if (juser.isSysAdmin()) {
                logger.info("is sysadmin");
                return true;
            }

            if (rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_CREATE
                    || rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_DELETE
                    || rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_EXECUTE
                    || rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_READ
                    || rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_WRITE) {


                if (rel.getStartID() == juser.getUserID()) {
                    logger.info("UserID: " + juser.getUserID() + " Rel.start: " + rel.getStartID());
                    return false;
//                    throw new JEVisException("Insufficient rights, user cannot delete its own user right", JEVisExceptionCodes.UNAUTHORIZED);
                }


                if (getObjectOwner(rel.getStartObject(), JEVisConstants.ObjectRelationship.MEMBER_DELETE).contains(juser.getUserObject())) {
//                    logger.info("is owner-owner: " + rel);
                    return true;
                }

            }
        } catch (Exception ex) {
            logger.fatal(ex);
        }

        return false;

    }

    public static boolean canDeleteOwnership(JEVisRelationship rel) {
        try {

            JEVisUser currentUser = rel.getStartObject().getDataSource().getCurrentUser();

            //Sys Admins can do everything for now
            if (currentUser.isSysAdmin()) {
                return true;
            }

            List<JEVisObject> groups = new ArrayList<>();
            List<JEVisRelationship> rels = currentUser.getUserObject().getRelationships(JEVisConstants.ObjectRelationship.MEMBER_READ, JEVisConstants.Direction.FORWARD);
            for (JEVisRelationship re : rels) {
                if (!groups.contains(re.getEndObject())) {
                    groups.add(re.getEndObject());
                }
            }

//            logger.info("My Groups: " + Arrays.toString(userGroups.toArray()));
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER) {

                //get Owner Group
                JEVisObject relOwner = rel.getEndObject();
//                JEVisObject relTarget = rel.getStartObject();

                //User cannot delte his own permission
                if (groups.contains(relOwner)) {
//                    logger.info("cannot delete my own right");
                    return false;
                }

                //get Owner of this group
                for (JEVisRelationship relOwnerRel : relOwner.getRelationships(JEVisConstants.ObjectRelationship.OWNER, JEVisConstants.Direction.FORWARD)) {
                    try {
                        JEVisObject groupOwnerOwner = relOwnerRel.getEndObject();
//                        logger.info("groupOwnerOwner: " + groupOwnerOwner.getName());

                        if (groups.contains(groupOwnerOwner)) {
//                            logger.info("im the owner of the owner so its ok");
                            return true;
                        }

                    } catch (Exception ex) {
                        //TODO: remove this trycatch workaround which is nessasary because the user can see but not access relationshipts which are not for him
                    }
                }

            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }

        return false;
    }

    /**
     * Checks if the given users is allowed to delete the given relationship
     *
     * TODO: add handling for none Userrights relationships TODO: i it a goodway
     * to return only true and exeptions?
     *
     * @param user the current user using the JEAPI
     * @param rel JEVisRelationship to test for if it can be deleted
     *
     * @return true if the user has the permission to delete the
     * JEVisRelationship, throws JEVisException if false
     * @throws JEVisException
     */
    public static boolean canDeleteRelationship(JEVisObject user, JEVisRelationship rel) throws JEVisException {
//        user = user.getDataSource().getCurrentUser();
        if (rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_CREATE
                || rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_DELETE
                || rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_EXECUTE
                || rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_READ
                || rel.getType() == JEVisConstants.ObjectRelationship.MEMBER_WRITE) {
            return canDeleteMembership(rel);
        } else if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER) {
            return canDeleteOwnership(rel);
        } else {
            //TODO: check the rules for other kind of relationships
            //Workaround for the alpha phase, normally the default is false
            return true;
        }
    }

}
