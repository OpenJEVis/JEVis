package org.jevis.jeconfig.application.jevistree.methods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.jeconfig.dialog.ProgressForm;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculationMethods extends CommonMethods {
    private static final Logger logger = LogManager.getLogger(CalculationMethods.class);

    public static void deleteAllCalculations(ProgressForm pForm, JEVisObject jeVisObject, DateTime from, DateTime to) {
        JEVisClass calculationClass = null;
        JEVisClass outputClass = null;
        JEVisClass cleanDataClass = null;
        JEVisClass rawDataClass = null;
        try {
            calculationClass = jeVisObject.getDataSource().getJEVisClass("Calculation");
            outputClass = jeVisObject.getDataSource().getJEVisClass("Output");
            rawDataClass = jeVisObject.getDataSource().getJEVisClass("Data");
            cleanDataClass = jeVisObject.getDataSource().getJEVisClass("Clean Data");
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        List<JEVisObject> rawData = new ArrayList<>();
        try {
            rawData = getAllRawDataRec(jeVisObject, rawDataClass);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        List<JEVisObject> allCalculations = new ArrayList<>();
        try {
            allCalculations = jeVisObject.getDataSource().getObjects(calculationClass, false);
        } catch (JEVisException e) {
            e.printStackTrace();
        }

        List<JEVisObject> allTargets = new ArrayList<>();
        Map<JEVisObject, JEVisObject> targetAndCalc = new HashMap<>();
        for (JEVisObject calcObject : allCalculations) {
            try {
                for (JEVisObject output : calcObject.getChildren(outputClass, false)) {
                    JEVisAttribute attribute = output.getAttribute("Output");
                    if (attribute != null && attribute.hasSample()) {
                        TargetHelper th = new TargetHelper(jeVisObject.getDataSource(), attribute);
                        allTargets.addAll(th.getObject());
                        targetAndCalc.put(th.getObject().get(0), calcObject);
                    }
                }
            } catch (JEVisException e) {
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

        deleteSamplesInList(pForm, from, to, foundCalcTarget);

        List<JEVisObject> allCleanData = new ArrayList<>();
        for (JEVisObject data : foundCalcTarget) {
            try {
                allCleanData.addAll(data.getChildren(cleanDataClass, false));
            } catch (JEVisException e) {
                e.printStackTrace();
            }
        }

        deleteSamplesInList(pForm, from, to, allCleanData);

        for (JEVisObject concernedCalculation : calculationsToDisable) {
            setEnabled(concernedCalculation, "Calculation", true);
        }
    }

    private static List<JEVisObject> getAllRawDataRec(JEVisObject parent, JEVisClass rawDataClass) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();
        if (parent.getJEVisClass().equals(rawDataClass)) {
            list.add(parent);
        }
        for (JEVisObject child : parent.getChildren()) {
            list.addAll(getAllRawDataRec(child, rawDataClass));
        }
        return list;
    }
}
