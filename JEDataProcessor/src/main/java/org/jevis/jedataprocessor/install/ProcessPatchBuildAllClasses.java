/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.install;

import org.jevis.api.JEVisDataSource;
import org.jevis.commons.database.ClassHandler;
import org.jevis.jedataprocessor.data.CleanDataObjectJEVis;

/**
 * @author broder
 */
public class ProcessPatchBuildAllClasses {

    public static void createAllClasses(JEVisDataSource dc) {
        ClassHandler classHandler = new ClassHandler(dc);
        classHandler.createClass(CleanDataObjectJEVis.CLASS_NAME);
        classHandler.setInheritanceParent("Data", CleanDataObjectJEVis.CLASS_NAME);
        classHandler.setOkParent("Data", CleanDataObjectJEVis.CLASS_NAME);
        for (CleanDataObjectJEVis.AttributeName attribute : CleanDataObjectJEVis.AttributeName.values()) {
            classHandler.addType(CleanDataObjectJEVis.CLASS_NAME, attribute.getAttributeName());
        }
    }
}
