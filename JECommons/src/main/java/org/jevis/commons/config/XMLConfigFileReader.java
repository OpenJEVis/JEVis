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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

/**
 * The XMLConfigFilereader can read common JEVis XML-configuration files.
 *
 * @author Florian Simon
 */
public class XMLConfigFileReader {

    private XMLConfiguration xmlConfig;

    /**
     * Create an new XMLConfigFileReader with the given XML-file.
     *
     * @param file
     * @throws ConfigurationException
     */
    public XMLConfigFileReader(File file) throws ConfigurationException {

        xmlConfig = new XMLConfiguration(file);
    }

    /**
     * Get All Value-Key-pairs from a prefix as a Map
     *
     * @param prefix
     * @return
     */
    public Map<String, String> getValues(String prefix) {
        Map<String, String> map = new HashMap();

        for (Iterator<String> iterator = xmlConfig.getKeys(prefix); iterator.hasNext();) {
            String nextKey = iterator.next();
            map.put(nextKey.replaceAll(prefix, ""), xmlConfig.getString(nextKey));
        }

        return map;

    }

}
