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
package org.jevis.commons.cli;

import org.apache.commons.cli.Options;

/**
 * Factory to create the common JEVis application CLI options.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class CLIOptionFactory {

    /**
     * Build a minimal options set for a JEVis client
     *
     * @return all necessary options for a JEVis client.
     */
    public static Options BuildJEVisClient() {
        Options options = new Options();
        options.addOption(CommonCLIOptions.HELP);
        options.addOption(CommonCLIOptions.CONFIG_FILE);
//        options.addOption(StartOptions.JEVis.DataSource.DS_CLASS);
//        options.addOption(StartOptions.JEVis.DataSource.HOST);
//        options.addOption(StartOptions.JEVis.DataSource.PORT);
//        options.addOption(StartOptions.JEVis.DataSource.USERNAME);
//        options.addOption(StartOptions.JEVis.DataSource.PASSWORD);
//        options.addOption(StartOptions.JEVis.DataSource.SECURITY);

        return options;
    }

    /**
     * Build an options set for an typical JEVis service application
     *
     * @return all necessary options for a JEVis service.
     */
    public static Options BuildJEVisService() {
        Options options = BuildJEVisClient();
        options.addOption(CommonCLIOptions.JEVis.Service.MODE);

        return options;
    }

}
