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


import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.function.*;
import org.jevis.commons.json.JsonTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains various methods for manipulating ProcessChains.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ProcessChains {
    private static final Logger logger = LogManager.getLogger(ProcessChains.class);

    public static final String CLASS_PROCESS_CHAIN = "Process Chain";
    public static final String ATTRIBUTE_DATA = "Data";

    public static Process getProcessChain(JEVisDataSource ds, String name) throws JEVisException, IOException {

        List<JEVisObject> pcs = ds.getObjects(ds.getJEVisClass(CLASS_PROCESS_CHAIN), true);
        for (JEVisObject obj : pcs) {
            if (obj.getName().equalsIgnoreCase(name)) {
                return getProcessChain(obj);
            }
        }

        return null;
    }

    /**
     * Returns an ProcessChain from an existing JEVisObject representation.
     *
     * @param object
     * @return
     * @throws JEVisException
     */
    public static Process getProcessChain(JEVisObject object) throws JEVisException, IOException {
        if (!object.getJEVisClass().getName().equalsIgnoreCase(CLASS_PROCESS_CHAIN)) {
            throw new IllegalArgumentException("Object is not from the Class " + CLASS_PROCESS_CHAIN);
        }

        JEVisAttribute taskAttribute = object.getAttribute(ATTRIBUTE_DATA);
        String jsonString = taskAttribute.getLatestSample().getValueAsString();

//        Gson gson = new Gson();
//        JsonProcess jTask = gson.fromJson(jsonString, JsonProcess.class);
        JsonProcess jTask = JsonTools.objectMapper().readValue(jsonString, JsonProcess.class);
        return new BasicProcess(object.getDataSource(), jTask, null, object);
    }

    public static Process BuildProcessChain(JEVisDataSource ds, String functionName, String id, Process parent) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        logger.info("BuildTask(): {}    {}  {}", ds, functionName, id);
        ProcessFunction newFunction = BuildFunction(functionName);

        if (newFunction != null) {
            Process newTask = new BasicProcess();
            newTask.setJEVisDataSource(ds);
            newTask.setID(id);
            newTask.setFunction(newFunction);
            newTask.setOptions(getDefaultOptions(newFunction));
            newTask.setParent(parent);
            return newTask;

        }
        throw new ClassNotFoundException("Could not build function: '" + functionName + "'");

    }

    /**
     * Creates an new ProcessFunction by its name
     *
     * @param name
     * @return
     */
    public static ProcessFunction BuildFunction(String name) {
        //TODO: part of the driver loading logic

        //hardcoded workaround
        switch (name) {
            case AggregatorFunction.NAME:
                return new AggregatorFunction();
            case ConverterFunction.NAME:
                return new ConverterFunction();
            case CounterFunction.NAME:
                return new CounterFunction();
            case ImpulseFunction.NAME:
                return new ImpulseFunction();
            case LimitCheckerFunction.NAME:
                return new ImpulseFunction();
            case MathFunction.NAME:
                return new MathFunction(name);
            case InputFunction.NAME:
                return new InputFunction();
            default:
                return null;
        }

    }

    public static List<ProcessChain> getAvailableProcessChains(JEVisDataSource ds) throws JEVisException {
        List<ProcessChain> returnList = new ArrayList<>();
        JEVisClass processChain = ds.getJEVisClass(CLASS_PROCESS_CHAIN);
        if (processChain != null) {
            List<JEVisObject> objects = ds.getObjects(processChain, true);

            if (objects != null) {
                for (JEVisObject obj : objects) {
                    returnList.add(new ProcessChain(obj));
                }
            }

        }

        return returnList;

    }

    /**
     * Retuns all default ProcessOptions for an ProcessFunction
     *
     * @param pf
     * @return
     */
    public static List<ProcessOption> getDefaultOptions(ProcessFunction pf) {
        List<ProcessOption> map = new ArrayList<>();

        for (ProcessOption o : pf.getAvailableOptions()) {
            map.add(o);
        }

        return map;
    }

    /**
     * Delevlopment helper to create an new compley Example for testing purpose
     *
     * @param ds
     * @return
     * @deprecated
     */
    public static Process BuildExampleTask(JEVisDataSource ds) {

        Process inputProcess = new BasicProcess();
        inputProcess.setJEVisDataSource(ds);
        inputProcess.setID("Input");
        inputProcess.setFunction(new InputFunction());
        inputProcess.setOptions(getDefaultOptions(new InputFunction()));

        Process counterProcess = new BasicProcess();
        counterProcess.setJEVisDataSource(ds);
        counterProcess.setID("Counter");
        counterProcess.setFunction(new CounterFunction());
        counterProcess.setOptions(getDefaultOptions(new CounterFunction()));

        Process mxbProcess = new BasicProcess();
        mxbProcess.setJEVisDataSource(ds);
        mxbProcess.setID("Mx+b");
        mxbProcess.setFunction(new ConverterFunction());
        mxbProcess.setOptions(getDefaultOptions(new ConverterFunction()));

        List<Process> subMxb = new ArrayList<>();
        subMxb.add(counterProcess);
        mxbProcess.setSubProcesses(subMxb);

        List<Process> subCounter = new ArrayList<>();
        subCounter.add(inputProcess);
        counterProcess.setSubProcesses(subCounter);

        return mxbProcess;

//        ProcessChain aggFunction = new BasicProcessChain();
//        aggFunction.setJEVisDataSource(ds);
//        aggFunction.setID("Aggrigation Task");
//        aggFunction.setFunction(new AggregatorFunction());
//        aggFunction.setOptions(getDefaultOptions(new AggregatorFunction()));
//        aggFunction.getSubTasks().add(inputProcess);
//        inputProcess.setParent(aggFunction);
//
////        Option decountOptions = new OptionImp(null);
//        ProcessChain decounterFunction = new BasicProcessChain();
//        decounterFunction.setJEVisDataSource(ds);
//        decounterFunction.setID("Counter Function");
//        decounterFunction.setFunction(new CounterFunction());
//        decounterFunction.setOptions(getDefaultOptions(new CounterFunction()));
//
////        decounterFunction.setObject(null);
//        ProcessChain result = new BasicProcessChain();
//        result.setJEVisDataSource(ds);
//        result.setID("Result");
//        result.setFunction(new InputFunction());
//        List<ProcessChain> subTask = new ArrayList<>();
//        subTask.add(decounterFunction);
//        subTask.add(aggFunction);
//        aggFunction.setParent(result);
//        decounterFunction.setParent(result);
//        result.setSubTasks(subTask);
//        result.setOptions(getDefaultOptions(new InputFunction()));
//
//        return result;
    }

    /**
     * Returns an list of allavilable funtions.
     *
     * @param ds
     * @return
     */
    public static List<ProcessFunction> getAvailableFunctions(JEVisDataSource ds) {
        List<ProcessFunction> result = new ArrayList<>();

        //TODO: fetch all available function driver from data source
        //Workaround, static adding of functions
        result.add(new AggregatorFunction());
        result.add(new ConverterFunction());
        result.add(new CounterFunction());
        result.add(new ImpulseFunction());
        result.add(new InputFunction());
        result.add(new LimitCheckerFunction());
        //todo default function?
        result.add(new MathFunction("default"));
        result.add(new NullFunction(ManipulationMode.NONE, AggregationPeriod.NONE));

        return result;

    }

    /**
     * Returns an Json String representation of an Task
     *
     * @param task
     * @return
     */
    public static String processChainToJSon(Process task) throws JsonProcessingException {
        BasicProcess json = new BasicProcess();
        json.setFunction(new NullFunction(ManipulationMode.NONE, AggregationPeriod.NONE));
//        Gson gson = new GsonBuilder().create();
//        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return JsonTools.prettyObjectMapper().writeValueAsString(new JsonProcess(task));
    }

    /**
     * Creates an new Task from an JSon String
     *
     * @param ds
     * @param json
     * @return
     */
    public static Process jsonToProcessChain(JEVisDataSource ds, String json) throws IOException {
//        JsonProcess jTask = new Gson().fromJson(json, JsonProcess.class);
        JsonProcess jTask = JsonTools.objectMapper().readValue(json, JsonProcess.class);

        return new BasicProcess(ds, jTask, null);
    }

    /**
     * Returns an matching Processor based on the name.
     *
     * @param name Name of the Processor
     * @return
     */
    public static ProcessFunction getFunction(String name) {

        //TODO: replace this workaround with the Driver loading function
        switch (name) {
            case InputFunction.NAME:
                return new InputFunction();
            case AggregatorFunction.NAME:
                return new AggregatorFunction();
            case CounterFunction.NAME:
                return new CounterFunction();
            case ImpulseFunction.NAME:
                return new ImpulseFunction();
            case LimitCheckerFunction.NAME:
                return new LimitCheckerFunction();
            case MathFunction.NAME:
                return new MathFunction(name);
            case ConverterFunction.NAME:
                return new ConverterFunction();
            case NullFunction.NAME:
            default:
                return new NullFunction(ManipulationMode.NONE, AggregationPeriod.NONE);
        }
    }

}
