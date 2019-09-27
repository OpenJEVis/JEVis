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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.ws.json.JsonObject;

import javax.swing.event.EventListenerList;
import java.io.IOException;
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
    private JsonObject json;

    public JEVisObjectWS(JEVisDataSourceWS ds, JsonObject json) {
        this.ds = ds;
        this.json = json;

    }

    public void update(JsonObject json) {
        this.json = json;
    }


    @Override
    public void addEventListener(JEVisEventListener listener) {
        this.listeners.add(JEVisEventListener.class, listener);
    }

    @Override
    public void removeEventListener(JEVisEventListener listener) {
        this.listeners.remove(JEVisEventListener.class, listener);
    }

    @Override
    public synchronized void notifyListeners(JEVisEvent event) {
        logger.trace("Object event[{}] listeners: {} event:", getID(), this.listeners.getListenerCount(), event.getType());
        for (JEVisEventListener l : this.listeners.getListeners(JEVisEventListener.class)) {
            l.fireEvent(event);
        }
    }

    @Override
    public String getName() {
        return this.json.getName();
    }

    @Override
    public void setName(String name) {
        this.json.setName(name);
    }

    @Override
    public Long getID() {
        return this.json.getId();
    }

    private boolean isLink() {
        return this.json.getJevisClass().equals("Link");
    }

    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
        if (isLink()) {
            JEVisObject linkedObject = getLinkedObject();
            if (linkedObject != null) {
                return linkedObject.getJEVisClass();
            }
        }
        return this.ds.getJEVisClass(this.json.getJevisClass());
    }

    @Override
    public List<JEVisObject> getParents() throws JEVisException {
        List<JEVisObject> parents = new ArrayList<>();
        try {
            for (JEVisRelationship rel : getRelationships()) {
                if (rel.getType() == 1) {
                    if (rel.getStartObject().getID().equals(getID())) {
                        if (rel.getEndObject() != null && !parents.contains(rel.getEndObject())) {
                            parents.add(rel.getEndObject());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.warn("Missing or unacceptable parent: {}", getID());
        }

        return parents;

    }

    @Override
    public String getJEVisClassName() {
        return this.json.getJevisClass();
    }

    @Override
    public List<JEVisObject> getChildren() throws JEVisException {
        List<JEVisObject> children = new ArrayList<>();
        try {
            for (JEVisRelationship rel : getRelationships()) {
                try {
                    Long id = rel.getEndID();
                    if ((rel.getType() == JEVisConstants.ObjectRelationship.PARENT) && (id.equals(getID()))) {
                        if (rel.getStartObject() != null && !children.contains(rel.getStartObject())) {
                            children.add(rel.getStartObject());
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            logger.warn("Could not get all children", ex);
        }
        logger.trace("Child.size: {}.{}", getID(), children.size());
        return children;
    }

    /**
     * TODO: this seems to not work properly needs testing
     *
     * @param jclass
     * @param inherit Include inherited classes
     * @return
     * @throws JEVisException
     */
    @Override
    public List<JEVisObject> getChildren(JEVisClass jclass, boolean inherit) throws JEVisException {
        List<JEVisObject> filterLIst = new ArrayList<>();
        for (JEVisObject obj : getChildren()) {
            try {

                JEVisClass oClass = obj.getJEVisClass();
                if (oClass != null && oClass.equals(jclass)) {
                    filterLIst.add(obj);
                } else if (oClass != null) {
                    Set<JEVisClass> inheritanceClasses = getInheritanceClasses(new HashSet<>(), oClass);
                    for (JEVisClass curClass : inheritanceClasses) {
                        if (curClass.equals(jclass)) {
                            filterLIst.add(obj);
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex);
            }
        }
        logger.debug("[{}] getChildren: \n{}\n", getID(), filterLIst);

        return filterLIst;
    }

    @Override
    public List<JEVisAttribute> getAttributes() {
        if (isLink()) {
            JEVisObject linkedObject = getLinkedObject();
            if (linkedObject != null) {
                return this.ds.getAttributes(linkedObject.getID());
            }
        }

        return this.ds.getAttributes(getID());
    }

//    public List<JEVisAttribute> getAttributesWS() {
//        return this.ds.getAttributes(getID());
//    }

    @Override
    public JEVisAttribute getAttribute(JEVisType type) throws JEVisException {
        //TODO not optimal, getAttribute() will not cached if we call all this in a loop we do N Webserive calls
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

        return this.ds.deleteObject(getID());
    }


    @Override
    public JEVisObject buildObject(String name, JEVisClass type) throws JEVisException {
        logger.trace("buildObject: {} {}", name, type.getName());
        JsonObject newJson = new JsonObject();
        newJson.setName(name);
        newJson.setJevisClass(type.getName());
        newJson.setParent(getID());


        return new JEVisObjectWS(this.ds, newJson);
    }

    @Override
    public JEVisObject getLinkedObject() {
        try {
            for (JEVisRelationship relationship : getRelationships(JEVisConstants.ObjectRelationship.LINK, JEVisConstants.Direction.FORWARD)) {
                try {
                    JEVisObject originalObj = relationship.getEndObject();
                    if (originalObj != null) {
                        return originalObj;
                    }
                    /** TODO: maybe return some spezial object of the user has not access to the otherObject **/
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public JEVisRelationship buildRelationship(JEVisObject otherObj, int type, int direction) throws JEVisException {
        JEVisRelationship rel;
        if (direction == JEVisConstants.Direction.FORWARD) {
            rel = this.ds.buildRelationship(getID(), otherObj.getID(), type);

            if (type == JEVisConstants.ObjectRelationship.PARENT) {
                otherObj.notifyListeners(new JEVisEvent(rel.getEndObject(), JEVisEvent.TYPE.OBJECT_NEW_CHILD, rel.getStartObject()));
            }

        } else {
            rel = otherObj.buildRelationship(this, type, JEVisConstants.Direction.FORWARD);
        }


        return rel;
    }

    @Override
    public void deleteRelationship(JEVisRelationship rel) throws JEVisException {
        this.ds.deleteRelationship(rel.getStartID(), rel.getEndID(), rel.getType());

        /**
         * Delete form cache and other objects
         */
        if (rel.getType() == JEVisConstants.ObjectRelationship.PARENT) {
            rel.getEndObject().notifyListeners(new JEVisEvent(rel.getEndObject(), JEVisEvent.TYPE.OBJECT_CHILD_DELETED, rel.getStartObject().getID()));

        }

        notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_UPDATED, this));
    }

    @Override
    public List<JEVisRelationship> getRelationships() {
        return this.ds.getRelationships(getID());
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
        List<JEVisClass> allowedChildren = new ArrayList<>();
        for (JEVisClass vp : getJEVisClass().getValidChildren()) {
            if (vp.isUnique()) {
                if (getChildren(vp, false).isEmpty()) {
                    allowedChildren.add(vp);
                }
            } else {
                allowedChildren.add(vp);
            }
        }

        return allowedChildren;
    }

    @Override
    public boolean isAllowedUnder(JEVisObject otherObject) throws JEVisException {
        boolean classIsAllowedUnderClass = getJEVisClass().isAllowedUnder(otherObject.getJEVisClass());
        boolean isDirectory = this.ds.getJEVisClass("Directory").getHeirs().contains(this.getJEVisClass());
        boolean isUnique = getJEVisClass().isUnique();
        boolean isAlreadyUnderParent = false;
        if (otherObject.getParents() != null) {
            for (JEVisObject silvering : otherObject.getParents().get(0).getChildren()) {
                try {
                    if (silvering.getJEVisClassName().equals(getJEVisClassName())) {
                        isAlreadyUnderParent = true;
                    }
                } catch (Exception ex) {

                }
            }
        }

        /**
         * first check if its allowed to be created under other object class
         */
        if (classIsAllowedUnderClass) {
            if (!isUnique) {
                /**
                 * if its not unique its always allowed to be created
                 */
                return true;
            } else /**
             *  if it is a directory and its of the same class of its parent its allowed to be created
             */if (!isAlreadyUnderParent) {
                /**
                 * if it is unique and not already created its allowed to be created
                 */
                return true;
            } else return isDirectory && otherObject.getJEVisClassName().equals(getJEVisClassName());
        }

        return false;
    }

    @Override
    public JEVisDataSource getDataSource() {
        return this.ds;
    }

    @Override
    public void commit() throws JEVisException {
        try {
            System.out.println("Object.commit()");
//            Gson gson = new Gson();
//            logger.trace("Commit: {}", gson.toJson(this.json));
//            Benchmark benchmark = new Benchmark();
            String resource = REQUEST.API_PATH_V1
                    + REQUEST.OBJECTS.PATH;

            boolean update = false;

            if (this.json.getId() > 0) {//update existing
                resource += getID();
//                resource += REQUEST.OBJECTS.OPTIONS.INCLUDE_RELATIONSHIPS;
//                resource += "true";
                update = true;
            }

//            StringBuffer response = ;
//            //TODO: remove the relationship from the post json, like in the Webservice JSonFactory

            JsonObject newJson = this.ds.getObjectMapper().readValue(this.ds.getHTTPConnection().postRequest(resource, this.ds.getObjectMapper().writeValueAsString(this.json)).toString(), JsonObject.class);
//            JsonObject newJson = gson.fromJson(response.toString(), JsonObject.class);
            logger.debug("commit object ID: {} public: {}", newJson.getId(), newJson.getisPublic());
            this.json = newJson;
//            benchmark.printBenchmarkDetail("After ws call");
//            this.ds.reloadRelationships();
            this.ds.reloadRelationships(this.json.getId());
//            benchmark.printBenchmarkDetail("After reloadRel");
            /** reload object to be sure all events will be handled and the cache is working correctly **/
            this.ds.addToObjectCache(this);
            if (update) {
                notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_UPDATED, this));
            } else {
                this.ds.reloadAttribute(this);
                if (!getParents().isEmpty()) {
                    try {
                        getParents().get(0).notifyListeners(new JEVisEvent(this, JEVisEvent.TYPE.OBJECT_NEW_CHILD, this));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }


//            benchmark.printBenchmarkDetail("done commit");
        } catch (JsonParseException ex) {
            throw new JEVisException("Json parse exception. Could not commit to server", 8236341, ex);
        } catch (JsonMappingException ex) {
            throw new JEVisException("Json mapping exception. Could not commit to server", 8236342, ex);
        } catch (JsonProcessingException ex) {
            throw new JEVisException("Json processing exception. Could not commit to server", 8236343, ex);
        } catch (IOException ex) {
            throw new JEVisException("IO exception. Could not commit to server", 8236344, ex);
        } catch (Exception ex) {
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

        return getID().compareTo(o.getID());
    }

    @Override
    public boolean equals(Object o) {
        try {
            if (o instanceof JEVisObject) {
                return ((JEVisObject) o).getID().equals(getID());
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
        return this.json.getisPublic();
    }

    @Override
    public void setIsPublic(boolean ispublic) {
        this.json.setisPublic(ispublic);
    }


    @Override
    public String toString() {
        return "JEVisObjectWS [ id: '" + getID() + "' name: '" + getName() + "' jclass: '" + getJEVisClassName() + "']";
    }
}
