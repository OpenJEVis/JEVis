/**
 * Copyright (C) 2016 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEAPI-WS.
 *
 * JEAPI-WS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 *
 * JEAPI-WS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * JEAPI-WS. If not, see <http://www.gnu.org/licenses/>.
 *
 * JEAPI-WS is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.jeapi.ws;

import org.jevis.api.JEVisClassRelationship;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisRelationship;

/**
 * Main Class for testing purpose
 *
 * @author fs
 */
public class Main {

    public static void main(String[] args) throws JEVisException {
        JEVisDataSource ds = new JEVisDataSourceWS();
        ds.init(null);
        ds.connect("Sys Admin", "OpenJEVis2016");

        System.out.println("Test#0");
        System.out.println("User: " + ds.getCurrentUser().getFirstName() + " " + ds.getCurrentUser().getLastName());

        System.out.println("Test#1");
        ds.getObject(1l);

        System.out.println("TEST#ClassIcon");
        ds.getJEVisClass("Building").getIcon();

        System.out.println("Test#2");
        ds.getJEVisClass("Data");

        System.out.println("Test#3");
        ds.getJEVisClass("Report Link");

//
//        System.out.println("Test#4");
//        ds.getJEVisClasses();
//
        System.out.println("Test#5");
//        System.out.println(ds.getObject(5270l).getAttribute("KVA").getPrimitiveType());

        System.out.println("Test#6");
//        System.out.println(ds.getRootObjects());

        System.out.println("Test#7");
        for (JEVisObject obj : ds.getObjects()) {
//            System.out.println("obj: " + obj.getID());
        }

        System.out.println("Test#8");
        for (JEVisRelationship rel : ds.getRelationships()) {
//            System.out.println("Rel: " + rel);
        }

        System.out.println("Test#9");
        for (JEVisClassRelationship rel : ds.getClassRelationships()) {
//            System.out.println("Rel: " + rel);
        }

    }

}
