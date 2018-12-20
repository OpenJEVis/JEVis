package org.jevis.jeconfig.application.jevistree;

/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEConfig.
 * <p>
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */

import org.jevis.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Fake Root
 *
 * @author fs
 */
public class JEVisRootObject implements JEVisObject {

    //    private final List<JEVisObject> _children;
//    private final JEVisDataSource _ds;
    private String _name = "Fake Root";
    private List<JEVisObject> children = new ArrayList<>();

    public JEVisRootObject() {
//        this._ds = ds;
//        _children = _ds.getRootObjects();
    }

//    public JEVisRootObject(JEVisDataSource ds, List<JEVisObject> roots) {
//        _ds = ds;
//
//        _children = roots;
//    }


    @Override
    public List<JEVisObject> getParents() throws JEVisException {
        return children;
    }

    @Override
    public List<JEVisObject> getChildren() throws JEVisException {
        return children;
    }

    @Override
    public List<JEVisObject> getChildren(JEVisClass type, boolean inherit) throws JEVisException {
        return children;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public void setName(String name) {
        _name = name;
    }

    @Override
    public Long getID() {
        return -1l;
    }

    @Override
    public JEVisClass getJEVisClass() {
        return null;
    }


    @Override
    public List<JEVisAttribute> getAttributes() {
        return new ArrayList<>();
    }

    @Override
    public JEVisAttribute getAttribute(JEVisType type) {
        return null;
    }

    @Override
    public JEVisAttribute getAttribute(String type) {
        return null;
    }

    @Override
    public boolean isAllowedUnder(JEVisObject otherObject) {
        return false;
    }

    @Override
    public boolean delete() {
        return false;
    }

    @Override
    public JEVisObject buildObject(String name, JEVisClass type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisObject getLinkedObject() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisRelationship buildRelationship(JEVisObject obj, int type, int direction) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteRelationship(JEVisRelationship rel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisRelationship> getRelationships() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisRelationship> getRelationships(int type, int direction) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisClass> getAllowedChildrenClasses() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisDataSource getDataSource() {
        return null;
    }

    @Override
    public void commit() throws JEVisException {
    }

    @Override
    public void rollBack() {
    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public int compareTo(JEVisObject o) {
        return -1;
    }

    @Override
    public String toString() {
        return "JEVisRootObject{ This Object is a fake and does not exist on the Server }";
    }

    //    @Override
//    public JEVisObject buildLink(String name, JEVisObject parent) throws JEVisException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//    }
    @Override
    public String getJEVisClassName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isPublic() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setIsPublic(boolean ispublic) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addEventListener(JEVisEventListener listener) {

    }

    @Override
    public void removeEventListener(JEVisEventListener listener) {

    }

    @Override
    public void notifyListeners(JEVisEvent event) {
    }


}
