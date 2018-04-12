/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JECommons.
 *
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 *
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;

/**
 *
 * @author fs
 */
public class CacheManager {

    private static final CacheManager INSTANCE = new CacheManager();
    private final Map<Long, JEVisObject> objects = Collections.synchronizedMap(new HashMap<Long, JEVisObject>());
    private final Map<String, JEVisRelationship> relationships = Collections.synchronizedMap(new HashMap<String, JEVisRelationship>());
    private final Map<String, JEVisClass> classes = Collections.synchronizedMap(new HashMap<String, JEVisClass>());

    private CacheManager() {
    }

    public static CacheManager getInstance() {
        return CacheManager.INSTANCE;
    }

    public void addObject(JEVisObject obj) {
        synchronized (objects) {
            if (!objects.containsKey(obj.getID())) {
                System.out.println("==add object to cache: " + obj.getID());
                objects.put(obj.getID(), obj);
            } else {
                System.out.println("==object is already in cache: " + obj.getID());
            }
        }
    }

    public JEVisObject getObject(Long id) {
        synchronized (objects) {
            return objects.get(id);
        }
    }

    public boolean containsObject(Long id) {
        System.out.println("==cache size: " + objects.size());
        synchronized (objects) {
            return objects.containsKey(id);
        }
    }

    public void addJEVisClass(JEVisClass obj) throws JEVisException {
        synchronized (classes) {
            if (!classes.containsKey(obj.getName())) {
                classes.put(obj.getName(), obj);
            }
        }
    }

    public List<JEVisClass> getJEVisClass() {
        synchronized (classes) {
            return (ArrayList) ((ArrayList) classes).clone();
        }
    }

    public JEVisClass getJEVisClass(String id) {
        synchronized (classes) {
            return classes.get(id);
        }
    }

    public boolean containsClass(String id) {
        synchronized (classes) {
            return classes.containsKey(id);
        }
    }

    private String buildRelKey(Long fromObj, Long toObj, int type) {
        return fromObj + "-" + toObj + "-" + type;
    }

    public void addRelationship(JEVisRelationship obj) throws JEVisException {
        synchronized (relationships) {
            String key = buildRelKey(obj.getStartObject().getID(), obj.getEndObject().getID(), obj.getType());
            if (!relationships.containsKey(key)) {
                relationships.put(key, obj);
            }
        }
    }

    public JEVisRelationship getRelationship(Long fromObj, Long toObj, int type) {
        synchronized (relationships) {
            return relationships.get(buildRelKey(fromObj, toObj, type));
        }
    }

    public boolean containsRelationships(Long fromObj, Long toObj, int type) {
        synchronized (relationships) {
            return relationships.containsKey(buildRelKey(fromObj, toObj, type));
        }
    }

}
