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
package org.jevis.commons.dataprocessing;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Florian Simon
 */
@XmlRootElement(name = "process")
public class JsonProcess {

    private String id = "";
    private String function = "";
    private List<JsonProcessOption> options = new ArrayList<>();
    private List<JsonProcess> subProcesses = new ArrayList<>();

    public JsonProcess(Process task) {
        id = task.getID();
        function = task.getFunction().getName();
        for (ProcessOption opt : task.getOptions()) {
            options.add(new JsonProcessOption(opt));
        }
        for (Process stask : task.getSubProcesses()) {
            subProcesses.add(new JsonProcess(stask));
        }

    }

    public void setID(String id) {
        this.id = id;
    }

    @XmlElement(name = "id")
    public String getID() {
        return this.id;
    }

    @XmlElement(name = "function")
    public String getFunction() {
        return this.function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public void setSubTasks(List<JsonProcess> processes) {
        this.subProcesses = processes;
    }

    @XmlElement(name = "processes")
    public List<JsonProcess> getSubTasks() {
        return this.subProcesses;
    }

    public List<JsonProcessOption> getOptions() {
        return this.options;
    }

    public void setOptions(List<JsonProcessOption> options) {
        this.options = options;
    }

}
