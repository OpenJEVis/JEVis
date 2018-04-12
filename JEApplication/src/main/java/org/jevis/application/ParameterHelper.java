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
package org.jevis.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import org.jevis.api.JEVisOption;
import org.jevis.commons.cli.ConnectionEncoder;
import org.jevis.commons.config.BasicOption;
import org.jevis.commons.config.CommonOptions;

/**
 * This class helps with common task relatet to the Application.Parameters.
 *
 * @author Florian Simon
 */
public class ParameterHelper {

    /**
     * Converts parameters to an JEVisConfiguration object
     *
     * @TODO: make it more flexible and save to parse multible level of Options
     *
     * @param parameters
     * @return
     */
    public static List<JEVisOption> ParseJEVisConfiguration(Application.Parameters parameters) {
        List<JEVisOption> options = new ArrayList<>();

        //simple implementation to convert the parameters. Paramaerts can be seperated by dot.
        for (Map.Entry<String, String> entrySet : parameters.getNamed().entrySet()) {
            String[] keyGroup = entrySet.getKey().split("\\.", 2);

            if (keyGroup.length == 1) {//simple paramerter like "enabled"
                JEVisOption newOpt = new BasicOption();
                newOpt.setKey(keyGroup[0]);
                newOpt.setValue(entrySet.getValue());
                options.add(newOpt);
            } else if (keyGroup.length > 1) {//complex parameter with dop seperation like "datasource.host"
                //workaround solution an mask connection. Can be removed soon.
                if (keyGroup[1].equalsIgnoreCase(CommonOptions.DataSource.CONNECTION.getKey())) {
                    options.add(ConnectionEncoder.decode(entrySet.getValue()));
                    continue;
                }

                JEVisOption newOpt = new BasicOption();
                newOpt.setKey(keyGroup[0]);
                newOpt.setValue(entrySet.getValue());

                for (JEVisOption oldOpt : options) {
                    if (oldOpt.getKey().equals(keyGroup[0])) {
                        newOpt = oldOpt;
                    }
                }

                JEVisOption newOpt2 = new BasicOption();
                newOpt2.setKey(keyGroup[1]);
                newOpt2.setValue(entrySet.getValue());
                for (JEVisOption oldOpt : options) {
                    if (oldOpt.getKey().equals(keyGroup[0])) {
                        newOpt = oldOpt;
                    }
                }
                newOpt.addOption(newOpt2, true);

                if (!options.contains(newOpt)) {
                    options.add(newOpt);
                }

            }
        }
        return options;
    }

}
