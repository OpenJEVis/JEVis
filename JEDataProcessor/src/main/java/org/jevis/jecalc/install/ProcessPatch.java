/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.install;

import org.jevis.api.JEVisDataSource;
import org.jevis.commons.patch.Patch;

/**
 * @author broder
 */
public enum ProcessPatch implements Patch {

    ALL_CLASSES("all") {
        @Override
        public void apply() {
            ProcessPatchBuildAllClasses.createAllClasses(datasource);
        }

        @Override
        public void undo() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    };
    private static JEVisDataSource datasource;
    private final String version;

    ProcessPatch(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    @Override
    public void setJEVisDataSource(JEVisDataSource ds) {
        ProcessPatch.datasource = ds;
    }

}
