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
package org.jevis.commons.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.jevis.api.JEVisOption;
import org.jevis.commons.json.JsonOption;

/**
 * Basic implementation of the JEVisOption interface.
 *
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class BasicOption implements JEVisOption {

    private String value;
    private String key;
    private String description;
    private List<JEVisOption> options = new ArrayList<>();

    public BasicOption() {
    }

    public BasicOption(String key, String value, String description) {
        this.value = value;
        this.key = key;
        this.description = description;
    }

    public BasicOption(JsonOption jopt) {
        key = jopt.getKey();
        value = jopt.getValue();
        description = jopt.getDescription();
        if (jopt.getChildren() != null) {
            for (JsonOption child : jopt.getChildren()) {
                options.add(new BasicOption(child));
            }
        }

    }

    @Override
    public List<JEVisOption> getOptions() {
//        System.out.println("-- BasicOption.getChildren: " + options.size());
        return options;
    }

    @Override
    public void addOption(JEVisOption option, boolean overwrite) {
//        System.out.println("-- Add to " + key + "[" + this + "]   child: " + option.getKey() + "[" + option.getValue() + "]");
        if (!hasOption(option.getKey())) {
            options.add(option);
        } else if (overwrite) {
            options.add(option);
        }
    }

    @Override
    public JEVisOption getOption(String optionName) {
        for (JEVisOption opt : options) {
            if (opt.getKey().equalsIgnoreCase(optionName)) {
                return opt;
            }
        }

//        return null;
//        default return an emty option
        BasicOption newOpt = new BasicOption();
        newOpt.setKey(optionName);
        getOptions().add(newOpt);
        return newOpt;
    }

    @Override
    public boolean hasOption(String optionName) {
        for (JEVisOption opt : options) {
            if (opt.getKey().equalsIgnoreCase(optionName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public void setKey(String key) {
//        System.out.println("--setKey: " + key);
        this.key = key;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void removeOption(JEVisOption option) {

        this.options.remove(option);
    }

//    @Override
//    public String toString() {
//        return "JEVisOption{ Group: '" + group + "' Key: '" + key + "' value:'" + value + "' }";
//    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BasicOption other = (BasicOption) obj;
        if (!Objects.equals(this.key, other.key)) {
            return false;
        }
        return true;
    }

}
