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

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * @TODO: store/load cache local and load, needs and changelog on server side
 * @TODO: order an reload for an object/attribute/tec
 * @TODO: oder an complite reload
 * @TODO: implement change listeners
 * @author fs
 */
public class JEVisDataSourceCache implements JEVisDataSource {

    private final Logger logger = LogManager.getLogger(JEVisDataSourceCache.class);
    private final JEVisDataSource otherDS;
    private final Map<Long, JEVisObject> objects = Collections.synchronizedMap(new HashMap<Long, JEVisObject>());
    private final Map<Long, List<JEVisAttribute>> attributes = Collections.synchronizedMap(new HashMap<Long, List<JEVisAttribute>>());
    private final Map<String, List<JEVisType>> types = Collections.synchronizedMap(new HashMap<String, List<JEVisType>>());
    private final Map<String, JEVisClass> classes = Collections.synchronizedMap(new HashMap<String, JEVisClass>());
    private final Map<String, BufferedImage> classIcons = Collections.synchronizedMap(new HashMap<String, BufferedImage>());
    private final List<JEVisRelationship> relationships = Collections.synchronizedList(new ArrayList<JEVisRelationship>());
    private final List<JEVisObject> rootCache = Collections.synchronizedList(new ArrayList<JEVisObject>());
    private List<JEVisClassRelationship> classRelationships = Collections.synchronizedList(new ArrayList<JEVisClassRelationship>());

    private JEVisUser user;

    public JEVisDataSourceCache(JEVisDataSource ds) {
        this.otherDS = ds;
    }

    @Override
    public void init(List<JEVisOption> config) throws IllegalArgumentException {

    }

    @Override
    public List<JEVisOption> getConfiguration() {
        return otherDS.getConfiguration();
    }

    @Override
    public void setConfiguration(List<JEVisOption> config) {
        //TODO: check for parameters for this DS
        otherDS.setConfiguration(config);
    }

    @Override
    public JEVisClass buildClass(String name) throws JEVisException {
        logger.trace("buildClass: {}", name);
        JEVisClass newClass = otherDS.buildClass(name);
        JEVisClassCache newChachClass = new JEVisClassCache(this, newClass);
        classes.put(newChachClass.getName(), newChachClass);

        return newChachClass;
    }

    @Override
    public JEVisClassRelationship buildClassRelationship(String fromClass, String toClass, int type) throws JEVisException {
        logger.trace("buildClassRelationship: {} {} {}", fromClass, toClass, type);
        JEVisClassRelationship newRel = otherDS.buildClassRelationship(fromClass, toClass, type);
        JEVisClassRelationshipCache cachRel = new JEVisClassRelationshipCache(this, newRel);
        classRelationships.add(cachRel);

        if (cachRel.getType() == JEVisConstants.ClassRelationship.INHERIT) {
            logger.trace("Fire new ClassChild event");
            ((Cached) cachRel.getEnd()).fireEvent(new CacheObjectEvent(cachRel.getEnd(), CacheEvent.TYPE.CLASS_BUILD_CHILD));
        }
        ((Cached) cachRel.getStart()).fireEvent(new CacheObjectEvent(cachRel.getStart(), CacheEvent.TYPE.CLASS_UPDATE));
        ((Cached) cachRel.getEnd()).fireEvent(new CacheObjectEvent(cachRel.getEnd(), CacheEvent.TYPE.CLASS_UPDATE));

        return cachRel;

    }

    @Override
    public JEVisRelationship buildRelationship(Long fromObject, Long toObject, int type) throws JEVisException {
        logger.trace("buildRelationship: {} {} {}", fromObject, toObject, type);

        JEVisRelationship newRel = otherDS.buildRelationship(fromObject, toObject, type);
        JEVisRelationshipCache cachRel = new JEVisRelationshipCache(this, newRel);
        relationships.add(cachRel);

        if (cachRel.getType() == JEVisConstants.ObjectRelationship.LINK
                || cachRel.getType() == JEVisConstants.ObjectRelationship.PARENT) {
            logger.trace("Fire Object reload event");
            ((Cached) cachRel.getEndObject()).fireEvent(new CacheObjectEvent(cachRel.getEndObject(), CacheEvent.TYPE.OBJECT_UPDATE));
            ((Cached) cachRel.getStartObject()).fireEvent(new CacheObjectEvent(cachRel.getStartObject(), CacheEvent.TYPE.OBJECT_UPDATE));
        }

        getCurrentUser().reload();

        return cachRel;

    }

    @Override
    public JEVisObject buildLink(String name, JEVisObject parent, JEVisObject linkedObject) throws JEVisException {
        logger.trace("buildLink: {} {} {}", name, parent.getName(), linkedObject.getName());

        JEVisObject newObj = otherDS.buildLink(name, parent, linkedObject);

        return getObject(newObj.getID());

    }

    @Override
    public List<JEVisObject> getRootObjects() throws JEVisException {

        if (rootCache.isEmpty()) {
            List<JEVisObject> roots = otherDS.getRootObjects();
            for (JEVisObject obj : roots) {
                JEVisObjectCache objCaahe = new JEVisObjectCache(this, obj);
                synchronized (objects) {
                    if (!objects.containsKey(obj.getID())) {
                        objects.put(obj.getID(), objCaahe);
                        rootCache.add(objCaahe);
                    } else {
                        rootCache.add(objects.get(obj.getID()));
                    }
                }

            }
        }

        return rootCache;

    }

    public void updateObject(JEVisObject obj) {
        logger.trace("CacheDS.updateObject: {}", obj.getName());
        relationships.clear();
//        JEVisObject update = otherDS.getObject(obj.getID());
//        if (update == null) {
//            logger.trace("is null");
//            ((Cached) obj).fireEvent(new CacheObjectEvent(obj, CacheEvent.TYPE.OBJECT_DELETE));
//        } else {
//            logger.trace("update.name: {}", update.getName());
//            obj.setName(update.getName());
//
//            relationships.clear();
//        }

    }

    /**
     * @TODO: cache this? wahtt pappens if this is called more than once.
     * Objects may need an last Update timestamp.s
     *
     * @return
     * @throws JEVisException
     */
    @Override
    public List<JEVisObject> getObjects() throws JEVisException {
        logger.trace("getObjects()");
        Date start = new Date();
        if (!objects.isEmpty()) {
            return new ArrayList<>(objects.values());
        } else {

            List<JEVisObject> dbObj = otherDS.getObjects();
            logger.trace("getAllObject.count: {}", dbObj.size());
            for (JEVisObject obj : dbObj) {
                JEVisObjectCache cachObj = new JEVisObjectCache(this, obj);
                objects.put(cachObj.getID(), cachObj);

//                cachObj.addEventHandler(new CacheEventHandler() {
//                    @Override
//                    public void handle(CacheEvent event) {
//                        logger.trace("Child event - type: " + event.getType());
//                        if (event.getType() == CacheEvent.TYPE.OBJECT_RELOAD) {
//                            try {
//                                updateObject(cachObj);
//                            } catch (JEVisException ex) {
//                                logger.catching(ex);
//                            }
//                        }
//                    }
//                });
            }

            Date end = new Date();
            logger.trace("AllObjectDuration: {}ms", (end.getTime() - start.getTime()));
            return new ArrayList<>(objects.values());
        }
    }

    private void addAllHeirs(List<JEVisClass> classes, JEVisClass jevisClass) throws JEVisException {
        List<JEVisClass> heirs = jevisClass.getHeirs();
        if (!heirs.isEmpty()) {
            classes.addAll(heirs);
            for (JEVisClass heir : heirs) {
                addAllHeirs(classes, heir);
            }
        }

    }

    @Override
    public List<JEVisObject> getObjects(JEVisClass jevisClass, boolean addheirs) throws JEVisException {
        List<JEVisObject> tmpResult = new ArrayList();
//        List<JEVisClass> heir = jevisClass.getHeirs();
        List<JEVisClass> heir = new ArrayList<>();
        heir.add(jevisClass);
        if (addheirs) {
            addAllHeirs(heir, jevisClass);
        }

        for (JEVisObject obj : getObjects()) {
            if (heir.contains(obj.getJEVisClass())) {
                tmpResult.add(obj);
            }
        }

        return tmpResult;
    }

    @Override
    public JEVisObject getObject(Long id) throws JEVisException {
        synchronized (objects) {
            if (objects.containsKey(id)) {
                logger.error("Object [{}] is in cache",id);
                return objects.get(id);
            } else {
                logger.error("Object [{}] is NOT cache", id);
                JEVisObjectCache dsObj = new JEVisObjectCache(this, otherDS.getObject(id));
                objects.put(id, dsObj);
                return dsObj;
            }
        }
    }

    @Override
    public JEVisClass getJEVisClass(String name) throws JEVisException {
        synchronized (classes) {
            if (classes.containsKey(name)) {
                return classes.get(name);
            } else {
                getJEVisClasses();
                return classes.get(name);
            }
        }
    }

    @Override
    public List<JEVisClass> getJEVisClasses() throws JEVisException {
        Date start = new Date();
        Date load = new Date();
        Date transform = new Date();

        if (classes.isEmpty()) {
            start = new Date();
            logger.trace("Fill Class Cache");
            List<JEVisClass> dsClasses = otherDS.getJEVisClasses();
            load = new Date();
            for (JEVisClass cl : dsClasses) {
                classes.put(cl.getName(), new JEVisClassCache(this, cl));
            }
            transform = new Date();
        } else {
            logger.trace("Using class cache");
        }
        List<JEVisClass> tmp = new ArrayList<>(classes.values());
        Date listtrans = new Date();

//        logger.trace("------>time total: {} {} {}", (load.getTime() - start.getTime()), (transform.getTime() - start.getTime()), (listtrans.getTime() - start.getTime()));
        return tmp;

    }

    @Override
    public List<JEVisClassRelationship> getClassRelationships() throws JEVisException {
        if (classRelationships.isEmpty()) {
            classRelationships = Collections.synchronizedList(new ArrayList<JEVisClassRelationship>());
            for (JEVisClassRelationship rel : otherDS.getClassRelationships()) {
                classRelationships.add(new JEVisClassRelationshipCache(this, rel));
            }
        }
        return classRelationships;
    }

    @Override
    public List<JEVisClassRelationship> getClassRelationships(String jclass) throws JEVisException {
        List<JEVisClassRelationship> tmp = new ArrayList<>();
        for (JEVisClassRelationship rel : getClassRelationships()) {
            if (rel.getStartName().equals(jclass) || rel.getEndName().equals(jclass)) {
                tmp.add(rel);
            }
        }
        return tmp;
    }

    @Override
    public JEVisUser getCurrentUser() throws JEVisException {
        if (user == null) {
            user = otherDS.getCurrentUser();
        }
        return user;
    }

    public void reloadObject(JEVisObjectCache object) throws JEVisException {
        JEVisObject otherObj = otherDS.getObject(object.getID());

        //rmove old relationships, do we have to notify the other object? guess yes but has no pri for now
        synchronized (relationships) {
            List<JEVisRelationship> toRemove = new ArrayList<>();
            for (JEVisRelationship rel : getRelationships()) {
                if (rel.getStartID() == object.getID() || rel.getEndID() == object.getID()) {
                    toRemove.add(rel);
                }
            }

            relationships.removeAll(toRemove);
        }

        List<JEVisRelationship> newRel = otherObj.getRelationships();
        for (JEVisRelationship rel : newRel) {
            relationships.add(new JEVisRelationshipCache(this, rel));
        }

        //delete/remoad children of the herachie changed?
    }

    @Override
    public List<JEVisRelationship> getRelationships() throws JEVisException {
        if (relationships.isEmpty()) {
            logger.trace("Load Objectrelationshipts inti cache");
//            relationships = Collections.synchronizedList(new ArrayList<JEVisRelationship>());
            synchronized (relationships) {
                for (JEVisRelationship rel : otherDS.getRelationships()) {
                    relationships.add(new JEVisRelationshipCache(this, rel));
                }
            }
        }
        return relationships;
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type) throws JEVisException {
        List<JEVisRelationship> filter = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            if (rel.getType() == type) {
                filter.add(rel);
            }
        }
        return filter;
    }

    @Override
    public List<JEVisRelationship> getRelationships(long objectID) throws JEVisException {
        List<JEVisRelationship> filter = new ArrayList<>();
        for (JEVisRelationship rel : getRelationships()) {
            if (rel.getStartID() == objectID
                    || rel.getEndID() == objectID) {
                filter.add(rel);
            }
        }
        return filter;
    }

    @Override
    public boolean connect(String username, String password) throws JEVisException {
        return otherDS.connect(username, password);
    }

    @Override
    public boolean disconnect() throws JEVisException {
        return otherDS.disconnect();
    }

    @Override
    public boolean reconnect() throws JEVisException {
        return otherDS.reconnect();
    }

    @Override
    public JEVisInfo getInfo() {
        return otherDS.getInfo();
    }

    @Override
    public boolean isConnectionAlive() throws JEVisException {
        return otherDS.isConnectionAlive();
    }

    @Override
    public List<JEVisUnit> getUnits() {
        return otherDS.getUnits();
    }

    @Override
    public List<JEVisAttribute> getAttributes(long objectID) throws JEVisException {
//        logger.trace("++++++getAttributes {}", objectID);
//        synchronized (attributes) {
//            if (attributes.containsKey(objectID)) {
//                return attributes.get(objectID);
//            } else {
                logger.trace("Attributes for Object [{}] are NOT cache", objectID);
                List<JEVisAttribute> dbAtt = new ArrayList<>();
                for (JEVisAttribute att : otherDS.getAttributes(objectID)) {
//                    JEVisObject obj = getObject(objectID);
                    JEVisAttributeCache cacheAtt = new JEVisAttributeCache(this, objectID, att);
                    dbAtt.add(cacheAtt);
                    

                }
//                attributes.put(objectID, dbAtt);
                return dbAtt;
//            }
//        }
    }

    public JEVisType getType(String className, String typeName) throws JEVisException {
        for (JEVisType type : getTypes(className)) {
            if (type.getName().equals(typeName)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public List<JEVisType> getTypes(String className) throws JEVisException {

//        logger.trace("getType for: {}", className);
        if (types.containsKey(className)) {//TODO: Is NOT save, what     if one class is loadet, the rest will not follow!
            logger.error("Type for [{}] is in chace",className);
            return types.get(className);
        } else {
            logger.error("Type [{}] is NOT cache", className);
            List<JEVisType> tmp = new ArrayList<>();
            synchronized (types) {
                for (JEVisType t : otherDS.getTypes(className)) {
                    tmp.add(new JEVisTypeCache(this, t, className));
                }

            }
            types.put(className, tmp);
            return tmp;
        }

    }

    @Override
    public boolean deleteRelationship(Long fromObject, Long toObject, int type) throws JEVisException {
        for (JEVisRelationship rel : getRelationships(type)) {
            if (rel.getStartID() == fromObject && rel.getEndID() == toObject) {

                if (otherDS.deleteRelationship(fromObject, toObject, type)) {

                    getRelationships().remove(rel);
                    JEVisObject startObj = getObject(fromObject);
                    JEVisObject endObj = getObject(fromObject);
                    ((Cached) startObj).fireEvent(new CacheObjectEvent(startObj, CacheEvent.TYPE.OBJECT_UPDATE));
                    ((Cached) endObj).fireEvent(new CacheObjectEvent(endObj, CacheEvent.TYPE.OBJECT_UPDATE));
                    return true;
                }

            }
        }
        return false;

    }

    @Override
    public boolean deleteClassRelationship(String fromClass, String toClass, int type) throws JEVisException {
        for (JEVisClassRelationship rel : getClassRelationships(fromClass)) {
            if (rel.getStartName().equals(fromClass) && rel.getEndName().equals(toClass) && rel.getType() == type) {
                if (otherDS.deleteClassRelationship(fromClass, toClass, type)) {
                    getClassRelationships().remove(rel);
                    JEVisClass startObj = getJEVisClass(fromClass);
                    JEVisClass endObj = getJEVisClass(toClass);
                    ((Cached) startObj).fireEvent(new CacheObjectEvent(startObj, CacheEvent.TYPE.CLASS_UPDATE));
                    ((Cached) endObj).fireEvent(new CacheObjectEvent(endObj, CacheEvent.TYPE.CLASS_UPDATE));
                    return true;
                }

            }
        }
        return false;
    }

    @Override
    public boolean deleteClass(String jclass) throws JEVisException {
        JEVisClass delClass = getJEVisClass(jclass);
        if (delClass != null && otherDS.deleteClass(jclass)) {

            synchronized (classes) {
                for (JEVisClass heir : delClass.getHeirs()) {
                    //return success?!
                    deleteClass(heir.getName());
                }
            }

            classes.remove(jclass);

            ((Cached) delClass).fireEvent(new CacheObjectEvent(delClass, CacheEvent.TYPE.CLASS_DELETE));
            if (delClass.getInheritance() != null) {
                //Problem with recrusion? (delete parent, notify parent, load children -> msissing, delete parent, notify... etc)
                ((Cached) delClass.getInheritance()).fireEvent(new CacheObjectEvent(delClass, CacheEvent.TYPE.CLASS_CHILD_DELETE));
            }
            //TODO: warning if object from this type exist? (userright problem)

            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean deleteObject(long objectID) throws JEVisException {
        JEVisObject toDelete = getObject(objectID);
        List<JEVisObject> parents = toDelete.getParents();

        if (otherDS.deleteObject(objectID)) {
            objects.remove(objectID);
            if (toDelete instanceof Cached) {
                Cached ca = (Cached) toDelete;
                ca.fireEvent(new CacheObjectEvent(toDelete, CacheEvent.TYPE.OBJECT_DELETE));
            }
            for (JEVisObject parent : parents) {
                if (parent instanceof Cached) {
                    Cached ca = (Cached) parent;

                    ca.fireEvent(new CacheObjectEvent(parent, CacheEvent.TYPE.OBJECT_CHILD_DELETED));
                }
            }

            return true;
        } else {
            return false;
        }

    }

    @Override
    public void preload() {
        //performace workaround, we know the we will only use the JEVisDatasourceWs but 
        //the JEAPI does not provide the needet functions to handel the advanced features of the JEVisDatasourceWs. 

//        if (otherDS instanceof JEVisDataSourceWS) {
//            JEVisDataSourceWS tmpDS = (JEVisDataSourceWS) otherDS;
//            classIcons.putAll(tmpDS.getClassIcon());
//        }

//        getObjects();
    }

}
