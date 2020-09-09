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
package org.jevis.ws.sql;

import jersey.repackaged.com.google.common.collect.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author fs
 */
public class UserRightManagerForWS {

    private final JEVisUserNew user;
    private SQLDataSource ds;
    private List<Long> readGIDS = new ArrayList<>();
    private List<Long> createGIDS = new ArrayList<>();
    private List<Long> deleteGIDS = new ArrayList<>();
    private List<Long> exeGIDS = new ArrayList<>();
    private List<Long> writeGIDS = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(UserRightManagerForWS.class);

    public UserRightManagerForWS(SQLDataSource ds) {
//        Logger logger = LogManager.getLogger(UserRightManagerForWS.class);
        logger.trace("Init UserRightManagerForWS for user");
        this.user = ds.getCurrentUser();
        this.ds = ds;
        init();
    }

    private void init() {
        //get user groups
        List<JsonRelationship> userRel = this.ds.getRelationships(this.ds.getCurrentUser().getUserObject().getId());
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
    }

    /**
     * @param allRelationShips
     * @return
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

        //check for group permissions

        for (JsonRelationship rel : this.ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.readGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

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

    public List<JsonObject> filterList(List<JsonObject> objects) {
        if (isSysAdmin()) {
            return objects;
        }

        List<JsonObject> list = new LinkedList<>();
        List<JsonRelationship> allRel = this.ds.getRelationships();

        for (JsonObject obj : objects) {
            if (obj.getisPublic() || isReadOK(allRel, obj)) {
                list.add(obj);
            }
        }

        return list;
    }

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

    public boolean isSysAdmin() {
        return this.user.isSysAdmin();
    }


    public boolean canWriteWOE(JsonObject object) {
        try {
            return canWrite(object);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean canWrite(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : this.ds.getRelationships(object.getId())) {
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

    public boolean canCreateWOE(JsonObject object, String jevisclass) {
        try {
            return canCreate(object, jevisclass);
        } catch (Exception ex) {
            return false;
        }
    }

    public final List<String> exceptionClass = Lists.newArrayList(new String[]{"Data Notes","User Data"});

    public boolean canCreate(JsonObject object, String jevisClass) throws JEVisException {
        logger.error("canCreate: {} ,'{}'", object, jevisClass);
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : this.ds.getRelationships(object.getId())) {
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

    public boolean canCreate(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : this.ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.createGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    public boolean canExecuteWOE(JsonObject object) {
        try {
            return canExecute(object);
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean canExecute(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : this.ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.exeGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    public boolean canDelete(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : this.ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && this.deleteGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    public boolean canDeleteClass(String jclass) {
        return this.user.isSysAdmin();
    }

    public void clear() {
        this.readGIDS.clear();
        this.deleteGIDS.clear();
        this.exeGIDS.clear();
        this.writeGIDS.clear();
        this.createGIDS.clear();
    }

}
