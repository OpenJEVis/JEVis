/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI-WS.
 * <p>
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.ws.json.JsonObject;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author fs
 */
public class JEVisObjectWS implements JEVisObject {
    private static final Logger logger = LogManager.getLogger(JEVisObjectWS.class);

    private final EventListenerList listeners = new EventListenerList();
    private JEVisDataSourceWS ds;
    private List<JEVisObject> parents = null;
    private List<JEVisObject> children = null;
    private JsonObject json;

    public JEVisObjectWS(JEVisDataSourceWS ds, JsonObject json) {
        this.ds = ds;
        this.json = json;
    }

    @Override
    public void addEventListener(JEVisEventListener listener) {
        listeners.add(JEVisEventListener.class, listener);
    }

    @Override
    public void removeEventListener(JEVisEventListener listener) {
        listeners.remove(JEVisEventListener.class, listener);
    }

    @Override
    public synchronized void notifyListeners(JEVisEvent event) {
//        if (event.getType() == JEVisEvent.TYPE.OBJECT_NEW_CHILD) {
//            children = null;
//        }

        for (JEVisEventListener l : listeners.getListeners(JEVisEventListener.class)) {
            l.fireEvent(event);
        }
    }

    @Override
    public String getName() {
        return json.getName();
    }

    @Override
    public void setName(String name) {
        json.setName(name);
    }

    @Override
    public Long getID() {
        return json.getId();
    }

    @Override
    public JEVisClass getJEVisClass() {
        return ds.getJEVisClass(json.getJevisClass());
    }

    @Override
    public List<JEVisObject> getParents() throws JEVisException {
        parents = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            if (rel.getType() == 1) {
                if (rel.getStartObject().getID().equals(getID())) {
                    parents.add(rel.getEndObject());
                }
            }
        }

        return parents;

    }

    @Override
    public String getJEVisClassName() {
        return json.getJevisClass();
    }

    @Override
    public List<JEVisObject> getChildren() throws JEVisException {
        children = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            try {
                if (rel.getType() == 1 && rel.getEndObject().equals(this)) {
                    children.add(rel.getStartObject());
                }
            } catch (NullPointerException ex) {

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

            JEVisClass oClass = obj.getJEVisClass();
            if (oClass != null && oClass.equals(jclass)) {
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
    public List<JEVisAttribute> getAttributes() {
        return getAttributesWS();
    }

    public List<JEVisAttribute> getAttributesWS() {
        return ds.getAttributes(getID());
    }

    @Override
    public JEVisAttribute getAttribute(JEVisType type) throws JEVisException {
        //TODO not uptimal, getAttribute() will not cached if we call all this in a loop we do N Webserive calls
        for (JEVisAttribute att : getAttributes()) {
            if (att.getName().equals(type.getName())) {
                return att;
            }
        }
        return null;
    }

    @Override
    public JEVisAttribute getAttribute(String type) {
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
    public boolean delete() {
        boolean delete = ds.deleteObject(getID());

        return delete;
    }

    @Override
    public JEVisObject buildObject(String name, JEVisClass type) throws JEVisException {
        logger.trace("buildObject: {} {}", name, type.getName());
        JsonObject newJson = new JsonObject();
        newJson.setName(name);
        newJson.setJevisClass(type.getName());
        newJson.setParent(getID());

        JEVisObject newObj = new JEVisObjectWS(ds, newJson);


        return newObj;
    }

    @Override
    public JEVisObject getLinkedObject() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisRelationship buildRelationship(JEVisObject otherObj, int type, int direction) throws JEVisException {
        JEVisRelationship rel;
        if (direction == JEVisConstants.Direction.FORWARD) {
            rel = ds.buildRelationship(getID(), otherObj.getID(), type);

            if (type == JEVisConstants.ObjectRelationship.PARENT) {
                otherObj.notifyListeners(new JEVisEvent(rel.getEndObject(), JEVisEvent.TYPE.OBJECT_NEW_CHILD));
            }

        } else {
            rel = otherObj.buildRelationship(this, type, JEVisConstants.Direction.FORWARD);
        }


        return rel;
    }

    @Override
    public void deleteRelationship(JEVisRelationship rel) throws JEVisException {
        ds.deleteRelationship(rel.getStartID(), rel.getEndID(), rel.getType());

        /**
         * Delete form cache and other objects
         */
        if (rel.getType() == JEVisConstants.ObjectRelationship.PARENT) {
            rel.getEndObject().notifyListeners(new JEVisEvent(rel.getEndObject(), JEVisEvent.TYPE.OBJECT_CHILD_DELETED));

        }

        notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_UPDATED));
    }

    @Override
    public List<JEVisRelationship> getRelationships() {
        return ds.getRelationships(getID());
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
            if (vp.isUnique()) {
                if (getChildren(vp, false).isEmpty()) {
                    allowedChildern.add(vp);
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
    public JEVisDataSource getDataSource() {
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

            boolean update = false;

            if (json.getId() > 0) {//update existing
                resource += getID();
                update = true;
            }

            StringBuffer response = ds.getHTTPConnection().postRequest(resource, gson.toJson(json));
            //TODO: remove the relationship from the post json, like in the Webservice JSonFactory

            JsonObject newJson = gson.fromJson(response.toString(), JsonObject.class);
            logger.trace("commit object ID: {} public: {}", newJson.getId(), newJson.getisPublic());
            this.json = newJson;

            ds.reloadRelationships();
            if (update) {
                notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_UPDATED));
            } else {
                if (!getParents().isEmpty()) {
                    getParents().get(0).notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_NEW_CHILD));
                }
            }

        } catch (Exception ex) {
            logger.catching(ex);
            throw new JEVisException("Could not commit to server", 8236348, ex);
        }

    }

    @Override
    public void rollBack() {
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
            logger.fatal(ex);
        }
        return hashSet;
    }

    @Override
    public boolean isPublic() {
        return json.getisPublic();
    }

    @Override
    public void setIsPublic(boolean ispublic) {
        json.setisPublic(ispublic);
    }

}
