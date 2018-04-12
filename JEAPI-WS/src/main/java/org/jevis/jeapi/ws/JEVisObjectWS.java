/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI-WS.
 *
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisEvent;
import org.jevis.api.JEVisEventListener;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;
import org.jevis.api.JEVisType;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonRelationship;

/**
 *
 * @author fs
 */
public class JEVisObjectWS implements JEVisObject {

    private JEVisDataSourceWS ds;
//    private String name = "";
//    private String jclassS = "";
//    private JEVisClass jclass = null;
//    private long id = -999;
//    private long parent = -999;
    private List<JEVisRelationship> relationships = null;
    private List<JEVisObject> parents = null;
    private List<JEVisObject> children = null;
    private List<JEVisAttribute> attributes = null;
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(JEVisObjectWS.class);
    private JsonObject json;

    public JEVisObjectWS(JEVisDataSourceWS ds, JsonObject json) {
//        logger.trace("New Object: {} {}", json.getId(), json.getJevisClass());
        this.ds = ds;
//        name = json.getName();
//        jclassS = json.getJevisClass();
//        id = json.getId();
        this.json = json;

    }

    @Override
    public String getName() {
        return json.getName();
    }

    @Override
    public void setName(String name) throws JEVisException {
        json.setName(name);
    }

    @Override
    public Long getID() {
        return json.getId();
    }

    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
//        if (jclass == null) {
//            jclass = ds.getJEVisClass(jclassS);
//        }
//        return jclass;

        return ds.getJEVisClass(json.getJevisClass());
    }

    @Override
    public List<JEVisObject> getParents() throws JEVisException {
        //TODO remove this local cache ant letzt the DataSource handel it? will bit a bit slower but saver?
        if (parents == null) {
            parents = new ArrayList<>();
            for (JEVisRelationship rel : getRelationships()) {
                if (rel.getType() == 1) {
                    if (rel.getStartObject().getID().equals(getID())) {
                        parents.add(rel.getEndObject());
                    }
                }
            }
        }

        return parents;

    }

    @Override
    public String getJEVisClassName() throws JEVisException {
        return json.getJevisClass();
    }

    @Override
    public List<JEVisObject> getChildren() throws JEVisException {
        if (children == null) {
            children = new ArrayList<>();
            for (JEVisRelationship rel : getRelationships()) {
                if (rel.getType() == 1 && rel.getEndObject().equals(this)) {
                    logger.trace("Add Child to {}: {}", getID(), rel.getStartObject());
                    children.add(rel.getStartObject());
                }
            }
        }
        logger.trace("Child.size: {}", children.size());
        return children;
    }

    @Override
    public List<JEVisObject> getChildren(JEVisClass jclass, boolean inherit) throws JEVisException {
        List<JEVisObject> filterLIst = new ArrayList<>();
        if (children == null) {
            getChildren();
        }
        for (JEVisObject obj : children) {
            //TODO: also get inherit

            if (obj.getJEVisClass().equals(jclass)) {
                filterLIst.add(obj);
            } else {
                Set<JEVisClass> inheritanceClasses = getInheritanceClasses(new HashSet<JEVisClass>(), obj.getJEVisClass());
                for (JEVisClass curClass : inheritanceClasses) {
                    if (curClass.equals(jclass)) {
                        filterLIst.add(obj);
                        break;
                    }
                }
            }
        }

        return filterLIst;
    }

    @Override
    public List<JEVisAttribute> getAttributes() throws JEVisException {
        if (attributes == null) {
            attributes = ds.getAttributes(this);
        }

        return attributes;
    }

    @Override
    public JEVisAttribute getAttribute(JEVisType type) throws JEVisException {

        for (JEVisAttribute att : getAttributes()) {
            if (att.getName().equals(type.getName())) {
                return att;
            }
        }
        return null;
    }

    @Override
    public JEVisAttribute getAttribute(String type) throws JEVisException {
        if (type == null) {
            return null;
        }

        for (JEVisAttribute att : getAttributes()) {
            if (att.getName().equalsIgnoreCase(type)) {
                return att;
            }
        }

        return null;
    }

    @Override
    public boolean delete() throws JEVisException {
        return ds.deleteObject(getID());
    }

    @Override
    public JEVisObject buildObject(String name, JEVisClass type) throws JEVisException {
        logger.trace("buildObject: {} {}", name, type.getName());
        JsonObject newJson = new JsonObject();
        newJson.setName(name);
        newJson.setJevisClass(type.getName());
        newJson.setParent(getID());

        JEVisObject newObj = new JEVisObjectWS(ds, newJson);
        newObj.commit();//hmm who commits.

        ds.getCurrentUser().reload();//will realod all relationships

        return newObj;
    }

    @Override
    public JEVisObject getLinkedObject() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisRelationship buildRelationship(JEVisObject obj, int type, int direction) throws JEVisException {
        return ds.buildRelationship(getID(), obj.getID(), direction);
    }

    @Override
    public void deleteRelationship(JEVisRelationship rel) throws JEVisException {
        ds.deleteRelationship(rel.getStartID(), rel.getEndID(), rel.getType());
    }

    @Override
    public List<JEVisRelationship> getRelationships() throws JEVisException {
        if (relationships == null) {
            relationships = new ArrayList<>();
            for (JsonRelationship rel : json.getRelationships()) {
                try {

                    JEVisRelationship newRel = new JEVisRelationshipWS(ds, rel);
                    relationships.add(newRel);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            //NOTE: we can not build the relationshipt in the constructore because
            //      we will end up in en endless loop(getObject->getRelationshio->getObject)
            //      with the cache. we could implement the cache here but but i dont like this.
            //      null the json after the we will not need it anymore
//            json = null;
        }

        return relationships;
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type) throws JEVisException {
        List<JEVisRelationship> filter = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            if (rel.isType(type)) {
                filter.add(rel);
            }
        }

        return filter;
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type, int direction) throws JEVisException {
        List<JEVisRelationship> filter = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            if (rel.isType(type)) {
                if (rel.getStartObject().equals(this) && direction == JEVisConstants.Direction.FORWARD) {
                    filter.add(rel);
                } else if (rel.getEndObject().equals(this) && direction == JEVisConstants.Direction.BACKWARD) {
                    filter.add(rel);
                }
            }
        }

        return filter;
    }

    @Override
    public List<JEVisClass> getAllowedChildrenClasses() throws JEVisException {

        ArrayList allowedChildern = new ArrayList<>();
        for (JEVisClass vp : getJEVisClass().getValidChildren()) {
            //Check if the class is Unique
            if (vp.isUnique()) {
                if (getChildren(vp, false).isEmpty()) {
                    allowedChildern.add(vp);
                } else {

                }
            } else {
                allowedChildern.add(vp);
            }
        }

        return allowedChildern;
    }

    @Override
    public boolean isAllowedUnder(JEVisObject otherObject) throws JEVisException {
        return getJEVisClass().isAllowedUnder(otherObject.getJEVisClass());
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return ds;
    }

    @Override
    public void commit() throws JEVisException {
//        ds.commitObject(this);
        try {
            Gson gson = new Gson();
            logger.trace("Commit: {}", gson.toJson(json));

            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH;

            if (json.getId() > 0) {//update existing
                resource += getID();
            }

            
            StringBuffer response = ds.getHTTPConnection().postRequest(resource, gson.toJson(json));
            //TODO: remove the realtaionship from the post json, like in the Webservice JSonFactory

            JsonObject newJson = gson.fromJson(response.toString(), JsonObject.class);
            logger.trace("new object ID: {}", newJson.getId());
            this.json = newJson;

        } catch (Exception ex) {
            logger.catching(ex);
            throw new JEVisException("Could not commit to server", 8236348, ex);
        }

    }

    @Override
    public void rollBack() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        //TODO hasChanged
        return true;
    }

    @Override
    public int compareTo(JEVisObject o) {
        return getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (o instanceof JEVisObject) {
                JEVisObject obj = (JEVisObject) o;
                if (obj.getID().equals(getID())) {
                    return true;
                }
            }
        } catch (Exception ex) {
            System.out.println("error, cannot compare objects");
            return false;
        }
        return false;
    }

    private Set<JEVisClass> getInheritanceClasses(Set<JEVisClass> hashSet, JEVisClass obj) {
        try {
            JEVisClass inheritance = obj.getInheritance();
            if (inheritance == null) {
                return hashSet;
            } else {
                hashSet.add(inheritance);
                return getInheritanceClasses(hashSet, inheritance);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(JEVisObjectWS.class.getName()).log(Level.SEVERE, null, ex);
        }
        return hashSet;
    }

    // TODO : implement listener support
    @Override
    public boolean isPublic() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setIsPublic(boolean ispublic) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addEventListener(JEVisEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeEventListener(JEVisEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void notifyListeners(JEVisEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
