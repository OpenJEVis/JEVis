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
package org.jevis.commons.dataprocessing.v2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.config.Options;
import org.jevis.commons.object.plugin.VirtualSumData;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Florian Simon
 * @TODO: should this have an interface?!
 */
public class DataProcessing {
    private static final Logger logger = LogManager.getLogger(DataProcessing.class);

    private static final String DATA_PROCESSING = "Data Processing";
    private static final String WORKFLOW_OPTION = "Workflows";
    private static final String DETFAULT_DATAPROCESSING = "Default";

    public static List<JEVisSample> getSamples(JEVisAttribute attribute, DateTime from, DateTime until, String dataProcess) throws JEVisException {
        logger.info("DataProcessing.getSamples: " + attribute + "    from: " + from + "   until: " + until + "     process: " + dataProcess);
        //TODO: this hast du be some kinde of driver structure where we can add new VirtualData without changing this code
        if (attribute.getObject().getJEVisClass().getName().equals("Virtual Sum")) {
            logger.info("is Virtual sum ");
            VirtualSumData vd = new VirtualSumData(attribute.getObject());
            //TODO: this result can also use an addional DataProcessor
            List<JEVisSample> result = vd.getResult(from, until);

//            DataWorkflow dwf = GetConfiguredWorkflow(attribute, dataProcess);
//
//            Result result = dwf.getResult();
            return result;

        } else {
            logger.info("is raw data");
            return attribute.getSamples(from, until);
        }
    }

    public static List<String> GetConfiguredWorkflowNames(JEVisAttribute attribute) {
        List<String> names = new ArrayList<>();

        JEVisOption dataPorcessingRoot = GetDataProcessingOption(attribute);

        if (dataPorcessingRoot != null && dataPorcessingRoot.getOptions() != null && !dataPorcessingRoot.getOptions().isEmpty()) {

            if (Options.hasOption(WORKFLOW_OPTION, dataPorcessingRoot)) {
                JEVisOption workflowRoot = Options.getFirstOption(WORKFLOW_OPTION, dataPorcessingRoot);

                if (workflowRoot.getOptions() != null && !workflowRoot.getOptions().isEmpty()) {
                    for (JEVisOption workflowOptions : workflowRoot.getOptions()) {
                        names.add(workflowOptions.getKey());
                    }
                }

            }
        }

        return names;
    }

    public static JEVisOption GetDataProcessingOption(JEVisAttribute attribute) {
        JEVisOption dataPorcessingRoot = null;

        if (attribute != null && attribute.getOptions() != null && !attribute.getOptions().isEmpty()) {
            logger.info("attribute has Options: " + attribute.getName());

            Options.toString(attribute.getOptions());
            logger.info("-----------------------------");

            for (JEVisOption opt : attribute.getOptions()) {
                if (opt.getKey().equals(DATA_PROCESSING)) {
                    dataPorcessingRoot = opt;
                    break;
                } else if (Options.hasOption(DATA_PROCESSING, opt)) {
                    logger.info("has DATA_PROCESSING");
                    dataPorcessingRoot = Options.getFirstOption(DATA_PROCESSING, opt);
                    break;
                }
            }
        } else {
            logger.warn("Warning no Data Workflow available");
        }

        return dataPorcessingRoot;
    }

    public static JEVisObject GetWorkflowObject(JEVisDataSource ds, String name) throws JEVisException {
        JEVisClass workflowClass = ds.getJEVisClass(DataWorkflow.DATA_WORKFLOW_CLASS);

        List<JEVisObject> objects = ds.getObjects(workflowClass, true);
        logger.info("There are " + objects.size() + " workflows in the system searching for '" + name + "'");
        for (JEVisObject obj : objects) {
            logger.info("workflow: " + obj);
            if (obj.getName().equals(name)) {
                logger.info(" <--");
                return obj;
            } else {
                logger.info(" x ");
            }
        }
        logger.info("Workflow not found");
        return null;
    }

    public static JEVisObject GetDataProcessor(JEVisDataSource ds, String name) throws JEVisException {
        JEVisClass processorClass = ds.getJEVisClass(Function.JEVIS_CLASS);

        List<JEVisObject> objects = ds.getObjects(processorClass, true);
        for (JEVisObject obj : objects) {
            if (obj.getName().equals(name)) {
                return obj;
            }
        }

        return null;
    }

    public static List<DataWorkflow> GetSystemWorkflows(JEVisDataSource ds) throws JEVisException {
        List<DataWorkflow> allWorkflows = new ArrayList<>();

        JEVisClass workflowClass = ds.getJEVisClass(DataWorkflow.DATA_WORKFLOW_CLASS);

        List<JEVisObject> dps = ds.getObjects(workflowClass, true);
        if (dps.isEmpty()) {
            throw new JEVisException("This DataSource has no DataProcessors", 63405);
        } else if (dps.size() > 1) {
            logger.warn("Waring there are more than one DataProcessor on the first Level. All but the first will be ignored");
        }

        return allWorkflows;

    }

    public static DataWorkflow GetConfiguredDefaultWorkflow(JEVisAttribute attribut) throws JEVisException {
        JEVisOption dataPorcessingRoot = GetDataProcessingOption(attribut);

        String defaultValue = GetOptionValue(dataPorcessingRoot, DETFAULT_DATAPROCESSING, "");
        logger.info("default is set: '" + defaultValue + "'");

        for (String name : GetConfiguredWorkflowNames(attribut)) {
            if (name.equals(defaultValue)) {
                return GetConfiguredWorkflow(attribut, defaultValue);
            }
        }
        throw new NullPointerException("Default Data Workflow not found");

    }

    public static DataWorkflow GetConfiguredWorkflow(JEVisAttribute attribut, String workflowName) throws JEVisException {
        JEVisOption dataPorcessingRoot = GetDataProcessingOption(attribut);

        if (dataPorcessingRoot != null && dataPorcessingRoot.getOptions() != null && !dataPorcessingRoot.getOptions().isEmpty()) {

            if (Options.hasOption(WORKFLOW_OPTION, dataPorcessingRoot)) {
                JEVisOption workflowRoot = Options.getFirstOption(WORKFLOW_OPTION, dataPorcessingRoot);
                for (JEVisOption workflowOptions : workflowRoot.getOptions()) {
                    if (workflowOptions.getKey().equals(workflowName)) {
                        DataWorkflow wflo = new BasicDataWorkflow();

                        JEVisObject workFlowObject = GetWorkflowObject(attribut.getDataSource(), workflowName);
                        if (workFlowObject != null) {
                            wflo.setObject(workFlowObject);
                        } else {
                            logger.warn("WARNING: no workflow object");
                        }
                        wflo.setOption(workflowOptions);
                        wflo.setAttribute(attribut);
                        return wflo;
                    }
                }

            }

        }
        return null;
    }

    public static List<DataWorkflow> GetUnConfiguredWorkflows(JEVisAttribute attribute) {
        List<DataWorkflow> workflows = new ArrayList<>();

        JEVisOption dataPorcessingRoot = null;

        if (dataPorcessingRoot != null && dataPorcessingRoot.getOptions() != null && !dataPorcessingRoot.getOptions().isEmpty()) {
            for (JEVisOption workflowOptions : dataPorcessingRoot.getOptions()) {
                if (workflowOptions.equals(DETFAULT_DATAPROCESSING)) {
                    //SetDefault... or so
                } else {
                    DataWorkflow wflo = new BasicDataWorkflow();
//                    wflo.setObject(workFlowObject);
                    wflo.setOption(workflowOptions);
//                    wflo.setTask(loadTasks(null, dps.get(0), wflo.getOption()));
                    workflows.add(wflo);
                }
            }
        }

        return workflows;
    }

    public static String GetDataProcessorID(JEVisObject dataProcessorObject) {
        try {
            return dataProcessorObject.getAttribute(Function.ATTRIBUTE_ID).getLatestSample().getValueAsString();
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return "";

    }

    public static <T> T GetOptionValue(JEVisOption parent, String key, Object defaultvalue) {
        try {
            logger.info("Gettring option value '" + key + "' from parent: " + parent.getKey() + " fallback value: '" + defaultvalue + "'");
            if (Options.hasOption(key, parent)) {
                logger.info("option exist");
                String mString = Options.getFirstOption(key, parent).getValue();

                if (defaultvalue instanceof Double) {
                    return (T) (Double) Double.parseDouble(mString);
                } else if (defaultvalue instanceof Integer) {
                    return (T) (Integer) Integer.parseInt(mString);
                } else if (defaultvalue instanceof String) {
                    return (T) mString;
                } else if (defaultvalue instanceof DateTime) {
                    DateTime dateTime = DateTime.parse(mString, ISODateTimeFormat.dateTime());//yyyy-MM-dd'T'HH:mm:ss.SSSZZ
                    return (T) dateTime;
                } else if (defaultvalue instanceof Boolean) {
                    return (T) Boolean.valueOf(mString);
                } else {
                    logger.error("Unsupported Class '" + defaultvalue.getClass() + "' returning String");
                    return (T) mString;
                }

            } else {
                logger.error("Parameter not found returning default");
                return (T) defaultvalue;
            }
        } catch (Exception ex) {
            logger.error("error return default: " + ex);
            return (T) defaultvalue;
        }
    }

    public static Task BuildTask(Task parent, JEVisObject processor, JEVisOption options, DataWorkflow workflow) throws JEVisException {
        logger.info("--BuildTask--");
        logger.info("Parent: " + parent + " pro: " + processor.getName() + " opt: " + options.getKey());
        Task task = new BasicTask();
        JEVisClass processorClass = processor.getDataSource().getJEVisClass(Function.JEVIS_CLASS);

        Function dp;
        dp = DataProcessorDriverManager.loadDriver(processor);

        logger.info("loadTasks.1: " + processor);
        task.setDataProcessor(dp);
        dp.setWorkflow(workflow);

        logger.info("loadTasks.2: " + dp.getID() + "   Option: " + options.getKey() + "\n------\n");

        if (Options.hasOption(dp.getID(), options)) {//error
            dp.setOptions(Options.getFirstOption(dp.getID(), options));
            logger.info("setOption to: " + dp.getID());
        } else {
            logger.warn("Option for DP not found");
        }

        logger.info("\n----\n");

        List<Task> dependency = new ArrayList<>();
        logger.info("Dp has Children: " + processor.getChildren(processorClass, true).size());
        for (JEVisObject child : processor.getChildren(processorClass, true)) {
            logger.info("Build dependency: " + child.getName());
            dependency.add(BuildTask(task, child, options, workflow));
        }
        logger.info("add all depency to " + task.getDataProcessor().getID());
        task.setDependency(dependency);

        return task;
    }

}
