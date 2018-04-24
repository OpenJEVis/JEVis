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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 *
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

    public UserRightManagerForWS(SQLDataSource ds) {
        Logger logger = LogManager.getLogger(UserRightManagerForWS.class);
        logger.trace("Init UserRightManagerForWS for user");
        this.user = ds.getCurrentUser();
        this.ds = ds;
        init();
    }

    private void init() {
        //get user groups
        List<JsonRelationship> userRel = ds.getRelationships(ds.getCurrentUser().getUserObject().getId());
        for (JsonRelationship rel : userRel) {
            switch (rel.getType()) {
                case JEVisConstants.ObjectRelationship.MEMBER_READ:
                    readGIDS.add(rel.getTo());
                    break;
                case JEVisConstants.ObjectRelationship.MEMBER_WRITE:
                    writeGIDS.add(rel.getTo());
                    break;
                case JEVisConstants.ObjectRelationship.MEMBER_DELETE:
                    deleteGIDS.add(rel.getTo());
                    break;
                case JEVisConstants.ObjectRelationship.MEMBER_EXECUTE:
                    exeGIDS.add(rel.getTo());
                    break;
                case JEVisConstants.ObjectRelationship.MEMBER_CREATE:
                    createGIDS.add(rel.getTo());
                    break;
            }
        }
    }

    public List<JsonRelationship> filterReadRelationships(List<JsonRelationship> rels) {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return rels;
        }
        
        List<JsonRelationship> list = new ArrayList<>();
        List<Long> objectIDOFGroupOwenedObj = new LinkedList<>();
        
        //hmm what is with object wich are public??
        
        
        for (JsonRelationship rel : rels) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && readGIDS.contains(rel.getTo())) {
                objectIDOFGroupOwenedObj.add(rel.getFrom());
            }
        }
        System.out.println("User owned: "+objectIDOFGroupOwenedObj.size());

        for (JsonRelationship rel : rels) {
            if (objectIDOFGroupOwenedObj.contains(rel.getFrom()) ||  objectIDOFGroupOwenedObj.contains(rel.getTo())) {
//            if ( readGIDS.contains(rel.getTo()) || readGIDS.contains(rel.getFrom())) {
                list.add(rel);
            }
        }
        return list;
    }

    public boolean canRead(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //Public object can be read by all
        if (object.getisPublic()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && readGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    public List<JsonObject> getRoots() {
        List<JsonObject> roots = new ArrayList<>();
        for (Long id : readGIDS) {
            try {
                for (JsonRelationship rel : ds.getRelationships(id)) {
                    if (rel.getFrom() == id && rel.getType() == JEVisConstants.ObjectRelationship.ROOT) {
                        roots.add(ds.getObject(rel.getTo()));
                    }
                }

            } catch (Exception ex) {

            }
        }

        return roots;
    }

    public List<JsonObject> filterList(List<JsonObject> objects) {
        System.out.println("filterList: "+isSysAdmin()+ "   - "+objects.size());
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return objects;
        }
        System.out.println("isnotAdmin");
        
        List<JsonObject> list = new LinkedList<>();
        List<JsonRelationship> allRel = ds.getRelationships();
        
        for (JsonObject obj : objects) {
            if (obj.getisPublic()) {
                list.add(obj);
                continue;
            }
            if(isReadOK(allRel, obj)){
                list.add(obj);
            }
        }

        return list;
    }

    private boolean isReadOK(List<JsonRelationship> rels, JsonObject obj) {
        for (JsonRelationship rel : rels) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER 
                    && rel.getFrom() == obj.getId() 
                    && readGIDS.contains(rel.getTo())) {
                return true;
            }
        }
        return false;
    }

    public boolean isSysAdmin() {
        return user.isSysAdmin();
    }

    public boolean canWrite(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && writeGIDS.contains(rel.getTo())) {
                return true;
            }
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
        for (JsonRelationship rel : ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && createGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    public boolean canExecute(JsonObject object) throws JEVisException {
        //Sys Admin can read it all
        if (isSysAdmin()) {
            return true;
        }

        //check for group permissions
        for (JsonRelationship rel : ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && exeGIDS.contains(rel.getTo())) {
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
        for (JsonRelationship rel : ds.getRelationships(object.getId())) {
            if (rel.getType() == JEVisConstants.ObjectRelationship.OWNER && exeGIDS.contains(rel.getTo())) {
                return true;
            }
        }

        //no permission
        throw new JEVisException("permission denied", 3021);
    }

    public boolean canDeleteClass(String jclass) {
        return user.isSysAdmin();
    }

}
