/**
 * Copyright (C) 2009 - 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEConfig.
 *
 * JEConfig is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEConfig is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEConfig. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEConfig is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeconfig.plugin.classes;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisEvent;
import org.jevis.api.JEVisEventListener;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;

/**
 * Fake Root
 *
 * @author fs
 */
public class JEVisRootClass implements JEVisClass {

    private List<JEVisClass> _children = new ArrayList<JEVisClass>();
    private final JEVisDataSource _ds;
    private String _name = "Classes";

    public JEVisRootClass(JEVisDataSource ds) throws JEVisException {
        this._ds = ds;

//        List<JEVisClass> allClasses = _ds.getJEVisClasses();
//        List<JEVisClass> allClasses = JEConfig.getPreLodedClasses();//workaround, The login will preload Classes in the moment
        List<JEVisClass> allClasses = ds.getJEVisClasses();
        for (JEVisClass jclass : allClasses) {
            if (jclass.getInheritance() == null) {
                _children.add(jclass);
            }

        }

    }

    public JEVisRootClass(JEVisDataSource ds, List<JEVisClass> roots) throws JEVisException {
        _ds = ds;

        _children = roots;
    }

    @Override
    public boolean deleteType(String type) throws JEVisException {
        return true;
    }

    @Override
    public String getName() {
        return _name;
    }

    @Override
    public void setName(String name) throws JEVisException {
        _name = name;
    }

    @Override
    public void commit() throws JEVisException {
        ;
    }

    @Override
    public void rollBack() throws JEVisException {
        ;
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
    public BufferedImage getIcon() throws JEVisException {
        return null;
    }

    @Override
    public void setIcon(BufferedImage icon) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setIcon(File icon) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDescription() throws JEVisException {
        return "This Class is a fake and does not exist on the Server";
    }

    @Override
    public void setDescription(String discription) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisType> getTypes() throws JEVisException {
        return new ArrayList<>();
    }

    @Override
    public JEVisType getType(String typename) throws JEVisException {
        return null;
    }

    @Override
    public JEVisType buildType(String name) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisClass getInheritance() throws JEVisException {
        return null;
    }

    @Override
    public List<JEVisClass> getHeirs() throws JEVisException {
        return _children;
    }

    @Override
    public List<JEVisClass> getValidParents() throws JEVisException {
        return new ArrayList<>();
    }

    @Override
    public boolean isAllowedUnder(JEVisClass jevisClass) throws JEVisException {
        return false;
    }

    @Override
    public boolean isUnique() throws JEVisException {
        return true;
    }

    @Override
    public void setUnique(boolean unique) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete() throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisClassRelationship> getRelationships() throws JEVisException {
        return new ArrayList<>();
    }

    @Override
    public List<JEVisClassRelationship> getRelationships(int type) throws JEVisException {
        return new ArrayList<>();
    }

    @Override
    public List<JEVisClassRelationship> getRelationships(int type, int direction) throws JEVisException {
        return new ArrayList<>();
    }

    @Override
    public JEVisClassRelationship buildRelationship(JEVisClass jclass, int type, int direction) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deleteRelationship(JEVisClassRelationship rel) throws JEVisException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JEVisDataSource getDataSource() throws JEVisException {
        return _ds;
    }

    @Override
    public int compareTo(JEVisClass o) {
        return -1;
    }

    @Override
    public List<JEVisClass> getValidChildren() throws JEVisException {
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
