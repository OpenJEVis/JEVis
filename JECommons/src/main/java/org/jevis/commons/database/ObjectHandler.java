/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.commons.database;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

/**
 *
 * @author broder
 */
public class ObjectHandler {

    private final JEVisDataSource dataSource;

    public ObjectHandler(JEVisDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void printObject(long l) {
        try {
            JEVisObject jevisObject = dataSource.getObject(l);
            System.out.println(jevisObject.getName());
            for (JEVisAttribute attr : jevisObject.getAttributes()) {
                Object attrValue = null;
                if (attr.getLatestSample() != null) {
                    attrValue = attr.getLatestSample().getValue();
                }
                System.out.println(attr.getName() + "," + attrValue);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ClassHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JEVisObject getFirstParent(JEVisObject calcObject) {
        JEVisObject parent = null;
        try {
            if (!calcObject.getParents().isEmpty()) {
                parent = calcObject.getParents().get(0);
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ObjectHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return parent;
    }

    public JEVisObject getObject(long l) {
        JEVisObject object = null;
        try {
            object = dataSource.getObject(l);
        } catch (JEVisException ex) {
            Logger.getLogger(ObjectHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return object;
    }

    public JEVisObject buildObject(JEVisObject parentObject, String typeName, String objectName) {
        JEVisObject childObject = null;
        try {
            JEVisClass jeVisClass = dataSource.getJEVisClass(typeName);
            childObject = parentObject.buildObject(objectName, jeVisClass);
        } catch (JEVisException ex) {
            Logger.getLogger(ObjectHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        return childObject;
    }

    public void deleteChildren(long l) {
        try {
            JEVisObject object = dataSource.getObject(l);
            List<JEVisObject> children = object.getChildren();
            for (JEVisObject obj : children) {
            }
        } catch (JEVisException ex) {
            Logger.getLogger(ObjectHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
