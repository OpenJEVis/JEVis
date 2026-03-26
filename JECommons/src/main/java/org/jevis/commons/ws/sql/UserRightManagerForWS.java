/*
  Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>

  This file is part of JECommons.

  JECommons is free software: you can redistribute it and/or modify it under
  the terms of the GNU General Public License as published by the Free Software
  Foundation in version 3.

  JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  A PARTICULAR PURPOSE. See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with
  JECommons. If not, see <http://www.gnu.org/licenses/>.

  JECommons is part of the OpenJEVis project, further project information are
  published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.ws.sql;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;

import java.util.*;

/**
 * Per-request user rights manager that evaluates object-level permissions
 * for the currently authenticated user.
 *
 * <p>Permissions are derived from OWNER relationships between objects and the
 * user's group memberships (loaded from {@link CachedAccessControl}).
 * Five permission levels are supported: READ, WRITE, DELETE, EXECUTE, and
 * CREATE.
 *
 * <p>Sys-admin users bypass all permission checks and are allowed to perform
 * any operation.
 *
 * @author fs
 */
public class UserRightManagerForWS {

    private final JEVisUserSQL user;
    private final SQLDataSource ds;
    private final boolean isSSO;
    private final List<Long> readGIDS = new ArrayList<>();
    private final List<Long> createGIDS = new ArrayList<>();
    private final List<Long> deleteGIDS = new ArrayList<>();
    private final List<Long> exeGIDS = new ArrayList<>();
    private final List<Long> writeGIDS = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(UserRightManagerForWS.class);

    /**
     * Creates a new rights manager for the current user and populates the
     * per-permission group ID lists from the membership cache.
     *
     * @param ds  the per-request data source (used to resolve memberships)
     * @param SSO {@code true} if SSO-based sessions are supported
     */
    public UserRightManagerForWS(SQLDataSource ds, boolean SSO) {
        this.user = ds.getCurrentUser();
        this.ds = ds;
        this.isSSO = SSO;
        init();
    }

    /**
     * Populates the read/write/delete/execute/create group ID lists by querying
     * the {@link CachedAccessControl} membership cache for the current user.
     * Called once from the constructor.
     */
    public void init() {
        try {
            List<JsonRelationship> userRel = CachedAccessControl.getInstance(ds, this.isSSO).getUserMemberships(this.ds.getCurrentUser().getUserID());

            for (JsonRelationship rel : userRel) {
                switch (rel.getType()) {
                    case JEVisConstants.ObjectRelationship.MEMBER_READ:
                        this.readGIDS.add(rel.getTo());
                        break;
                    case JEVisConstants.ObjectRelationship.MEMBER_WRITE:
                        this.writeGIDS.add(rel.getTo());
                        break;
                    case JEVisConstants.ObjectRelationship.MEMBER_DELETE:
                        this.deleteGIDS.add(rel.getTo());
                        break;
                    case JEVisConstants.ObjectRelationship.MEMBER_EXECUTE:
                        this.exeGIDS.add(rel.getTo());
                        break;
                    case JEVisConstants.ObjectRelationship.MEMBER_CREATE:
                        this.createGIDS.add(rel.getTo());
                        break;
                }
            }
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    /**
     * Filters the full relationship list to those the current user is allowed
     * to see. Sys-admins receive the unfiltered list.
     * <p>
     * A relationship is visible if both its source and target objects are
     * accessible to the user (owned by one of the user's read groups, or public).
     *
     * @param allRelationShips the complete list of relationships
     * @return a filtered list containing only the visible relationships
     */
    public List<JsonRelationship> filterRelationships(List<JsonRelationship> allRelationShips) {
//        logger.error("filterRelationships---\n{}\n---", this.readGIDS);
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return allRelationShips;
        }

        List<JsonRelationship> list = Collections.synchronizedList(new LinkedList());
        List<Long> objectIBOGroupOwnedObj = Collections.synchronizedList(new LinkedList());

        /**
         * Add User
         */
        objectIBOGroupOwnedObj.add(this.user.getUserID());
        objectIBOGroupOwnedObj.addAll(this.readGIDS);

        /**
         * Get all Objects which are owed by one of groups this user is member of
         */
        allRelationShips.parallelStream()
                .filter(rel -> rel.getType() == JEVisConstants.ObjectRelationship.OWNER)
                .forEach(rel -> {
                    try {

                        /**
                         * if the relationship is an ownership and belongs to one of the usergoup where the user is part of
                         */
                        if (this.readGIDS.contains(rel.getTo())) {
                            objectIBOGroupOwnedObj.add(rel.getFrom());
                        }
                    } catch (Exception ex) {
                        logger.error(ex);
                    }

                });
//        System.out.println("objectIBOGroupOwnedObj: " + objectIBOGroupOwnedObj.size());

        /**
         * Add all Public Objects
         */
        try {
            for (JsonObject publicObj : this.ds.getObjectTable().getAllPublicObjects()) {
                try {
                    if (!objectIBOGroupOwnedObj.contains(publicObj.getId())) {
                        objectIBOGroupOwnedObj.add(publicObj.getId());
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

//        System.out.println("objectIBOGroupOwnedObj: " + objectIBOGroupOwnedObj.size());
        List<JsonRelationship> debugOut = Collections.synchronizedList(new LinkedList());
        //.filter(rel -> rel.getType() == JEVisConstants.ObjectRelationship.OWNER)
        allRelationShips.parallelStream().forEach(rel -> {
//            System.out.println(rel + " " + objectIBOGroupOwnedObj.contains(rel.getFrom()) + "|" + objectIBOGroupOwnedObj.contains(rel.getTo()));
            if (objectIBOGroupOwnedObj.contains(rel.getFrom()) && objectIBOGroupOwnedObj.contains(rel.getTo()) && !list.contains(rel)) {
                list.add(rel);
            } else {
                debugOut.add(rel);
            }
        });
        logger.debug("relationships after filter: {}", list.size());
        return list;
    }

    public List<JsonRelationship> filterReadRelationships(List<JsonRelationship> rels) {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return rels;
        }

        List<JsonRelationship> list = Collections.synchronizedList(new ArrayList<>());
        List<Long> objectIDOFGroupOwenedObj = new LinkedList<>();

        //hmm what is with object which are public??

        rels.parallelStream().forEach(rel -> {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.readGIDS.contains(rel.getTo())) {
                objectIDOFGroupOwenedObj.add(rel.getFrom());
            }
        });


        try {
            for (JsonObject publicObj : this.ds.getObjectTable().getAllPublicObjects()) {
                objectIDOFGroupOwenedObj.add(publicObj.getId());
            }
        } catch (JEVisException ex) {
            ex.printStackTrace();
        }

        rels.parallelStream().forEach(rel -> {
            if (objectIDOFGroupOwenedObj.contains(rel.getFrom()) || objectIDOFGroupOwenedObj.contains(rel.getTo())) {
//            if ( readGIDS.contains(rel.getTo()) || readGIDS.contains(rel.getFrom())) {
                list.add(rel);
            }
        });
        return list;
    }

    /**
     * Returns {@code true} if the current user may read the given object.
     * <p>
     * Read access is granted if any of the following is true:
     * <ol>
     *   <li>The user is a sys-admin.</li>
     *   <li>The object is public.</li>
     *   <li>The object is the user's own object.</li>
     *   <li>The object has an OWNER relationship pointing to a group that the
     *       user belongs to (with READ membership).</li>
     * </ol>
     *
     * @param object the object to check
     * @return {@code true} if readable
     * @throws JEVisException with code 3021 if access is denied
     */
    public boolean canRead(JsonObject object) throws JEVisException {
        if (object == null) {
            return false;
        }

        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //Public object can be read by all
        if (object.getisPublic()) {
            return true;
        }

        //User has access to his own object
        if (this.user.getUserID() == object.getId()) {
            return true;
        }

        List<JsonRelationship> debug = this.ds.getGroupOwnerRelationships(object.getId());
        //check for group permissions
        for (JsonRelationship rel : this.ds.getGroupOwnerRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.readGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    /**
     * Returns all root objects accessible to the current user by scanning
     * ROOT-type relationships for the user's read groups.
     *
     * @return a list of root objects; may be empty
     */
    public List<JsonObject> getRoots() {
        List<JsonObject> roots = new ArrayList<>();
        this.readGIDS.parallelStream().forEach(id -> {
            try {
                for (JsonRelationship rel : this.ds.getRelationships(id)) {
                    if (rel.getFrom() == id && rel.getType() == JEVisConstants.ObjectRelationship.ROOT) {
                        roots.add(this.ds.getObject(rel.getTo()));
                    }
                }

            } catch (Exception ex) {

            }
        });

        return roots;
    }

    /**
     * Filters the given object list to those the current user may read.
     * Sys-admin users receive the unmodified list.
     *
     * @param objects the full object list to filter
     * @return a new list containing only readable objects
     */
    public List<JsonObject> filterList(List<JsonObject> objects) {
        System.out.println("filterList:");
        objects.forEach(jsonObject -> {
            // System.out.printf("jsonObject: " + jsonObject);
        });

        if (isSysAdmin()) {
            return objects;
        }

        List<JsonObject> list = new LinkedList<>();
        List<JsonRelationship> allRel = this.ds.getRelationships();

        System.out.println("ISReadOK:");
        for (JsonObject obj : objects) {
            //System.out.println("OB: " + obj.getId() + "-" + obj.getName() + " bool: " + isReadOK(allRel, obj));
            if (obj.getisPublic() || isReadOK(allRel, obj)) {
                list.add(obj);
            }
        }

        return list;
    }

    /**
     * Returns the object if the current user may read it, or {@code null} otherwise.
     *
     * @param object the object to check
     * @return {@code object} if readable, {@code null} if access is denied
     */
    public JsonObject filterObject(JsonObject object) {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return object;
        }

        List<JsonRelationship> allRel = this.ds.getRelationships();


        if (object.getisPublic() || isReadOK(allRel, object)) {
            return object;
        } else return null;
    }

    private boolean isReadOK(List<JsonRelationship> jsonRelationships, JsonObject obj) {
        /** user can read his user object and his groups**/

        if (this.readGIDS.contains(obj.getId()) || this.user.getUserID() == obj.getId()) {
            return true;
        }

        return jsonRelationships.parallelStream().anyMatch(jsonRelationship ->
                jsonRelationship.getType() == JEVisConstants.ObjectRelationship.OWNER
                        && jsonRelationship.getFrom() == obj.getId()
                        && this.readGIDS.contains(jsonRelationship.getTo()));

    }

    /**
     * Returns {@code true} if the current user has sys-admin privileges.
     *
     * @return {@code true} for sys-admins
     */
    public boolean isSysAdmin() {
        return this.user.isSysAdmin();
    }


    /**
     * Returns {@code true} if the current user may write to the given object.
     * Unlike {@link #canWrite}, this method swallows exceptions and returns
     * {@code false} instead.
     *
     * @param object the object to check
     * @return {@code true} if writable; {@code false} on any error or denial
     */
    public boolean canWriteWOE(JsonObject object) {
        try {
            return canWrite(object);
        } catch (Exception ex) {
            return false;
        }
    }

    public final List<String> exceptionClass = Arrays.asList("Data Notes", "User Data");

    /**
     * Returns {@code true} if the current user may create a child of type
     * {@code jevisclass} under {@code object}. Unlike {@link #canCreate(JsonObject, String)},
     * this method swallows exceptions and returns {@code false} instead.
     *
     * @param object     the parent object
     * @param jevisclass the JEVis class name of the object to create
     * @return {@code true} if creation is permitted; {@code false} on any error or denial
     */
    public boolean canCreateWOE(JsonObject object, String jevisclass) {
        try {
            return canCreate(object, jevisclass);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Returns {@code true} if the current user may write to the given object.
     *
     * @param object the object to check
     * @return {@code true} if writable
     * @throws JEVisException with code 3021 if access is denied
     */
    public boolean canWrite(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : this.ds.getRelationships(object.getId(), JEVisConstants.ObjectRelationship.OWNER)) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.writeGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        /**
         if (object.getJevisClass().equals("Data Notes") && canRead(object)) {
         logger.error("Can write because special rule");
         return true;
         }
         **/

        //no permission
        throw new JEVisException("permission denied", 3021);

    }

    /**
     * Returns {@code true} if the current user may create a child of type
     * {@code jevisClass} under {@code object}.
     * <p>
     * A special exception allows any user with execute permission to create
     * objects of the classes listed in {@link #exceptionClass}.
     *
     * @param object     the parent object
     * @param jevisClass the JEVis class name of the object to create
     * @return {@code true} if creation is permitted
     * @throws JEVisException with code 3021 if access is denied
     */
    public boolean canCreate(JsonObject object, String jevisClass) throws JEVisException {
        logger.debug("canCreate: {} ,'{}'", object, jevisClass);
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        /*
        for (JsonRelationship rel : this.ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.createGIDS.contains(rel.getTo())) {
                return true;
            }
        }
        */
        for (JsonRelationship rel : this.ds.getRelationships(object.getId(), JEVisConstants.ObjectRelationship.OWNER)) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.createGIDS.contains(rel.getTo())) {
                return true;
            }
        }


        /** Rule exception , to allow all users to create notes if they can see the Data Object **/
        if (exceptionClass.contains(jevisClass) && canExecuteWOE(object)) {
            logger.error("Can create because special rule");
            return true;
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    /**
     * Returns {@code true} if the current user may create a child object under
     * {@code object}, without regard to the target class.
     *
     * @param object the parent object
     * @return {@code true} if creation is permitted
     * @throws JEVisException with code 3021 if access is denied
     */
    public boolean canCreate(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : this.ds.getRelationships(object.getId(), JEVisConstants.ObjectRelationship.OWNER)) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.createGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    /**
     * Returns {@code true} if the current user may execute the given object.
     * Unlike {@link #canExecute}, this method swallows exceptions and returns
     * {@code false} instead.
     *
     * @param object the object to check
     * @return {@code true} if executable; {@code false} on any error or denial
     */
    public boolean canExecuteWOE(JsonObject object) {
        try {
            return canExecute(object);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * Returns {@code true} if the current user may execute the given object.
     *
     * @param object the object to check
     * @return {@code true} if execution is permitted
     * @throws JEVisException with code 3021 if access is denied
     */
    public boolean canExecute(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : this.ds.getRelationships(object.getId(), JEVisConstants.ObjectRelationship.OWNER)) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.exeGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    /**
     * Returns {@code true} if the current user may delete the given object.
     *
     * @param object the object to check
     * @return {@code true} if deletable
     * @throws JEVisException with code 3021 if access is denied
     */
    public boolean canDelete(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : this.ds.getRelationships(object.getId(), JEVisConstants.ObjectRelationship.OWNER)) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.deleteGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    /**
     * Returns {@code true} if the current user may delete the JEVis class definition
     * with the given name. Only sys-admin users may delete class definitions.
     *
     * @param jclass the class name
     * @return {@code true} for sys-admins; {@code false} otherwise
     */
    public boolean canDeleteClass(String jclass) {
        return this.user.isSysAdmin();
    }

    /**
     * Clears all cached permission lists. Call this when the rights manager is
     * no longer needed to assist garbage collection.
     */
    public void clear() {
        this.readGIDS.clear();
        this.deleteGIDS.clear();
        this.exeGIDS.clear();
        this.writeGIDS.clear();
        this.createGIDS.clear();
    }

    /**
     * Returns the group IDs that grant READ access for the current user.
     *
     * @return a mutable list of readable group IDs
     */
    public List<Long> getReadGIDS() {
        return readGIDS;
    }
}
