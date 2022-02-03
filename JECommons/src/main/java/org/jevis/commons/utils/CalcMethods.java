package org.jevis.commons.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.calculation.CalcInputObject;
import org.jevis.commons.calculation.CalcInputType;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalcMethods extends CommonMethods {
    private static final Logger logger = LogManager.getLogger(CalcMethods.class);

    public static void deleteAllCalculationDependencies(JEVisObject jeVisObject, DateTime from) {
        JEVisClass calculationClass = null;
        JEVisClass outputClass = null;
        JEVisClass cleanDataClass = null;
        JEVisClass rawDataClass = null;
        JEVisDataSource ds = null;
        try {
            ds = jeVisObject.getDataSource();
            calculationClass = ds.getJEVisClass("Calculation");
            outputClass = ds.getJEVisClass("Output");
            rawDataClass = ds.getJEVisClass("Data");
            cleanDataClass = ds.getJEVisClass("Clean Data");

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ds == null) {
            return;
        }

        List<JEVisObject> allCalculations = new ArrayList<>();
        try {
            allCalculations = ds.getObjects(calculationClass, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<JEVisObject> allTargets = new ArrayList<>();
        Map<JEVisObject, JEVisObject> targetAndCalc = new HashMap<>();
        for (JEVisObject calcObject : allCalculations) {
            try {
                for (JEVisObject output : calcObject.getChildren(outputClass, false)) {
                    JEVisAttribute attribute = output.getAttribute("Output");
                    if (attribute != null && attribute.hasSample()) {
                        try {
                            TargetHelper th = new TargetHelper(ds, attribute);
                            if (!th.getObject().isEmpty()) {
                                allTargets.addAll(th.getObject());
                                targetAndCalc.put(th.getObject().get(0), calcObject);
                            }
                        } catch (Exception e) {
                            logger.error("Error with output {}:{}", output.getName(), output.getID(), e);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<JEVisObject> foundCalcTarget = new ArrayList<>();
        List<JEVisObject> calculationsToDisable = new ArrayList<>();
        for (JEVisObject target : allTargets) {
            if (jeVisObject.equals(target)) {
                foundCalcTarget.add(target);
                calculationsToDisable.add(targetAndCalc.get(target));
                getOtherDependencies(target, allCalculations, foundCalcTarget, calculationsToDisable, ds, outputClass);
            }
        }

        for (JEVisObject concernedCalculation : calculationsToDisable) {
            setEnabled(concernedCalculation, "Calculation", false);
        }

        DateTime to = DateTime.now();
        deleteSamplesInList(from, to, foundCalcTarget);

        List<JEVisObject> allCleanData = new ArrayList<>();
        for (JEVisObject data : foundCalcTarget) {
            try {
                allCleanData.addAll(data.getChildren(cleanDataClass, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        deleteSamplesInList(from, to, allCleanData);

        for (JEVisObject concernedCalculation : calculationsToDisable) {
            setEnabled(concernedCalculation, "Calculation", true);
        }
    }

    private static void getOtherDependencies(JEVisObject jeVisObject, List<JEVisObject> allCalculations, List<JEVisObject> foundCalcTarget, List<JEVisObject> calculationsToDisable, JEVisDataSource ds, JEVisClass outputClass) {
        List<JEVisObject> allTargets = new ArrayList<>();
        Map<JEVisObject, JEVisObject> targetAndCalc = new HashMap<>();
        for (JEVisObject calcObject : allCalculations) {
            try {
                for (JEVisObject output : calcObject.getChildren(outputClass, false)) {
                    JEVisAttribute attribute = output.getAttribute("Output");
                    if (attribute != null && attribute.hasSample()) {
                        try {
                            TargetHelper th = new TargetHelper(ds, attribute);
                            if (!th.getObject().isEmpty()) {
                                allTargets.addAll(th.getObject());
                                targetAndCalc.put(th.getObject().get(0), calcObject);
                            }
                        } catch (Exception e) {
                            logger.error("Error with output {}:{}", output.getName(), output.getID(), e);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (JEVisObject target : allTargets) {
            if (jeVisObject.equals(target)) {
                foundCalcTarget.add(target);
                calculationsToDisable.add(targetAndCalc.get(target));
                getOtherDependencies(target, allCalculations, foundCalcTarget, calculationsToDisable, ds, outputClass);
            }
        }
    }

    public static String getTranslatedFormula(JEVisObject calculationObject) {
        String expression = "";

        try {
            JEVisDataSource ds = calculationObject.getDataSource();
            JEVisAttribute expressionAttribute = calculationObject.getAttribute(CalcJobFactory.Calculation.EXPRESSION.getName());
            expression = expressionAttribute.getLatestSample().getValueAsString();
            JEVisClass inputClass = ds.getJEVisClass(CalcJobFactory.Calculation.INPUT.getName());
            List<JEVisObject> calcInputObjects = calculationObject.getChildren(inputClass, false);

            for (JEVisObject inputObject : calcInputObjects) {
                JEVisAttribute targetAttr = inputObject.getAttribute(CalcJobFactory.Calculation.INPUT_DATA.getName());
                TargetHelper targetHelper = new TargetHelper(ds, targetAttr);
                JEVisAttribute valueAttribute = targetHelper.getAttribute().get(0);
                String identifier = inputObject.getAttribute(CalcJobFactory.Calculation.IDENTIFIER.getName()).getLatestSample().getValueAsString();
                String inputTypeString = inputObject.getAttribute(CalcJobFactory.Calculation.INPUT_TYPE.getName()).getLatestSample().getValueAsString();
                CalcInputType inputType = CalcInputType.valueOf(inputTypeString);
                CalcInputObject calcInputObject = new CalcInputObject(identifier, inputType, valueAttribute);
                String name = "\"";
                if (calcInputObject.getValueAttribute().getObject().getJEVisClassName().equals("Clean Data")) {
                    JEVisObject parent = CommonMethods.getFirstParentalDataObject(calcInputObject.getValueAttribute().getObject());
                    if (parent != null) {
                        name += parent.getName();
                    }
                } else if (calcInputObject.getValueAttribute().getObject().getJEVisClassName().equals("Data")) {
                    name += calcInputObject.getValueAttribute().getObject().getName();
                }
                name += "\"";

                if (!name.equals("\"\"")) {
                    expression = expression.replace(calcInputObject.getIdentifier(), name);
                }
            }

            expression = expression.replace("#", "");
            expression = expression.replace("{", "");
            expression = expression.replace("}", "");
        } catch (Exception e) {

        }

        return expression;
    }


    public static void deleteAllCalculations(JEVisObject jeVisObject, DateTime from, DateTime to) {
        JEVisClass calculationClass = null;
        JEVisClass outputClass = null;
        JEVisClass cleanDataClass = null;
        JEVisClass rawDataClass = null;
        JEVisDataSource ds = null;
        try {
            ds = jeVisObject.getDataSource();
            calculationClass = ds.getJEVisClass("Calculation");
            outputClass = ds.getJEVisClass("Output");
            rawDataClass = ds.getJEVisClass("Data");
            cleanDataClass = ds.getJEVisClass("Clean Data");

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (ds == null) {
            return;
        }

        List<JEVisObject> rawData = new ArrayList<>();
        try {
            rawData = getAllRawDataRec(jeVisObject, rawDataClass);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<JEVisObject> allCalculations = new ArrayList<>();
        try {
            allCalculations = ds.getObjects(calculationClass, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<JEVisObject> allTargets = new ArrayList<>();
        Map<JEVisObject, JEVisObject> targetAndCalc = new HashMap<>();
        for (JEVisObject calcObject : allCalculations) {
            try {
                for (JEVisObject output : calcObject.getChildren(outputClass, false)) {
                    JEVisAttribute attribute = output.getAttribute("Output");
                    if (attribute != null && attribute.hasSample()) {
                        try {
                            TargetHelper th = new TargetHelper(ds, attribute);
                            if (!th.getObject().isEmpty()) {
                                allTargets.addAll(th.getObject());
                                targetAndCalc.put(th.getObject().get(0), calcObject);
                            }
                        } catch (Exception e) {
                            logger.error("Error with output {}:{}", output.getName(), output.getID(), e);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<JEVisObject> foundCalcTarget = new ArrayList<>();
        List<JEVisObject> calculationsToDisable = new ArrayList<>();
        for (JEVisObject target : allTargets) {
            if (rawData.contains(target)) {
                foundCalcTarget.add(target);
                calculationsToDisable.add(targetAndCalc.get(target));
            }
        }

        for (JEVisObject concernedCalculation : calculationsToDisable) {
            setEnabled(concernedCalculation, "Calculation", false);
        }

        deleteSamplesInList(from, to, foundCalcTarget);

        List<JEVisObject> allCleanData = new ArrayList<>();
        for (JEVisObject data : foundCalcTarget) {
            try {
                allCleanData.addAll(data.getChildren(cleanDataClass, false));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        deleteSamplesInList(from, to, allCleanData);

        for (JEVisObject concernedCalculation : calculationsToDisable) {
            setEnabled(concernedCalculation, "Calculation", true);
        }
    }

    public static List<JEVisObject> getAllRawDataRec(JEVisObject parent, JEVisClass rawDataClass) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();
        if (parent.getJEVisClass() != null && parent.getJEVisClass().equals(rawDataClass)) {
            list.add(parent);
        }
        for (JEVisObject child : parent.getChildren()) {
            list.addAll(getAllRawDataRec(child, rawDataClass));
        }
        return list;
    }
}
