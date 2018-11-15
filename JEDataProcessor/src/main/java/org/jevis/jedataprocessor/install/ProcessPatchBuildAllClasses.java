/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.install;

import org.jevis.api.JEVisDataSource;
import org.jevis.commons.database.ClassHandler;
import org.jevis.jedataprocessor.data.CleanDataAttributeJEVis;

/**
 * @author broder
 */
public class ProcessPatchBuildAllClasses {

    public static void createAllClasses(JEVisDataSource dc) {
        ClassHandler classHandler = new ClassHandler(dc);
        classHandler.createClass(CleanDataAttributeJEVis.CLASS_NAME);
        classHandler.setInheritanceParent("Data", CleanDataAttributeJEVis.CLASS_NAME);
        classHandler.setOkParent("Data", CleanDataAttributeJEVis.CLASS_NAME);
        for (CleanDataAttributeJEVis.AttributeName attribute : CleanDataAttributeJEVis.AttributeName.values()) {
            classHandler.addType(CleanDataAttributeJEVis.CLASS_NAME, attribute.getAttributeName());
        }
    }
}
