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
package org.jevis.commons.classes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisType;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fs
 */
public class ClassHelper {

    private static final Logger logger = LogManager.getLogger(ClassHelper.class);

    public static void updateTypesForHeirs(JEVisDataSource ds, String jclass) throws JEVisException {
        JEVisClass parentClass = ds.getJEVisClass(jclass);

        if (parentClass != null) {

            //get Type from the parentclass
            for (JEVisType type : parentClass.getTypes()) {

                //get all heirs form the parent add add/update the types to them
                for (JEVisClass heir : parentClass.getHeirs()) {
                    logger.trace("Add Type: '{}' to {}", type.getName(), heir.getName());
                    JEVisType childType = heir.getType(type.getName());
                    if (childType == null) {//add new
                        childType = heir.buildType(type.getName());
                    }

                    CopyTypeInto(type, childType);
                    childType.commit();
                }
            }

            //delete old
            for (JEVisClass heir : parentClass.getHeirs()) {
                List<JEVisClass> parents = new ArrayList<>();
                AddAllInherited(parents, heir);

                for (JEVisType type : heir.getTypes()) {
                    if (type.isInherited()) {

                        boolean parentHasType = false;
                        for (JEVisClass parent : parents) {
                            JEVisType pType = parent.getType(type.getName());
                            if (pType != null && !pType.isInherited()) {
                                parentHasType = true;
                            }
                        }
                        if (!parentHasType) {
                            logger.trace("Delete Type: {}", type.getName());
                            heir.deleteType(type.getName());
                        }
                    }
                }

            }
        } else {
            logger.error("Why is the class null: {}", jclass);
        }

    }

    public static boolean isDirectory(JEVisClass jclass) throws JEVisException {

        if (jclass.getName().equals("Directory")) {
            return true;
        }

        if (jclass.getInheritance() != null) {
            if (jclass.getInheritance().getName().equals("Directory")) {
                return true;
            } else {
                return isDirectory(jclass.getInheritance());
            }
        } else {
            return false;
        }
    }

    public static void AddAllInherited(List<JEVisClass> all, JEVisClass jclass) throws JEVisException {
        JEVisClass inhert = jclass.getInheritance();
        if (inhert != null) {
            all.add(inhert);
            AddAllInherited(all, inhert);
        }

    }

    public static void CopyTypeInto(JEVisType from, JEVisType into) throws JEVisException {
        into.setAlternativSymbol(from.getAlternativSymbol());
        into.setConfigurationValue(from.getConfigurationValue());
        into.setDescription(from.getDescription());
        into.setPrimitiveType(from.getPrimitiveType());
        into.setGUIDisplayType(from.getGUIDisplayType());
//        into.setGUIPosition(from.getGUIPosition());//is per Class and not inheritedt

        into.setUnit(from.getUnit());
        into.setValidity(from.getValidity());
        into.setInherited(true);
    }

}
