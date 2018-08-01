/**
 * Copyright (C) 2014 Envidatec GmbH <info@envidatec.com>
 *
 * This file is part of JEApplication.
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
package org.jevis.commons.application;

import org.jevis.api.JEVisInfo;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fs
 */
public class ApplicationInfo {

    private String version = "";
    private JEVisInfo api;
    private String name = "";
    private List<LibraryInfo> libs = new ArrayList<>();

    public ApplicationInfo(String name, String version) {
        this.name = name;
        this.version = version;

    }

    public void setJEVisAPI(JEVisInfo api) {
        this.api = api;
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name=name;
    }

    public String getVersion() {
        return version;
    }

    public JEVisInfo getAPIVersion() {
        return api;
    }

    public void addLibrary(LibraryInfo lib) {
        libs.add(lib);
    }

    public List<LibraryInfo> getLibrarys() {
        return libs;
    }

    @Override
    public String toString() {
        String msg = name + " Version: " + version + "\n";

        for (LibraryInfo lib : libs) {
            msg += "\n" + lib.getName() + ": " + lib.getVersion();
        }

        return msg;
    }

}
