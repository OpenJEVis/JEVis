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

import org.jevis.commons.dataprocessing.function.NullFunction;
import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.utils.Benchmark;

/**
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class BasicProcess implements Process {

    private ProcessFunction _processor = new NullFunction();
    private List<ProcessOption> _options = new ArrayList<>();
    private List<JEVisSample> _result;
    private List<Process> tasks = new ArrayList<>();
    private boolean isDone = false;
    private String _id = "*MISSING*";
    private JEVisDataSource _ds;
    private JEVisObject _originalObject = null;
    private Process _parent;

    public BasicProcess() {
    }

    public BasicProcess(JEVisDataSource ds, JsonProcess task, Process parentTask) {
        initTask(ds, task, parentTask, null);

    }

    public BasicProcess(JEVisDataSource ds, JsonProcess task, Process parentTask, JEVisObject object) {
        initTask(ds, task, parentTask, object);

    }

    public void initTask(JEVisDataSource ds, JsonProcess jTask, Process parentTask, JEVisObject parentObj) {
        for (JsonProcessOption opt : jTask.getOptions()) {
            _options.add(new BasicProcessOption(opt));
        }

        setFunction(ProcessChains.getFunction(jTask.getFunction()));
        setID(jTask.getID());
        setJEVisDataSource(ds);
        setObject(parentObj);
        for (JsonProcess jt : jTask.getSubTasks()) {
            System.out.println("make new subtak: " + jt);
            tasks.add(new BasicProcess(ds, jt, parentTask, parentObj));
        }
        _parent = parentTask;
    }

    @Override
    public void setParent(Process parent) {
        if (parent != null) {
            if (!parent.getSubProcesses().contains(this)) {
                parent.getSubProcesses().add(this);
            }

            this._parent = parent;
        }

    }

    @Override
    public Process getParent() {
        return _parent;
    }

    @Override
    public void setID(String id) {
        _id = id;
    }

    @Override
    public void setFunction(ProcessFunction processor) {
        System.out.println("setProcess: " + processor.getName());
        _processor = processor;
    }

    @Override
    public ProcessFunction getFunction() {
        return _processor;
    }

    @Override
    public void setSubProcesses(List<Process> tasks) {
        this.tasks = tasks;
    }

    @Override
    public List<Process> getSubProcesses() {
        return tasks;

    }

    @Override
    public void setObject(JEVisObject object) {
        _originalObject = object;
    }

    @Override
    public JEVisObject getObject() {
        return _originalObject;
    }

    @Override
    public List<JEVisSample> getResult() {
        if (_result != null) {
            return _result;
        }

        if (!isDone) {
            if (getSubProcesses().isEmpty()) {
//            System.out.println("[" + _id + "]  No more sub tasks!");

            } else {
                for (Process task : getSubProcesses()) {
                    task.getResult();
                }
//            System.out.println("[" + _id + "] All subtask are done!");
            }
            isDone = true;

        }

        Benchmark bench = new Benchmark();
        _result = getFunction().getResult(this);
        System.out.println("[" + _id + "] [" + _processor.getName() + "]  Result size: " + _result.size());

        bench.printBechmark(" Task " + getID());
        return _result;

    }

    @Override
    public void setJEVisDataSource(JEVisDataSource ds) {
        _ds = ds;
    }

    @Override
    public JEVisDataSource getJEVisDataSource() {
        return _ds;
    }

    @Override
    public String getID() {
        return _id;
    }

    @Override
    public List<ProcessOption> getOptions() {
        return _options;
    }

    @Override
    public void setOptions(List<ProcessOption> options) {
        _options = options;
    }

    @Override
    public String toString() {
        return "ProcessImp{" + "_processor=" + _processor + ", _options=" + _options + ", _result=" + _result + ", _prevTask=" + tasks.size() + ", isDone=" + isDone + ", _id=" + _id + ", _ds=" + _ds + '}';
    }

    public void print() {
        System.out.println(toString());
        for (Process task : getSubProcesses()) {
            ((BasicProcess) task).print();
//            System.out.println("--- " + task.toString());
        }
    }

    @Override
    public void restResult() {
        _result = null;
        _processor.resetResult();
        for (Process subt : getSubProcesses()) {
            subt.restResult();
        }
    }

}
