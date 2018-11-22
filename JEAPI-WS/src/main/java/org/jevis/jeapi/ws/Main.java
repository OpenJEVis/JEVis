/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JEAPI-WS.
 * <p>
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;

/**
 * Main Class for testing purpose
 *
 * @author fs
 */
public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws JEVisException {
        JEVisDataSource ds = new JEVisDataSourceWS();
        ds.init(null);
        ds.connect("Sys Admin", "OpenJEVis2016");

        logger.info("Test#0");
        logger.info("User: " + ds.getCurrentUser().getFirstName() + " " + ds.getCurrentUser().getLastName());

        logger.info("Test#1");
        ds.getObject(1l);

        logger.info("TEST#ClassIcon");
        ds.getJEVisClass("Building").getIcon();

        logger.info("Test#2");
        ds.getJEVisClass("Data");

        logger.info("Test#3");
        ds.getJEVisClass("Report Link");

//
//        logger.info("Test#4");
//        ds.getJEVisClasses();
//
        logger.info("Test#5");
//        logger.info(ds.getObject(5270l).getAttribute("KVA").getPrimitiveType());

        logger.info("Test#6");
//        logger.info(ds.getRootObjects());

        logger.info("Test#7");
        for (JEVisObject obj : ds.getObjects()) {
//            logger.info("obj: " + obj.getID());
        }

        logger.info("Test#8");
        for (JEVisRelationship rel : ds.getRelationships()) {
//            logger.info("Rel: " + rel);
        }

        logger.info("Test#9");
        for (JEVisClassRelationship rel : ds.getClassRelationships()) {
//            logger.info("Rel: " + rel);
        }

    }

}
