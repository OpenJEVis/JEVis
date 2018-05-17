package org.jevis.iso.add;

import org.jevis.api.JEVisConstants;
import org.jevis.api.JEVisException;
import org.jevis.commons.ws.json.*;
import org.jevis.ws.sql.SQLDataSource;

import java.util.ArrayList;
import java.util.List;

public class Snippets {

    public static void getParent(SQLDataSource ds, JsonObject obj) {
        if (obj != null) {
            for (JsonRelationship rel : ds.getRelationships()) {
                if (rel.getFrom() == obj.getId()) {
                    if (rel.getType() == 1) {
                        obj.setParent(rel.getTo());
                    }
                }
            }
        }
    }

    public static String getValueString(JsonAttribute att, String defaultValue) {
        if (att != null && att.getLatestValue() != null) {
            return att.getLatestValue().getValue();
        } else {
            return defaultValue;
        }
    }

    public static String getChildName(SQLDataSource ds, String JEVisClass) throws JEVisException {
        for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects())) {
            if (obj.getJevisClass().equals(JEVisClass)) {
                return obj.getName();
            }
        }
        return null;
    }

    public static String getChildName(SQLDataSource ds, String JEVisClass, JsonObject Parent) throws JEVisException {
        if (Parent != null) {
            for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects())) {
                if (obj.getJevisClass().equals(JEVisClass)) {
                    return obj.getName();
                }
            }
        }
        return null;
    }

    public static Long getUniqueObjectId(SQLDataSource ds, String JEVisClass) throws JEVisException {
        for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects())) {
            if (obj.getJevisClass().equals(JEVisClass)) {
                return obj.getId();
            }
        }
        return null;
    }

    public static Long getChildId(SQLDataSource ds, String JEVisClass, JsonObject Parent) throws JEVisException {
        if (Parent != null) {
            for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects())) {
                if (obj.getJevisClass().equals(JEVisClass)) {
                    return obj.getId();
                }
            }
        }
        return null;
    }

    public static List<JsonObject> getChildren(SQLDataSource ds, String jevisClass, JsonObject parent) throws JEVisException {
        List<JsonObject> list = new ArrayList<>();
        if (parent != null) {
            for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects(jevisClass, false))) {
                getParent(ds, obj);
                if (obj.getParent() == parent.getId()) {
                    list.add(obj);
                }
            }
        }
        return list;
    }

    public static List<JsonObject> getAllChildren(SQLDataSource ds, JsonObject parent) throws JEVisException {
        List<JsonObject> list = new ArrayList<>();
        if (parent != null) {
            for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects())) {
                getParent(ds, obj);
                if (obj.getParent() == parent.getId()) {
                    list.add(obj);
                }
            }
        }
        return list;
    }

    public static List<JsonJEVisClass> getRootClasses(SQLDataSource ds) throws JEVisException {
        List<JsonJEVisClass> list = new ArrayList<>();
        for (JsonClassRelationship crel : ds.getClassRelationships()) {
            if (crel.getType() != JEVisConstants.ClassRelationship.INHERIT
                    && crel.getStart().equals(crel.getEnd())) list.add(ds.getJEVisClass(crel.getStart()));
        }
        return list;
    }

    public static List<JsonJEVisClass> getAllChildren(SQLDataSource ds, JsonJEVisClass parent) throws JEVisException {
        List<JsonClassRelationship> listClassRelationships = ds.getClassRelationships();
        List<JsonJEVisClass> list = new ArrayList<>();
        if (parent != null) {
            for (JsonClassRelationship crel : listClassRelationships) {
                if (crel.getType() == JEVisConstants.ClassRelationship.INHERIT && crel.getEnd().equals(parent.getName())) {
                    list.add(ds.getJEVisClass(crel.getStart()));
                }
            }
        }
        return list;
    }

    public static List<JsonObject> getChildren(SQLDataSource ds, String jevisClass) throws JEVisException {
        List<JsonObject> list = new ArrayList<>();
        for (JsonObject obj : ds.getUserManager().filterList(ds.getObjects(jevisClass, false))) {
            list.add(obj);
        }
        return list;
    }
}
