/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
 *
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 *
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.application.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author fs
 */
public class JEVisObjectCache implements JEVisObject, Cached {

    private final JEVisObject otherDBObject;
    private final JEVisDataSourceCache cache;
    private List<JEVisAttribute> attributes = null;
    private List<JEVisObject> parents = null;
    private List<JEVisObject> children = null;
    private final List<CacheEventHandler> listeners = new ArrayList<>();
    private String name;
    private static final Logger logger = LogManager.getLogger(JEVisObjectCache.class);
    private final long id;
    private List<JEVisClass> allowedChildern;

    public JEVisObjectCache(JEVisDataSourceCache cache, JEVisObject orgObj) {
        otherDBObject = orgObj;
        this.cache = cache;
        id = orgObj.getID();

        addEventHandler(new CacheEventHandler() {
            @Override
            public void handle(CacheEvent event) {
                logger.trace("---------CacheObject Event: [{}] {}", getID(), event.getType());
                if (event.getType() == CacheEvent.TYPE.OBJECT_UPDATE || event.getType() == CacheEvent.TYPE.CLASS_CHILD_DELETE) {
                    try {
                        cache.updateObject(JEVisObjectCache.this);//will reload relationships
                        reload();

                    } catch (Exception ex) {
                        logger.catching(ex);
                    }
                }
            }
        });
    }

    @Override
    public void removeEventHandler(CacheEventHandler handler) {
        listeners.remove(handler);
    }

    @Override
    public void addEventHandler(CacheEventHandler handler) {
        listeners.add(handler);
    }

    @Override
    public void reload(RELOAD_MODE mode) {
        //TODO: reload .....
    }

    @Override
    public String getName() {
        return otherDBObject.getName();
//        if (name == null) {
//            name = otherDBObject.getName();;
//        }
//        return name;
    }

    @Override
    public void setName(String name) throws JEVisException {
        this.name = name;
        otherDBObject.setName(name);
    }

    @Override
    public Long getID() {
        return otherDBObject.getID();
    }

    @Override
    public JEVisClass getJEVisClass() throws JEVisException {
        return cache.getJEVisClass(getName());
    }

    @Override
    public String getJEVisClassName() throws JEVisException {
        return otherDBObject.getJEVisClassName();
    }

    private void loadLocation() throws JEVisException {
//        logger.trace("loadLocation() for {}", getID());
        if (parents == null) {
            parents = new ArrayList<>();
            children = new ArrayList<>();
            for (JEVisRelationship rel : cache.getRelationships()) {
//                logger.trace("is rel() for {}", rel);
                try {
                    if (rel.getType() == JEVisConstants.ObjectRelationship.PARENT) {
//                        logger.trace("is parentship");
                        if (rel.getStartID() == getID()) {
//                            logger.trace("Obj: {} Parent: {}", rel.getStartID(), rel.getEndID());
                            parents.add(cache.getObject(rel.getEndID()));
                        } else if (rel.getEndID() == getID()) {
//                            logger.trace("Obj: {} Child: {}", rel.getEndID(), rel.getStartID());
                            children.add(cache.getObject(rel.getStartID()));
                        }

                    }
                } catch (Exception ex) {
                    logger.error(ex);
                }
            }
        }
    }

    @Override
    public List<JEVisObject> getParents() throws JEVisException {
        loadLocation();
        return parents;
    }

    @Override
    public List<JEVisObject> getChildren() throws JEVisException {
//        logger.trace("GetChildren");
        loadLocation();
        return children;
    }

    @Override
    public List<JEVisObject> getChildren(JEVisClass type, boolean inherit) {
        try {
            List<JEVisClass> okclasses = new ArrayList<>();
            if (type != null) {
                okclasses.add(type);
                if (inherit) {
                    List<JEVisClass> heirs = type.getHeirs();
                    if (heirs != null && !heirs.isEmpty()) {
                        okclasses.addAll(heirs);
                    }

                }
            }

            List<JEVisObject> childrenObj = new LinkedList<JEVisObject>();

            for (JEVisObject child : getChildren()) {
//                if (okclasses.contains(child.getJEVisClassName())) {
                if (okclasses.contains(child.getJEVisClass())) {
                    childrenObj.add(child);
                }
            }

            Collections.sort(childrenObj);

            return childrenObj;
        } catch (Exception ex) {
            logger.catching(ex);
            return new ArrayList<>();
        }
    }

    @Override
    public List<JEVisAttribute> getAttributes() throws JEVisException {
        logger.trace("======getAttributes {}", getID());
        if (attributes == null) {
            logger.trace("======new", getID());
            attributes = cache.getAttributes(getID());
        }

        return attributes;
    }

    @Override
    public JEVisAttribute getAttribute(JEVisType type) throws JEVisException {
        return getAttribute(type.getName());
    }

    @Override
    public JEVisAttribute getAttribute(String type) throws JEVisException {
        logger.trace("getAttribute: {}", type);
        for (JEVisAttribute att : getAttributes()) {
            if (att.getName().equals(type)) {
                return att;
            }
        }

        return null;
    }

    @Override
    public boolean delete() throws JEVisException {
        return cache.deleteObject(getID());
    }

    @Override
    public void fireEvent(CacheEvent event) {
        for (CacheEventHandler listener : listeners) {
            try {
                listener.handle(event);
            } catch (Exception ex) {
                logger.catching(ex);
            }
        }
    }

    private void reload() throws JEVisException {
        logger.trace("reload ObjectCache: {}", getName());
        attributes = null;
        parents = null;
        children = null;
        name = null;
        allowedChildern = null;
        cache.reloadObject(this);
        logger.trace("fire updatedt event");
        fireEvent(new CacheObjectEvent(this, CacheEvent.TYPE.OBJECT_UPDATED));
    }

    @Override
    public JEVisObject buildObject(String name, JEVisClass type) throws JEVisException {
        JEVisObject newObj = otherDBObject.buildObject(name, type);

        JEVisObject newCacheObj = cache.getObject(newObj.getID());
        children.add(newCacheObj);
        fireEvent(new CacheObjectEvent(this, CacheEvent.TYPE.OBJECT_BUILD_CHILD));
//        reload();

        return newCacheObj;
    }

    @Override
    public JEVisObject getLinkedObject() {
        try {
            for (JEVisRelationship rel : getRelationships(JEVisConstants.ObjectRelationship.LINK, JEVisConstants.Direction.FORWARD)) {
//                logger.info("return link object: " + rel.getEndObject());
                return rel.getEndObject();
            }
        } catch (JEVisException ex) {
            logger.catching(ex);
        }
        return null;
    }

    @Override
    public JEVisRelationship buildRelationship(JEVisObject obj, int type, int direction) throws JEVisException {
        JEVisRelationship newRel;
        if (direction == JEVisConstants.Direction.FORWARD) {//from this to target
            newRel = cache.buildRelationship(getID(), obj.getID(), type);
        } else {
            newRel = cache.buildRelationship(obj.getID(), getID(), type);
        }

        return newRel;
    }

    @Override
    public void deleteRelationship(JEVisRelationship rel) throws JEVisException {
        cache.deleteRelationship(rel.getStartID(), rel.getEndID(), rel.getType());
    }

    @Override
    public List<JEVisRelationship> getRelationships() throws JEVisException {
        return cache.getRelationships(getID());
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type) throws JEVisException {
        List<JEVisRelationship> tmp = new LinkedList<>();
        for (JEVisRelationship rel : cache.getRelationships(getID())) {
            if (rel.isType(type)) {
                tmp.add(rel);
            }
        }
        return tmp;
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type, int direction) throws JEVisException {
        logger.trace("getRelationShip {} {} {}", getID(), type, direction);
        List<JEVisRelationship> tmp = new ArrayList<>();
        for (JEVisRelationship rel : cache.getRelationships(getID())) {
            try {

                if (rel.isType(type)) {
                    if (rel.getStartID() == getID() && direction == JEVisConstants.Direction.FORWARD) {
                        tmp.add(rel);
                    } else if (rel.getEndID() == getID() && direction == JEVisConstants.Direction.BACKWARD) {
                        tmp.add(rel);
                    }
                }
            } catch (Exception ex) {
                logger.catching(ex);
            }
        }
        return tmp;
    }

    @Override
    public List<JEVisClass> getAllowedChildrenClasses() throws JEVisException {
        logger.trace("{}", this.getID());
//        if (allowedChildern == null) {
        allowedChildern = new ArrayList<>();
        for (JEVisClass vp : getJEVisClass().getValidChildren()) {
            logger.trace("getAllowedChildrenClasses: {} total: {}", vp.getName(), getJEVisClass().getValidChildren().size());
            //Check if the class is Unique
            if (vp.isUnique()) {
                logger.trace("isUnique: {}", getChildren(vp, false).size());
                if (getChildren(vp, false).isEmpty()) {
                    allowedChildern.add(vp);
                } else {
                    logger.trace("'{}' Allready Exists, so ingnore", vp.getName());
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
        return cache;
    }

    @Override
    public void commit() throws JEVisException {
        otherDBObject.commit();
    }

    @Override
    public void rollBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(JEVisObject o) {
        return getID().compareTo(o.getID());
    }

    @Override
    public boolean isPublic() throws JEVisException{
        return otherDBObject.isPublic();
    }

    @Override
    public void setIsPublic(boolean ispublic) throws JEVisException {
        otherDBObject.setIsPublic(ispublic);
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
