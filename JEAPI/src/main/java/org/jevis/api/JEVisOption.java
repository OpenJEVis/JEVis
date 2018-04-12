/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI.
 *
 * JEAPI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.api;

import java.util.List;

/**
 * Container to store some common configuration values. This will be used for
 * the configuration of JEVisDatasource, localization, MetaData and so on
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public interface JEVisOption {

    /**
     * Get a list with all children options
     *
     * @return
     */
    List<JEVisOption> getOptions();

    /**
     * returns a single option by name
     *
     * @param optionName
     * @return
     */
    JEVisOption getOption(String optionName);

    /**
     * returns if this option has a child with the given name,
     *
     * @param optionName
     * @return true if the option exists, false if not
     */
    boolean hasOption(String optionName);

    /**
     * Add a new child option to this option.
     *
     * @param option new child option
     * @param overwrite if true overwrite the already existing option.
     */
    void addOption(JEVisOption option, boolean overwrite);

    /**
     * Remove and option from this option.
     *
     * @param option
     */
    void removeOption(JEVisOption option);

    /**
     * returns the value for this option
     *
     * @return
     */
    String getValue();

    /**
     * Set the value for this option
     *
     * @param value
     */
    void setValue(String value);

    /**
     * return the key/name of this option
     *
     * @return
     */
    String getKey();

    /**
     * Set the key of this option
     *
     * @TODO: maybe this function is not save because the parent cannot check if
     * the open is already in use. Better use the constructor and add to check
     * this
     * @param key
     */
    void setKey(String key);

    /**
     * Returns an human readable descripion
     *
     * @return
     */
    String getDescription();

    /**
     * Set the human readable description
     *
     * @param description
     */
    void setDescription(String description);

   

}
