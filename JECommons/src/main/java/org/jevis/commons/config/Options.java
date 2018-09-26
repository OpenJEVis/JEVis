/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisOption;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Florian Simon
 */
public class Options {
    private static final Logger logger = LogManager.getLogger(Options.class);

    public static String toString(List<JEVisOption> options) {
//        logger.info("Options.toString: ");
        String result = "";
        for (JEVisOption option : options) {
//            logger.info("option: " + option.getKey());
            result += toString(option, 0);
        }

        return result;
    }

    public static String toString(JEVisOption opt) {
        return toString(opt, 0);
    }

    private static String toString(JEVisOption opt, int level) {
        String prefix = "\n";
        for (int i = 0; i < level; i++) {
            prefix += "-";
        }
        prefix += "[" + opt.getKey() + "]='" + opt.getValue() + "'";

        if (opt.getOptions() != null && !opt.getOptions().isEmpty()) {
//            logger.info("options.toString: " + opt.getKey());
            int newLevel = level + 1;
            for (JEVisOption child : opt.getOptions()) {
                prefix += toString(child, newLevel);
            }
        }
//        logger.info("prefix: " + prefix);

        return prefix;

    }

    public static boolean hasOption(String key, JEVisOption parentOption) {
        return getFirstOption(key, parentOption) != null;
    }

    public static List<JEVisOption> getOptions(String searchKey, JEVisOption parentOption) {
        return getOptions(searchKey, parentOption, new ArrayList<JEVisOption>());
    }

    private static List<JEVisOption> getOptions(String searchKey, JEVisOption parentOption, List<JEVisOption> foundOptions) {
//        logger.info("Has it Option: " + searchKey);

//        logger.info("Option parent: " + parentOption + "   children: " + parentOption.getOptions());
        if (parentOption != null && parentOption.getOptions() != null) {
//            logger.info("o1");
            for (JEVisOption child : parentOption.getOptions()) {
//                System.out.print("o2| child: " + child.getKey());
                if (child.getKey().equalsIgnoreCase(searchKey)) {
                    foundOptions.add(child);
//                    logger.info(" =Found Option: " + child.getKey());
                } else {
//                    logger.info(" =NOT");
                }

                if (!child.getOptions().isEmpty()) {
                    getOptions(searchKey, child, foundOptions);
                }
            }
        } else {
            logger.warn("WARNING: Options are null");
        }
        return foundOptions;
    }

    public static JEVisOption getFirstOption(String searchKey, JEVisOption parentOption) {
        List<JEVisOption> foundOptions = getOptions(searchKey, parentOption, new ArrayList<JEVisOption>());
        if (foundOptions.isEmpty()) {
            return null;
        } else {
            return foundOptions.get(0);
        }

    }

}
