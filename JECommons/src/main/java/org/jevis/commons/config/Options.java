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
import org.jevis.api.JEVisOption;

/**
 *
 * @author Florian Simon
 */
public class Options {

    public static String toString(List<JEVisOption> options) {
//        System.out.println("Options.toString: ");
        String result = "";
        for (JEVisOption option : options) {
//            System.out.println("option: " + option.getKey());
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
//            System.out.println("options.toString: " + opt.getKey());
            int newLevel = level + 1;
            for (JEVisOption child : opt.getOptions()) {
                prefix += toString(child, newLevel);
            }
        }
//        System.out.println("prefix: " + prefix);

        return prefix;

    }

    public static boolean hasOption(String key, JEVisOption parentOption) {
        if (getFirstOption(key, parentOption) != null) {
            return true;
        } else {
            return false;
        }
    }

    public static List<JEVisOption> getOptions(String searchKey, JEVisOption parentOption) {
        return getOptions(searchKey, parentOption, new ArrayList<JEVisOption>());
    }

    private static List<JEVisOption> getOptions(String searchKey, JEVisOption parentOption, List<JEVisOption> foundOptions) {
//        System.out.println("Has it Option: " + searchKey);

//        System.out.println("Option parent: " + parentOption + "   children: " + parentOption.getOptions());
        if (parentOption != null && parentOption.getOptions() != null) {
//            System.out.println("o1");
            for (JEVisOption child : parentOption.getOptions()) {
//                System.out.print("o2| child: " + child.getKey());
                if (child.getKey().equalsIgnoreCase(searchKey)) {
                    foundOptions.add(child);
//                    System.out.println(" =Found Option: " + child.getKey());
                } else {
//                    System.out.println(" =NOT");
                }

                if (!child.getOptions().isEmpty()) {
                    getOptions(searchKey, child, foundOptions);
                }
            }
        } else {
            System.out.println("WARING: Options are null");
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
