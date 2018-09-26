/**
 * Copyright (C) 2017 Envidatec GmbH <info@envidatec.com>
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
package org.jevis.commons.cli;

import org.apache.commons.configuration.ConfigurationException;
import org.jevis.api.JEVisOption;
import org.jevis.commons.config.OptionFactory;
import org.jevis.commons.config.XMLConfigFileReader;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

/**
 *
 * @author Artur Iablokov
 */
public class ConfHelper {
    
    private static final String DOT = "\\.";

    public static Map<String, JEVisOption> ParseJEVisConfiguration(File file, String prefix, Map<String, JEVisOption> options) throws ConfigurationException {

        XMLConfigFileReader config = new XMLConfigFileReader(file);
        Map<String, String> conf = config.getValues(prefix);
        //conf.forEach((k, v) -> logger.info("Key: " + k + " Value: " + v));
        return ParseJEVisConfiguration(conf, options);
    }

    public static Map<String, JEVisOption> ParseJEVisConfiguration(Map<String, String> conf, Map<String, JEVisOption> options) {

        Map<String, String> treeMap = new TreeMap<>(conf);

        BiConsumer<String, String> consumer = (k, v) -> {
            String[] key = k.split(DOT);
            JEVisOption parent = null;
            for (int i = 0; i < key.length; i++) {
                String tmpKey = key[i];
                // option with this token not exist
                if (!options.containsKey(tmpKey)) {
                    //is last part of token name and may be first
                    if (i == key.length - 1) {
                        parent = OptionFactory.BuildOption(parent, tmpKey, v, "");
                        //is first part and not last
                        //not first and not last
                    } else {
                        parent = OptionFactory.BuildOption(parent, tmpKey, "", "");
                    }
                    options.put(tmpKey, parent);

                } // // option with this token exist
                else if (i == key.length - 1) {
                    options.get(tmpKey).setValue(v);
                    //is first part and not last
                } else {
                    parent = options.get(tmpKey);
                }
            }
        };

        treeMap.forEach(consumer);
        return options;
    }
}
