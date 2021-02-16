/**
 * Copyright (C) 2015 Envidatec GmbH <info@envidatec.com>
 * <p>
 * This file is part of JECommons.
 * <p>
 * JECommons is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation in version 3.
 * <p>
 * JECommons is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * JECommons. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * JECommons is part of the OpenJEVis project, further project information are
 * published at <http://www.OpenJEVis.org/>.
 */
package org.jevis.commons.dataprocessing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.function.NullFunction;
import org.jevis.commons.ws.json.JsonAttribute;
import org.jevis.commons.ws.json.JsonObject;
import org.jevis.commons.ws.json.JsonSample;
import org.jevis.commons.ws.sql.SQLDataSource;
import org.jevis.commons.ws.sql.sg.JsonSampleGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class BasicProcess implements Process {
    private static final Logger logger = LogManager.getLogger(BasicProcess.class);

    private ProcessFunction _processor = new NullFunction(ManipulationMode.NONE, AggregationPeriod.NONE);
    private List<ProcessOption> _options = new ArrayList<>();
    private List<JEVisSample> _result;
    private List<JsonSample> _jsonResult;
    private List<Process> tasks = new ArrayList<>();
    private boolean isDone = false;
    private String _id = "*MISSING*";
    private JEVisDataSource _ds;
    private SQLDataSource _sqlDS;
    private JEVisObject _originalObject = null;
    private JsonObject _originalSQLObject = null;
    private JsonAttribute _originalSQLAttribute = null;
    private Process _parent;
    private boolean sql = false;
    private JsonSampleGenerator jsonSampleGenerator;

    public BasicProcess() {
    }

    public BasicProcess(JsonSampleGenerator jsonSampleGenerator) {
        this.jsonSampleGenerator = jsonSampleGenerator;
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
            logger.debug("make new sub task: " + jt);
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
        logger.info("setProcess: " + processor.getName());
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
    public JsonObject getJsonObject() {
        return _originalSQLObject;
    }

    @Override
    public void setJsonObject(JsonObject object) {
        _originalSQLObject = object;
    }

    @Override
    public JsonAttribute getJsonAttribute() {
        return _originalSQLAttribute;
    }

    @Override
    public void setJsonAttribute(JsonAttribute object) {
        _originalSQLAttribute = object;
    }

    @Override
    public List<JsonSample> getJsonResult() {
        if (_jsonResult != null) {
            return _jsonResult;
        }

        logger.info("Begin task " + getID());

        if (!isDone) {
            if (getSubProcesses().isEmpty()) {
//            logger.info("[" + _id + "]  No more sub tasks!");

            } else {
                for (Process task : getSubProcesses()) {
                    task.getJsonResult();
                }
//            logger.info("[" + _id + "] All subtask are done!");
            }
            isDone = true;
            logger.info(getID() + " task is done");

        }

        _jsonResult = getFunction().getJsonResult(this);

        logger.info("[" + _id + "] [" + _processor.getName() + "]  Result size: " + _jsonResult.size());

        return _jsonResult;

    }

    @Override
    public void setJEVisDataSource(JEVisDataSource ds) {
        _ds = ds;
    }

    @Override
    public void setSQLDataSource(SQLDataSource ds) {
        sql = true;
        _sqlDS = ds;
    }

    @Override
    public JEVisDataSource getJEVisDataSource() {
        return _ds;
    }

    @Override
    public SQLDataSource getSqlDataSource() {
        return _sqlDS;
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
        logger.info(toString());
        for (Process task : getSubProcesses()) {
            ((BasicProcess) task).print();
//            logger.info("--- " + task.toString());
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

    public boolean isSql() {
        return sql;
    }

    public JsonSampleGenerator getJsonSampleGenerator() {
        return jsonSampleGenerator;
    }
}
