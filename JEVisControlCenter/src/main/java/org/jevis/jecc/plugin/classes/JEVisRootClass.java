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
package org.jevis.jecc.plugin.classes;

import org.jevis.api.*;
import org.jevis.commons.i18n.I18n;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Fake Root
 *
 * @author fs
 */
public class JEVisRootClass implements JEVisClass {

    public static String _name = I18n.getInstance().getString("plugin.classes.title");
    private final JEVisDataSource _ds;
    private List<JEVisClass> _children = new ArrayList<JEVisClass>();
    //private String _name = "Classes";

    public JEVisRootClass(JEVisDataSource ds) throws JEVisException {
        this._ds = ds;

        List<JEVisClass> allClasses = ds.getJEVisClasses();
        for (JEVisClass jclass : allClasses) {
            if (jclass.getInheritance() == null) {
                _children.add(jclass);
            }

        }

    }

    public JEVisRootClass(JEVisDataSource ds, List<JEVisClass> roots) {
        _ds = ds;

        _children = roots;
    }

    @Override
    public boolean deleteType(String type) {
        return true;
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
    public String toString() {
        return "JEVisRootClass{ This Class is a fake and does not exist on the Server }";
    }

    @Override
    public BufferedImage getIcon() {
        return null;
    }

    @Override
    public void setIcon(File icon) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setIcon(BufferedImage icon) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDescription() {
        return "This Class is a fake and does not exist on the Server";
    }

    @Override
    public void setDescription(String discription) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisType> getTypes() {
        return new ArrayList<>();
    }

    @Override
    public JEVisType getType(String typename) {
        return null;
    }

    @Override
    public JEVisType buildType(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisClass getInheritance() {
        return null;
    }

    @Override
    public List<JEVisClass> getHeirs() {
        return _children;
    }

    @Override
    public List<JEVisClass> getValidParents() {
        return new ArrayList<>();
    }

    @Override
    public boolean isAllowedUnder(JEVisClass jevisClass) {
        return false;
    }

    @Override
    public boolean isUnique() {
        return true;
    }

    @Override
    public void setUnique(boolean unique) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisClassRelationship> getRelationships() {
        return new ArrayList<>();
    }

    @Override
    public List<JEVisClassRelationship> getRelationships(int type) {
        return new ArrayList<>();
    }

    @Override
    public List<JEVisClassRelationship> getRelationships(int type, int direction) {
        return new ArrayList<>();
    }

    @Override
    public JEVisClassRelationship buildRelationship(JEVisClass jclass, int type, int direction) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteRelationship(JEVisClassRelationship rel) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisDataSource getDataSource() {
        return _ds;
    }

    @Override
    public int compareTo(JEVisClass o) {
        return -1;
    }

    @Override
    public List<JEVisClass> getValidChildren() {
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
