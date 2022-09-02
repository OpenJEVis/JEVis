package org.jevis.commons.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.processor.workflow.ProcessManager;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.*;

public class CommonMethods {
    private static final Logger logger = LogManager.getLogger(CommonMethods.class);

    public static List<String> DATA_CHILDREN = Arrays.asList("Clean Data", "Math Data", "Forecast Data");
    public static List<String> DATA_TYPES = Arrays.asList("Data", "Clean Data", "Base Data", "Math Data", "String Data");

    public static JEVisObject getFirstParentalDataObject(JEVisObject jeVisObject) throws JEVisException {
        for (JEVisObject object : jeVisObject.getParents()) {
            if (object.getJEVisClassName().equals("Data")) {
                return object;
            } else {
                return getFirstParentalDataObject(object);
            }
        }
        return jeVisObject;
    }

    public static JEVisObject getFirstParentalObjectOfClass(JEVisObject jeVisObject, String className) throws JEVisException {
        for (JEVisObject parent : jeVisObject.getParents()) {
            if (parent.getJEVisClassName().equals(className)) {
                return parent;
            } else {
                return getFirstParentalObjectOfClass(parent, className);
            }
        }
        return jeVisObject;
    }

    public static JEVisObject getFirstCleanObject(JEVisObject jeVisObject) throws JEVisException {
        for (JEVisObject object : jeVisObject.getChildren()) {
            if (object.getJEVisClassName().equals("Clean data")) {
                return object;
            } else {
                return getFirstCleanObject(object);
            }
        }
        return jeVisObject;
    }

    public static void setEnabled(JEVisObject object, String selectedClass, boolean b) {
        try {
            if (object.getJEVisClassName().equals(selectedClass) || selectedClass.equals("All")) {
                JEVisAttribute enabled = object.getAttribute("Enabled");
                if (enabled != null) {
                    JEVisSample sample = enabled.buildSample(new DateTime(), b);
                    sample.commit();
                    logger.info("Set enabled attribute of object {}:{} to {}", object.getName(), object.getID(), b);
                }
            }
            for (JEVisObject child : object.getChildren()) {
                setEnabled(child, selectedClass, b);
            }
        } catch (Exception e) {
            logger.error("Could not set enabled for {}:{}", object.getName(), object.getID());
        }
    }

    public static void deleteSamplesInList(DateTime from, DateTime to, List<JEVisObject> list) {
        for (JEVisObject object : list) {
            JEVisAttribute valueAtt = null;
            try {
                valueAtt = object.getAttribute("Value");
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (valueAtt != null) {
                if (from == null && to == null) {
                    try {
                        logger.info("Deleting all samples of object " + object.getName() + ":" + object.getID());
                        valueAtt.deleteAllSample();

                        allSamplesMathData(object, true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (from != null && to != null) {
                    try {
                        logger.info("Deleting samples of object " + object.getName() + ":" + object.getID()
                                + " from " + from.toString("YYYY-MM-dd HH:mm:ss") + " to " + to.toString("YYYY-MM-dd HH:mm:ss"));
                        valueAtt.deleteSamplesBetween(from, to);

                        fromToMathData(object, true, from, to);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (from != null) {
                    try {
                        logger.info("Deleting samples of object " + object.getName() + ":" + object.getID()
                                + " from " + from.toString("YYYY-MM-dd HH:mm:ss") + " to " + new DateTime().toString("YYYY-MM-dd HH:mm:ss"));
                        DateTime t = new DateTime();
                        valueAtt.deleteSamplesBetween(from, t);

                        fromToMathData(object, true, from, t);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        logger.info("Deleting samples of object " + object.getName() + ":" + object.getID()
                                + " from " + new DateTime(1990, 1, 1, 0, 0, 0).toString("YYYY-MM-dd HH:mm:ss") + " to " + to.toString("YYYY-MM-dd HH:mm:ss"));
                        DateTime f = new DateTime(1990, 1, 1, 0, 0, 0);
                        valueAtt.deleteSamplesBetween(f, to);

                        fromToMathData(object, true, f, to);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            logger.info("Deleting samples of object " + object.getName() + ":" + object.getID());
        }
    }

    public static void deleteAllSamples(JEVisObject object, boolean rawData, boolean cleanData) {
        try {
            JEVisAttribute value = object.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
            if (value != null) {
                if (((object.getJEVisClassName().equals("Clean Data") || object.getJEVisClassName().equals("Math Data")) && cleanData)
                        || (object.getJEVisClassName().equals("Data") && rawData)) {
                    logger.info("Deleting all samples of object " + object.getName() + ":" + object.getID());
                    value.deleteAllSample();

                    allSamplesMathData(object, cleanData);
                }
            }
            for (JEVisObject child : object.getChildren()) {
                deleteAllSamples(child, rawData, cleanData);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", object.getName(), object.getID());
        }
    }

    private static void allSamplesMathData(JEVisObject object, boolean cleanData) throws JEVisException {
        if (object.getJEVisClassName().equals("Math Data") && cleanData) {
            try {
                JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
                if (lastRunAttribute != null) {
                    List<JEVisSample> allSamples = lastRunAttribute.getAllSamples();
                    if (allSamples.size() > 1) {
                        allSamples.remove(0);
                        DateTime finalTS = allSamples.get(0).getTimestamp();
                        DateTime lastTS = allSamples.get(allSamples.size() - 1).getTimestamp();

                        lastRunAttribute.deleteSamplesBetween(finalTS, lastTS);
                    }
                }

            } catch (JEVisException e) {
                logger.error("Could not get math data last run time: ", e);
            }
        }
    }

    public static void deleteAllSamples(JEVisObject object, DateTime from, DateTime to, boolean rawData, boolean cleanData) {
        try {
            JEVisAttribute value = object.getAttribute(CleanDataObject.AttributeName.VALUE.getAttributeName());
            if (value != null) {
                if (((object.getJEVisClassName().equals("Clean Data") || object.getJEVisClassName().equals("Math Data")) && cleanData)
                        || (object.getJEVisClassName().equals("Data") && rawData)) {
                    DateTime f = null;
                    if (from == null) {
                        f = new DateTime(1990, 1, 1, 0, 0, 0);
                    } else {
                        f = from;
                    }

                    DateTime t = null;
                    if (to == null) {
                        t = new DateTime();
                    } else {
                        t = to;
                    }
                    logger.info("Deleting samples of object " + object.getName() + ":" + object.getID()
                            + " from " + f.toString("YYYY-MM-dd HH:mm:ss") + " to " + t.toString("YYYY-MM-dd HH:mm:ss"));
                    value.deleteSamplesBetween(f, t);

                    fromToMathData(object, cleanData, f, t);
                }
            }
            for (JEVisObject child : object.getChildren()) {
                deleteAllSamples(child, from, to, rawData, cleanData);
            }
        } catch (JEVisException e) {
            logger.error("Could not delete value samples for {}:{}", object.getName(), object.getID());
        }
    }

    private static void fromToMathData(JEVisObject object, boolean cleanData, DateTime f, DateTime t) throws JEVisException {
        if (object.getJEVisClassName().equals("Math Data") && cleanData) {
            try {
                JEVisAttribute lastRunAttribute = object.getAttribute("Last Run");
                JEVisAttribute periodOffsetAttribute = object.getAttribute("Period Offset");
                JEVisAttribute periodAttribute = object.getAttribute("Period");
                if (lastRunAttribute != null && periodOffsetAttribute != null && periodAttribute != null) {
                    List<JEVisSample> allSamples = lastRunAttribute.getAllSamples();
                    Long periodOffset = periodOffsetAttribute.getLatestSample().getValueAsLong();
                    Period period = new Period(periodAttribute.getLatestSample().getValueAsString());

                    if (periodOffset > 0) {
                        f = PeriodHelper.minusPeriodToDate(f, period);
                    } else if (periodOffset < 0) {
                        f = PeriodHelper.addPeriodToDate(f, period);
                    }

                    if (allSamples.size() > 0) {
                        allSamples.remove(0);
                        DateTime finalTS = null;
                        for (JEVisSample sample : allSamples) {
                            if (new DateTime(sample.getValueAsString()).isAfter(f)) {
                                finalTS = sample.getTimestamp();
                                break;
                            }
                        }

                        lastRunAttribute.deleteSamplesBetween(finalTS, t);
                    }
                }

            } catch (JEVisException e) {
                logger.error("Could not get math data last run time: ", e);
            }
        }
    }

    public static DateTime getStartDateFromSampleRate(JEVisAttribute attribute) {
        if (attribute.hasSample()) {
            DateTime start = attribute.getTimestampFromLastSample().minusDays(7);
            JEVisAttribute periodAttribute = null;
            try {
                periodAttribute = attribute.getObject().getAttribute("Period");
            } catch (JEVisException e) {
                e.printStackTrace();
            }
            if (periodAttribute != null) {
                JEVisSample latestSample = periodAttribute.getLatestSample();
                Period p = Period.ZERO;
                try {
                    p = new Period(latestSample.getValueAsString());
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                if (p.equals(Period.years(1))) {
                    start = attribute.getTimestampFromLastSample().minusYears(10);
                } else if (p.equals(Period.months(1))) {
                    start = attribute.getTimestampFromLastSample().minusMonths(12);
                } else if (p.equals(Period.weeks(1))) {
                    start = attribute.getTimestampFromLastSample().minusWeeks(10);
                } else if (p.equals(Period.days(1))) {
                    start = attribute.getTimestampFromLastSample().minusDays(14);
                } else if (p.equals(Period.hours(1))) {
                    start = attribute.getTimestampFromLastSample().minusDays(2);
                } else if (p.equals(Period.minutes(15))) {
                    start = attribute.getTimestampFromLastSample().minusHours(24);
                } else if (p.equals(Period.minutes(1))) {
                    start = attribute.getTimestampFromLastSample().minusHours(6);
                }
            }
            return start;
        }
        return DateTime.now().minusHours(12);
    }

    public static void processCleanData(JEVisObject cleanDataObject, DateTime from, DateTime to) throws Exception {
        cleanDataObject.getAttribute("Enabled").buildSample(new DateTime(), false).commit();

        deleteAllSamples(cleanDataObject, from, to, false, true);

        ProcessManager processManager = new ProcessManager(
                cleanDataObject,
                new ObjectHandler(cleanDataObject.getDataSource()), getProcessingSizeFromService(cleanDataObject.getDataSource(), "JEDataProcessor")
        );
        processManager.start();

        cleanDataObject.getAttribute("Enabled").buildSample(new DateTime(), true).commit();
        logger.info("cleaning done for: {}:{}", cleanDataObject.getName(), cleanDataObject.getID());
    }

    public static void processAllCleanData(JEVisObject cleanDataObject, DateTime from, DateTime to) throws Exception {
        processCleanData(cleanDataObject, from, to);

        for (JEVisObject jeVisObject : cleanDataObject.getChildren()) {
            processAllCleanData(jeVisObject, from, to);
        }
    }

    public static void processCleanData(JEVisObject cleanDataObject) throws Exception {
        cleanDataObject.getAttribute("Enabled").buildSample(new DateTime(), false).commit();

        ProcessManager processManager = new ProcessManager(
                cleanDataObject,
                new ObjectHandler(cleanDataObject.getDataSource()), getProcessingSizeFromService(cleanDataObject.getDataSource(), "JEDataProcessor")
        );
        processManager.start();

        cleanDataObject.getAttribute("Enabled").buildSample(new DateTime(), true).commit();
        logger.info("cleaning done for: {}:{}", cleanDataObject.getName(), cleanDataObject.getID());
    }

    public static void processAllCleanDataNoDelete(JEVisObject cleanDataObject) throws Exception {
        processCleanData(cleanDataObject);

        for (JEVisObject jeVisObject : cleanDataObject.getChildren()) {
            processAllCleanDataNoDelete(jeVisObject);
        }
    }

    public static int getProcessingSizeFromService(JEVisDataSource ds, String serviceClassName) {
        int size = 50000;
        try {
            JEVisClass serviceClass = ds.getJEVisClass(serviceClassName);
            List<JEVisObject> listServices = ds.getObjects(serviceClass, false);
            JEVisAttribute sizeAtt = listServices.get(0).getAttribute("Processing Size");
            if (sizeAtt != null && sizeAtt.hasSample()) {
                size = sizeAtt.getLatestSample().getValueAsLong().intValue();
            }

        } catch (Exception e) {
            logger.error("Couldn't get processing size from the JEVis System. Using standard Size of {}", "50.000", e);
        }
        return size;
    }

    public static void clearDependentData(DateTime from, List<JEVisObject> objects) throws Exception {
        List<JEVisObject> dependentObjects = new ArrayList<>();
        JEVisDataSource ds = objects.get(0).getDataSource();

        for (JEVisObject child : objects) {

            dependentObjects.add(child);

            try {
                for (JEVisObject object : getAllChildrenRecursive(child)) {
                    if (!dependentObjects.contains(object)) {
                        dependentObjects.add(object);
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        }

        Map<JEVisObject, List<JEVisObject>> targetAndCalculation = new HashMap<>();
        List<JEVisObject> dependentCalculations = new ArrayList<>();

        JEVisClass calculation = ds.getJEVisClass("Calculation");
        JEVisClass outputClass = ds.getJEVisClass("Output");
        JEVisClass inputClass = ds.getJEVisClass("Input");

        for (JEVisObject calculationObj : ds.getObjects(calculation, true)) {
            try {
                List<JEVisObject> outputs = calculationObj.getChildren(inputClass, true);

                if (outputs != null && !outputs.isEmpty()) {
                    for (JEVisObject output : outputs) {
                        JEVisAttribute targetAttribute = output.getAttribute("Output");
                        if (targetAttribute != null) {
                            try {
                                TargetHelper th = new TargetHelper(ds, targetAttribute);
                                if (th.getObject() != null && !th.getObject().isEmpty()) {
                                    JEVisObject id = th.getObject().get(0);
                                    if (targetAndCalculation.get(id) == null) {
                                        List<JEVisObject> objectIds = new ArrayList<>();
                                        objectIds.add(calculationObj);
                                        targetAndCalculation.put(id, objectIds);
                                    } else {
                                        List<JEVisObject> list = new ArrayList<>(targetAndCalculation.remove(id));
                                        list.add(calculationObj);
                                        targetAndCalculation.put(id, list);
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }


    }

    public static List<JEVisObject> getChildrenRecursive(JEVisObject firstObject, JEVisClass jeVisClass) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();
        if (firstObject.getJEVisClass().equals(jeVisClass)) list.add(firstObject);
        for (JEVisObject child : firstObject.getChildren()) {
            if (child.getJEVisClass().equals(jeVisClass)) list.add(child);

            for (JEVisObject secondChild : child.getChildren()) {
                list.addAll(getChildrenRecursive(secondChild, jeVisClass));
            }
        }

        return list;
    }

    public static List<JEVisObject> getAllChildrenRecursive(JEVisObject firstObject) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();
        list.add(firstObject);
        for (JEVisObject child : firstObject.getChildren()) {
            list.add(child);

            for (JEVisObject secondChild : child.getChildren()) {
                list.addAll(getAllChildrenRecursive(secondChild));
            }
        }

        return list;
    }

    public static List<JEVisObject> getChildrenRecursive(JEVisObject firstObject) throws JEVisException {
        List<JEVisObject> list = new ArrayList<>();
        for (JEVisObject child : firstObject.getChildren()) {
            list.add(child);

            for (JEVisObject secondChild : child.getChildren()) {
                list.addAll(getChildrenRecursive(secondChild));
            }
        }

        return list;
    }
}
