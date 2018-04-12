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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

/**
 * Static references for common CLI options
 *
 * NOTE: this is an prototype and not for productiv use
 *
 * @author Florian Simon
 */
public interface CommonCLIOptions {

//    public static Option HELP = new Option("h", "help", false, "Print help");
    public static Option HELP = OptionBuilder.withDescription("Print help")
            .isRequired(false)
            .withLongOpt("help")
            .create("h");

    public static Option CONFIG_FILE = OptionBuilder.withDescription("Configuration File")
            .isRequired(true)
            .withLongOpt("config")
            .create("c");

    public static Option LOG_FILE = new Option("lf", "logfile", true, "Set an logfile.");
    public static Option LOG_LEVEL = new Option("ll", "loglevel", true, "set the loglevel.");

    public interface JEVis {

        public interface DataSource {

            public static Option SECURITY = new Option("dssec", true, "Use an secure Connection");

            public static Option DS_CLASS = OptionBuilder.withArgName("JEVisDataSource Classname")
                    .hasArgs(1)
                    .isRequired(true)
                    .withDescription("Classname  of the DataSourcec e.g. 'org.jevis.api.sql.JEVisDataSourceSQL'")
                    .withLongOpt("dsclass")
                    .create("dsc");

            public static Option HOST = OptionBuilder.withArgName("Hostname")
                    .hasArgs(1)
                    .isRequired(false)
                    .withDescription("Hostname of the DataSource e.g. 'openjevis.org'")
                    .withLongOpt("dshost")
                    .create("dsh");

            public static Option PORT = OptionBuilder.withArgName("Port")
                    .hasArgs(1)
                    .isRequired(false)
                    .withDescription("Port of the DataSource e.g. '3306'")
                    .withLongOpt("dsport")
                    .create("dsp");

            public static Option USERNAME = OptionBuilder.withArgName("DataSource User")
                    .hasArgs(1)
                    .isRequired(false)
                    .withDescription("User of the DataSource e.g. 'Jon Doe'")
                    .withLongOpt("dspass")
                    .create("dsp");

            public static Option PASSWORD = OptionBuilder.withArgName("DataSource Password")
                    .hasArgs(1)
                    .isRequired(false)
                    .withDescription("Password of the DataSource e.g. 'admin123'")
                    .withLongOpt("dsuser")
                    .create("dsu");

        }

        public interface User {

            public static Option NAME = new Option("jevisuser", true, "Username  for the JEVis System");
            public static Option PASSWORD = new Option("jevispass", true, "Password  or the JEVis System");
        }

        public interface Provider {

            public static Option NAME = new Option("providername", true, "Name of the provider");
            public static Option LOGO = new Option("providerlogo", true, "URL to the logo of the provider");
        }

        public interface Login {

            public static Option SHOW_OPTIONS = new Option("loginshowoptions", true, "Enable/Disable if the user should cobnfigure the login setting.");

        }

        public interface Service {

            public static Option MODE = new Option("servicemode", true, "Configure the serice mode (single, service, complete )");

        }

    }

}
