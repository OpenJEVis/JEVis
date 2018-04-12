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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.dataprocessing.function.AggrigatorFunction;
import org.jevis.commons.dataprocessing.function.ConverterFunction;
import org.jevis.commons.dataprocessing.function.CounterFunction;
import org.jevis.commons.dataprocessing.function.ImpulsFunction;
import org.jevis.commons.dataprocessing.function.InputFunction;
import org.jevis.commons.dataprocessing.function.LimitCheckerFunction;
import org.jevis.commons.dataprocessing.function.MathFunction;
import org.jevis.commons.dataprocessing.function.NullFunction;

/**
 * This class contains various methods for manipulating ProcessChains.
 *
 * @author Florian Simon <florian.simon@envidatec.com>
 */
public class ProcessChains {

    public static final String CLASS_PROCESS_CHAIN = "Process Chain";
    public static final String ATTRIBUTE_DATA = "Data";

    public static Process getProcessChain(JEVisDataSource ds, String name) throws JEVisException {

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
    public static Process getProcessChain(JEVisObject object) throws JEVisException {
        if (!object.getJEVisClass().getName().equalsIgnoreCase(CLASS_PROCESS_CHAIN)) {
            throw new IllegalArgumentException("Object is not from the Class " + CLASS_PROCESS_CHAIN);
        }

        JEVisAttribute taskAttribute = object.getAttribute(ATTRIBUTE_DATA);
        String jsonString = taskAttribute.getLatestSample().getValueAsString();

        Gson gson = new Gson();
        JsonProcess jTask = gson.fromJson(jsonString, JsonProcess.class);
        Process newTask = new BasicProcess(object.getDataSource(), jTask, null, object);
        return newTask;
    }

    public static Process BuildProcessChain(JEVisDataSource ds, String functionName, String id, Process parent) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        System.out.println("BuildTask(): " + ds + "    " + functionName + "  " + id);
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
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static ProcessFunction BuildFunction(String name) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        //TODO: part of the driver loding logic

        //hardcodedt workaround
        switch (name) {
            case AggrigatorFunction.NAME:
                return new AggrigatorFunction();
            case ConverterFunction.NAME:
                return new ConverterFunction();
            case CounterFunction.NAME:
                return new CounterFunction();
            case ImpulsFunction.NAME:
                return new ImpulsFunction();
            case LimitCheckerFunction.NAME:
                return new ImpulsFunction();
            case MathFunction.NAME:
                return new MathFunction();
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
     * @deprecated
     * @param ds
     * @return
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
//        aggFunction.setFunction(new AggrigatorFunction());
//        aggFunction.setOptions(getDefaultOptions(new AggrigatorFunction()));
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
     *
     * @return
     */
    public static List<ProcessFunction> getAvailableFunctions(JEVisDataSource ds) {
        List<ProcessFunction> result = new ArrayList<>();

        //TODO: fetch all avilable function driver from datasource
        //Workaround, static adding of functions
        result.add(new AggrigatorFunction());
        result.add(new ConverterFunction());
        result.add(new CounterFunction());
        result.add(new ImpulsFunction());
        result.add(new InputFunction());
        result.add(new LimitCheckerFunction());
        result.add(new MathFunction());
        result.add(new NullFunction());

        return result;

    }

    /**
     * Returns an Json String representation of an Task
     *
     * @param task
     * @return
     */
    public static String processChainToJSon(Process task) {
        BasicProcess json = new BasicProcess();
        json.setFunction(new NullFunction());
//        Gson gson = new GsonBuilder().create();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson.toJson(new JsonProcess(task), JsonProcess.class);
    }

    /**
     * Creates an new Task from an JSon String
     *
     * @param ds
     * @param json
     * @return
     */
    public static Process jsonToProcessChain(JEVisDataSource ds, String json) {
        JsonProcess jTask = new Gson().fromJson(json, JsonProcess.class);
        Process newTask = new BasicProcess(ds, jTask, null);

        return newTask;
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
            case AggrigatorFunction.NAME:
                return new AggrigatorFunction();
            case CounterFunction.NAME:
                return new CounterFunction();
            case ImpulsFunction.NAME:
                return new ImpulsFunction();
            case LimitCheckerFunction.NAME:
                return new LimitCheckerFunction();
            case MathFunction.NAME:
                return new MathFunction();
            case NullFunction.NAME:
                return new NullFunction();
            case ConverterFunction.NAME:
                return new ConverterFunction();
            default:
                return new NullFunction();
        }
    }

}
