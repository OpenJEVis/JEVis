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

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisEvent;
import org.jevis.api.JEVisEventListener;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;

/**
 * @deprecated 
 * @author fs
 */
public class JEVisClassCache implements JEVisClass, Cached {

    private JEVisDataSourceCache cache;
    private JEVisClass otherClass;
    private BufferedImage image;
    private final Logger logger = LogManager.getLogger(JEVisClassCache.class);
    private List<JEVisType> types;
    private BufferedImage icon = null;
    private List<JEVisClassRelationship> relationships;

    private List<JEVisClass> heirs;
    private List<JEVisClass> validParents;
    private List<JEVisClass> validchildren;
    private JEVisClass inheritance;
    private boolean hasInheritance = true;
    private final List<CacheEventHandler> listeners = new ArrayList<>();

    public JEVisClassCache(JEVisDataSourceCache cache, JEVisClass jclass) {
        this.otherClass = jclass;
        this.cache = cache;
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

    
    
    @Override
    public String getName() throws JEVisException {
        return otherClass.getName();
    }

    @Override
    public void setName(String name) throws JEVisException {
        otherClass.setName(name);
    }

    @Override
    public BufferedImage getIcon() throws JEVisException {
        if (icon == null) {
            
            icon = otherClass.getIcon();
            if (icon == null) {
                //fallback
//                icon = new BufferedImagenew Image(JEVisClassCache.class.getResourceAsStream());
                try {
                    icon = ImageIO.read(getClass().getResourceAsStream("/icons/1390343812_folder-open.gif"));
                } catch (Exception ex) {

                }
            }
        }
        return icon;
    }

    @Override
    public void setIcon(BufferedImage icon) throws JEVisException {
        logger.debug("SetIcon with Buffer");
        otherClass.setIcon(icon);
        this.icon = icon;
    }

    @Override
    public void setIcon(File icon) throws JEVisException {
        logger.debug("SetIcon with file");
        otherClass.setIcon(icon);
        try {
            setIcon(ImageIO.read(icon));
//            System.out.println("set icon from file: " + _icon.getWidth());
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(JEVisClassCache.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String getDescription() throws JEVisException {
        return otherClass.getDescription();//is is better to use getCache.getIcon for updates?
    }

    @Override
    public void setDescription(String discription) throws JEVisException {
        otherClass.setDescription(discription);
    }

    @Override
    public List<JEVisType> getTypes() throws JEVisException {
        if (types == null) {
            types = cache.getTypes(getName());
            //the new DB structure will use redundante information so we dont need the Inheritance
//            JEVisClass inher = getInheritance();
//            if (inher != null) {
//                logger.trace("add inherit types for: {}", getName());
//                types.addAll(inher.getTypes());
//            }
        }
        return types;
    }

    @Override
    public JEVisType getType(String typename) throws JEVisException {
        for (JEVisType t : getTypes()) {
            if (t.getName().equals(typename)) {

                return t;
            }
        }

        return null;
    }

    @Override
    public JEVisType buildType(String name) throws JEVisException {
        JEVisType otherType = otherClass.buildType(name);
        JEVisType cachedType = new JEVisTypeCache(cache, otherType, getName());
        types.add(cachedType);
        return cachedType;
    }

    @Override
    public JEVisClass getInheritance() throws JEVisException {

        if (inheritance == null && hasInheritance) {
//            logger.trace("getInheritance() [{}]", getName());
            for (JEVisClassRelationship rel : cache.getClassRelationships()) {
                if (rel.getType() == JEVisConstants.ClassRelationship.INHERIT
                        && rel.getStartName().equals(getName())) {
                    inheritance = cache.getJEVisClass(rel.getEndName());
//                    logger.trace("Inheritance found: {}", inheritance.getName());
                }
            }
            hasInheritance = false;
        }
        return inheritance;
    }

    @Override
    public List<JEVisClass> getHeirs() throws JEVisException {
        if (heirs == null) {
//            logger.trace("getHeirs() for: [{}]", getName());
            heirs = new ArrayList<>();

            for (JEVisClassRelationship rel : cache.getClassRelationships()) {
                if (rel.getType() == JEVisConstants.ClassRelationship.INHERIT
                        && rel.getEndName().equals(getName())) {
//                    logger.trace("Add heir: [{}] {} {}", getName(), rel.getEndName(), rel.getStartName());
                    heirs.add(cache.getJEVisClass(rel.getStartName()));
                }
            }
        }

        return heirs;
    }

    @Override
    public List<JEVisClass> getValidChildren() throws JEVisException {
//        if (validchildren == null) {
//            logger.trace("getValidParents() [{}] rel.count: {}", getName(), getRelationships().size());
        validchildren = new ArrayList<>();

        for (JEVisClassRelationship rel : getRelationships()) {
            try {
//                    logger.trace("rel: {}", rel);
                if (rel.isType(JEVisConstants.ClassRelationship.OK_PARENT)
                        && rel.getEndName().equals(getName())) {
                    logger.trace("---OK parent: [{}] {}", this.getName(), rel.getOtherClass(this).getName());
                    if (!validchildren.contains(rel.getOtherClass(this))) {
                        validchildren.add(rel.getOtherClass(this));
                        logger.trace("Add it!");
                    }
//                        logger.trace("Add2");
//                        validParents.addAll(rel.getOtherClass(this).getHeirs());

                }
            } catch (Exception ex) {
                logger.error("An JEClassRelationship had an error for '{}': {}", getName(), ex);
            }
        }

//            if (getInheritance() != null) {
//                validParents.addAll(getInheritance().getValidParents());
//            }
        Collections.sort(validchildren);
//        }

        return validchildren;
    }

    @Override
    public List<JEVisClass> getValidParents() throws JEVisException {
//        if (validParents == null) {
        logger.trace("getValidParents() [{}] rel.count: {}", getName(), getRelationships().size());
        validParents = new ArrayList<>();

        for (JEVisClassRelationship rel : getRelationships()) {
            try {
                if (rel.isType(JEVisConstants.ClassRelationship.OK_PARENT)
                        && rel.getStart().equals(this)) {

                    if (!validParents.contains(rel.getOtherClass(this))) {
                        validParents.add(rel.getOtherClass(this));
                    }
//                        logger.trace("Add2");
//                        validParents.addAll(rel.getOtherClass(this).getHeirs());

                }
            } catch (Exception ex) {
                logger.error("An JEClassRelationship had an error for '{}': {}", getName(), ex);
            }
        }

//            if (getInheritance() != null) {
//                validParents.addAll(getInheritance().getValidParents());
//            }
        Collections.sort(validParents);
//        }

        return validParents;

    }

    @Override
    public boolean isAllowedUnder(JEVisClass jevisClass) throws JEVisException {
        logger.trace("{}", getName());
        for (JEVisClass jclass : getValidParents()) {
            if (jclass.getName().equals(jevisClass.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUnique() throws JEVisException {
        return otherClass.isUnique();
    }

    @Override
    public void setUnique(boolean unique) throws JEVisException {
        otherClass.setUnique(unique);
    }

    @Override
    public boolean delete() throws JEVisException {
        //TODO delte from DataSource, remove this API function?!
//        return otherClass.delete();
        return cache.deleteClass(getName());
    }

    @Override
    public boolean deleteType(String type) throws JEVisException {
        for (JEVisClass heir : getHeirs()) {
            //do something if false...
            heir.deleteType(type);
        }
        boolean del = otherClass.deleteType(type);
        fireEvent(new CacheObjectEvent(this, CacheEvent.TYPE.CLASS_UPDATE));
        return del;
    }

    @Override
    public List<JEVisClassRelationship> getRelationships() throws JEVisException {
//        if (relationships == null) {
        relationships = cache.getClassRelationships(getName());

//        }
        return relationships;
    }

    @Override
    public List<JEVisClassRelationship> getRelationships(int type) throws JEVisException {
        List<JEVisClassRelationship> tmp = new ArrayList<>();

        for (JEVisClassRelationship cr : getRelationships()) {
            if (cr.isType(type)) {
                tmp.add(cr);
            }
        }

        return tmp;

    }

    @Override
    public List<JEVisClassRelationship> getRelationships(int type, int direction) throws JEVisException {
        List<JEVisClassRelationship> tmp = new ArrayList<>();

        for (JEVisClassRelationship cr : getRelationships(type)) {
            if (direction == JEVisConstants.Direction.FORWARD && cr.getStart().equals(this)) {
                tmp.add(cr);
            } else if (direction == JEVisConstants.Direction.BACKWARD && cr.getEnd().equals(this)) {
                tmp.add(cr);
            }
        }

        return tmp;
    }

    @Override
    public JEVisClassRelationship buildRelationship(JEVisClass jclass, int type, int direction) throws JEVisException {
        JEVisClass start = null;
        JEVisClass end = null;
        if (direction == JEVisConstants.Direction.FORWARD) {
            start = this;
            end = jclass;
        } else {
            start = jclass;
            end = this;
        }

        JEVisClassRelationship newRel = cache.buildClassRelationship(start.getName(), end.getName(), type);
        //TODO FireEvent

//        JEVisClassRelationship newRel = otherClass.buildRelationship(jclass, type, direction);
//        JEVisClassRelationshipCache relCache = new JEVisClassRelationshipCache(cache, newRel);
//        getRelationships().add(relCache);
//        jclass.getRelationships().add(relCache);
//        cache.getClassRelationships().add(relCache);
        return newRel;

    }

    @Override
    public void deleteRelationship(JEVisClassRelationship rel) throws JEVisException {
        otherClass.deleteRelationship(rel);
        getRelationships().remove(rel);
        cache.getClassRelationships().remove(rel);
        rel.getOtherClass(this).getRelationships().remove(rel);

    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return cache;
    }

    @Override
    public void commit() throws JEVisException {

        logger.trace("Commit()");
        otherClass.commit();
        fireEvent(new CacheObjectEvent(this, CacheEvent.TYPE.CLASS_UPDATE));
    }

    @Override
    public void rollBack() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public int compareTo(JEVisClass o) {
        try {
            return getName().compareTo(o.getName());
        } catch (JEVisException jex) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return otherClass.toString();
    }

    @Override
    public void removeEventHandler(CacheEventHandler handler) {
        listeners.remove(handler);
    }

    @Override
    public void addEventHandler(CacheEventHandler handler) {
//        try {
//            logger.trace("Add EventHandler to: {}", getName());
//        } catch (Exception ex) {
//
//        }
        listeners.add(handler);
    }

    @Override
    public void reload(RELOAD_MODE mode) {
        //too
    }

    @Override
    public void fireEvent(CacheEvent event) {
        try {
            logger.trace("fireEvent in {}", getName());
        } catch (Exception ex) {

        }
        if (event.getType() == CacheEvent.TYPE.CLASS_UPDATE) {
            image = null;
            types = null;
            icon = null;
            relationships = null;

            heirs = null;
            validParents = null;
            validchildren = null;
            inheritance = null;
        }

        for (CacheEventHandler listener : listeners) {
            listener.handle(event);
        }
    }

}
