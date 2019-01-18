/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of Launcher.
 * <p>
 * Launcher is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * Launcher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Launcher. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Launcher is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.sandbox;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.commons.cli.AbstractCliApp;
import org.jevis.commons.utils.Optimization;

/**
 * The Launcher is an minimalistic alarm notification tool for JEVis 3.0
 * <p>
 * TODO: Add the possibility to create template file for the emails which allow
 * much more control over the formatting of the email notification.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class Launcher extends AbstractCliApp {
    private static final Logger logger = LogManager.getLogger(Launcher.class);
    private static final String APP_INFO = "Sandbox";
    public static String KEY = "process-id";
    private final String APP_SERVICE_CLASS_NAME = "Sandbox";
    private final Command commands = new Command();

    private Launcher(String[] args, String appname) {
        super(args, appname);
    }

    public static void main(String[] args) {

        logger.info("-------Start Sandbox-------");
        Launcher app = new Launcher(args, APP_INFO);
        app.execute();
    }


    @Override
    protected void addCommands() {
        comm.addObject(commands);
    }

    @Override
    protected void handleAdditionalCommands() {

    }

    @Override
    protected void runService() {
        logger.error("RunService");
        while (true) {
            try {
                logger.error("-- Round round goes the bird");
                Optimization.getInstance().printStatistics();

                Thread.sleep(10000);
                logger.error("clear cache");
                ds.clearCache();
                System.gc();
                Optimization.getInstance().printStatistics();
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

    }

    @Override
    protected void runSingle(Long id) {
        logger.error("RunSingle");
    }

    @Override
    protected void runServiceHelp() {
        logger.error("RunService");
    }

    @Override
    protected void runComplete() {
        logger.error("RunComplete");
    }

}
