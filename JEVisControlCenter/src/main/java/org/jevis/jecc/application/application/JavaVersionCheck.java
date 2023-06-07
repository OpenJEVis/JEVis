/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEApplication.
 * <p>
 * JEApplication is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation in version 3.
 * <p>
 * JEApplication is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEApplication. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEApplication is part of the OpenJEVis project, further project information
 * are published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jecc.application.application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Simplistic check for the java Version. The Envidatec GUI need minumim JAVA
 * 1.7.55
 *
 * @author fs
 */
public class JavaVersionCheck {
    private static final Logger logger = LogManager.getLogger(JavaVersionCheck.class);

    boolean versionK = false;

    public JavaVersionCheck() {

        String version = System.getProperty("java.version");
        logger.info("Your JAVA Version: {}", version);
        String[] split = version.split("\\.");
        int first = Integer.parseInt(split[0]);
        int majorV = Integer.parseInt(split[1]);

        // Version 2.x is ok
        if (first > 1) {
            versionK = true;
        }
        // Version 1.8_x is ok
        else if (majorV > 7) {
            versionK = true;
        } else {
            // Version newer 1.7_55 is ok
            int patch = Integer.parseInt((split[2].split("_")[1]).split("-")[0]);

            if (majorV == 7 && patch >= 55) {
                versionK = true;
            }
        }

        if (!versionK) {
            JOptionPane.showMessageDialog(null,
                    "JEVis needs JAVA version 1.7.55 or newer.\n",
                    "JAVA Version Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public boolean isVersionOK() {
        return versionK;
    }

}
