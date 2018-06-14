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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.ws.json.JsonObject;

import javax.swing.event.EventListenerList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author fs
 */
public class JEVisObjectWS implements JEVisObject {

    private JEVisDataSourceWS ds;
    private List<JEVisObject> parents = null;
    private List<JEVisObject> children = null;
    private org.apache.logging.log4j.Logger logger = LogManager.getLogger(JEVisObjectWS.class);
    private JsonObject json;
    private Cache<String, List> attributeCache;
    private final EventListenerList listeners = new EventListenerList();

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
        System.out.println("notify events: " + getID() + "  " + event.getType().toString());
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
    public void setName(String name) throws JEVisException {
        json.setName(name);
    }

    @Override
    public Long getID() {
        return json.getId();
    }

    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
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
    public String getJEVisClassName() throws JEVisException {
        return json.getJevisClass();
    }

    @Override
    public List<JEVisObject> getChildren() throws JEVisException {
//        if (children == null) {
        children = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            try {
                if (rel.getType() == 1 && rel.getEndObject().equals(this)) {
                    children.add(rel.getStartObject());
                }
            } catch (NullPointerException ex) {

            }
        }
//        }
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
        //temp cache for attributes because a lot of clients call a obj.getAttribute(type) 
        if (attributeCache == null) {
            attributeCache = CacheBuilder.newBuilder()
                    .expireAfterWrite(3, TimeUnit.SECONDS)
                    .build();

        }
        try {
            List<JEVisAttribute> list = attributeCache.get("egal", new Callable<List>() {
                        @Override
                        public List call() throws Exception {
                            return getAttributesWS();
                        }
                    }
            );
            if (list == null) {
                return new ArrayList<>();
            } else {
                return list;
            }

        } catch (Exception ex) {
            logger.error(ex);
            return new ArrayList<>();
        }

    }

    public List<JEVisAttribute> getAttributesWS() throws JEVisException {
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
        System.out.println("delete WS object");
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
    public JEVisObject getLinkedObject() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisRelationship buildRelationship(JEVisObject otherObj, int type, int direction) throws JEVisException {
        JEVisRelationship rel;
        if (direction == JEVisConstants.Direction.FORWARD) {
            rel = ds.buildRelationship(getID(), otherObj.getID(), type);

            if (type == JEVisConstants.ObjectRelationship.PARENT) {
                System.out.println("Event for: " + rel.getEndObject() + " " + rel.getStartObject() + " " + otherObj.getID());
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
        if (rel.getType() == JEVisConstants.ObjectRelationship.PARENT) {
            rel.getEndObject().notifyListeners(new JEVisEvent(rel.getEndObject(), JEVisEvent.TYPE.OBJECT_CHILD_DELETED));

        }

        notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_UPDATED));
    }

    @Override
    public List<JEVisRelationship> getRelationships() throws JEVisException {
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
                System.out.println("Class.isUniqe: " + vp.getName());
                if (getChildren(vp, false).isEmpty()) {
                    System.out.println("Does exist so add");
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

            boolean update = false;

            if (json.getId() > 0) {//update existing
                resource += getID();
                update = true;
            }
            System.out.println("Update path: " + resource);

            StringBuffer response = ds.getHTTPConnection().postRequest(resource, gson.toJson(json));
            //TODO: remove the relationship from the post json, like in the Webservice JSonFactory

            JsonObject newJson = gson.fromJson(response.toString(), JsonObject.class);
            logger.trace("commit object ID: {} public: {}", newJson.getId(), newJson.getisPublic());
            this.json = newJson;

            if (update) {
                notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_UPDATED));
            } else {
                ds.reloadRelationships();

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

    @Override
    public boolean isPublic() {
        return json.getisPublic();
    }

    @Override
    public void setIsPublic(boolean ispublic) throws JEVisException {
        json.setisPublic(ispublic);
    }

}
