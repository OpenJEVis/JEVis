/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.jeconfig.sample;

import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisOption;

/**
 * Simple implementation of the JEVisOption to fake an root node for the GUI
 * Tree.
 *
 * @author Florian Simon
 */
public class JEVisOptionTreeRoot implements JEVisOption {

    private List<JEVisOption> children = new ArrayList<>();
    private JEVisAttribute att;

    public JEVisOptionTreeRoot(JEVisAttribute att) {
        this.att = att;
        for (JEVisOption opt : att.getOptions()) {
            children.add(opt);
        }

    }

    @Override
    public void removeOption(JEVisOption option) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<JEVisOption> getOptions() {
        return children;
    }

    @Override
    public JEVisOption getOption(String optionName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean hasOption(String optionName) {
        return !children.isEmpty();
    }

    @Override
    public void addOption(JEVisOption option, boolean overwrite) {
        //TODO: check if this oprion allready exists?
        children.add(option);
        System.out.println("Add option to: " + att.getName() + " " + option);
        att.addOption(option);
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public void setValue(String value) {
    }

    @Override
    public String getKey() {
        return "Options";
    }

    @Override
    public void setKey(String key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDescription() {
        return "Root Option";
    }

    @Override
    public void setDescription(String description) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
