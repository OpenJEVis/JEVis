/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.config;

import org.jevis.api.JEVisOption;

/**
 * Factory to create JEVisOptions
 *
 * @author Florian Simon
 */
public class OptionFactory {

    /**
     * Build a new JEVisOption with the given parameters.
     *
     * @param parent Option group, null if the option has no parent
     * @param key Option key
     * @param value value
     * @param description short description for user interactions
     * @param required true if the parameter has to be set
     * @return
     */
    public static JEVisOption BuildOption(JEVisOption parent, String key, String value, String description) {
        JEVisOption opt = new BasicOption();
        opt.setDescription(description);
        opt.setValue(value);
        opt.setKey(key);
        if (parent != null) {
            parent.addOption(opt, true);
        }

        return opt;
    }

}
