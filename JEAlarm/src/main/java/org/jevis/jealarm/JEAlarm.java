/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAlarm.
 *
 * JEAlarm is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAlarm is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAlarm. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAlarm is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jealarm;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.configuration.ConfigurationException;
import org.jevis.api.JEVisException;

/**
 * The JEAlarm is an minimalistic alarm notification tool for JEVis 3.0
 *
 * TODO: Add the posibility to create template file for the emails which allow
 * much mor control over the formating of the mail notification.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class JEAlarm {

    final static Option help = new Option("h", "help", true, "Print this message.");
    final static Option configFile = new Option("c", "config", true, "Set the configuration file");
    final static Options options = new Options();

    public static void main(String[] args) throws ParseException, ConfigurationException {
        options.addOption(configFile);
        options.addOption(help);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd = parser.parse(options, args);

        HelpFormatter formatter = new HelpFormatter();

        if (cmd.hasOption(help.getLongOpt())) {
            formatter.printHelp("JEAlarm 1.0", options);
        }

        if (cmd.hasOption(configFile.getLongOpt())) {
            Config config = new Config(cmd.getOptionValue(configFile.getLongOpt()));
            try {
                AlarmHandler ah = new AlarmHandler(config);
                for (Alarm alarm : config.getAlarms()) {
                    ah.checkAlarm(alarm);
                }
            } catch (JEVisException ex) {
                Logger.getLogger(JEAlarm.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            System.out.println("Missing configuration file");
            formatter.printHelp("JEAlarm 1.0  2014-12-18", options);
        }

    }
}
