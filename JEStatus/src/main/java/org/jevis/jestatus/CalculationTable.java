package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.alarm.AlarmTable;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.object.plugin.TargetHelper;
import org.joda.time.DateTime;

import java.util.*;

public class CalculationTable extends AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CalculationTable.class);
    private final List<JEVisObject> dataServerObjects;
    private final DateTime furthestReported;
    private final DateTime latestReported;

    public CalculationTable(JEVisDataSource ds, DateTime furthestReported, DateTime latestReported, List<JEVisObject> dataServerObjects) {
        super(ds);
        this.dataServerObjects = dataServerObjects;
        this.furthestReported = furthestReported;
        this.latestReported = latestReported;

        try {
            createTableString();
        } catch (JEVisException e) {
            logger.error("Could not initialize.");
        }
    }

    private void createTableString() throws JEVisException {
        StringBuilder sb = new StringBuilder();
        sb.append("<br>");
        sb.append("<br>");

        sb.append("<h2>").append(I18n.getInstance().getString("status.table.title.calculations")).append("</h2>");

        /**
         * Start of Table
         */
        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.organisation")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.building")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.calculation")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.resulttarget")).append("</th>");
        sb.append("    <th>").append(I18n.getInstance().getString("status.table.captions.lasttimestamp")).append("</th>");
        sb.append("  </tr>");//border=\"0\"


        List<JEVisObject> calcObjects = getCalcObjects();

        Map<JEVisObject, JEVisObject> calcAndTarget = new HashMap<>();

        for (JEVisObject calcObject : calcObjects) {
            List<JEVisObject> results = new ArrayList<>(calcObject.getChildren(getOutputClass(), true));
            if (!results.isEmpty()) {
                calcAndTarget.put(calcObject, results.get(0));
            }
        }

        Map<JEVisObject, JEVisObject> calcAndResult = new HashMap<>();
        List<JEVisObject> outOfBounds = new ArrayList<>();

        for (JEVisObject calculation : calcObjects) {
            JEVisObject result = calcAndTarget.get(calculation);
            if (result != null) {
                JEVisAttribute lastAtt = result.getAttribute(OUTPUT_ATTRIBUTE_NAME);
                if (lastAtt != null) {
                    JEVisSample lastSampleOutput = lastAtt.getLatestSample();
                    TargetHelper th = null;
                    if (lastSampleOutput != null) {
                        th = new TargetHelper(ds, lastSampleOutput.getValueAsString());
                        JEVisObject target = null;
                        if (!th.getObject().isEmpty()) {
                            target = th.getObject().get(0);
                        }
                        if (target != null) {
                            getListCheckedData().add(target);

                            calcAndResult.put(calculation, target);

                            JEVisAttribute resultAtt = target.getAttribute(VALUE_ATTRIBUTE_NAME);
                            if (resultAtt != null) {
                                if (resultAtt.hasSample()) {
                                    JEVisSample lastSample = resultAtt.getLatestSample();
                                    if (lastSample != null) {
                                        if (lastSample.getTimestamp().isBefore(latestReported) && lastSample.getTimestamp().isAfter(furthestReported)) {
                                            outOfBounds.add(calculation);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Map<JEVisObject, JEVisObject> allInputs = new HashMap<>();

        for (JEVisObject calcObject : outOfBounds) {
            for (JEVisObject input : calcObject.getChildren(getInputClass(), true)) {
                JEVisAttribute lastAtt = input.getAttribute(INPUT_DATA_ATTRIBUTE_NAME);
                if (lastAtt != null) {
                    JEVisSample lastSampleOutput = lastAtt.getLatestSample();
                    TargetHelper th = null;
                    if (lastSampleOutput != null) {
                        th = new TargetHelper(ds, lastSampleOutput.getValueAsString());
                        JEVisObject target = null;
                        if (!th.getObject().isEmpty()) {
                            target = th.getObject().get(0);
                        }
                        if (target != null) {
                            if (target.getJEVisClass().equals(getRawDataClass())) {
                                allInputs.put(target, calcObject);
                            } else if (target.getJEVisClass().equals(getCleanDataClass())) {
                                for (JEVisObject parent : target.getParents()) {
                                    allInputs.put(parent, calcObject);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }

        for (JEVisObject dataServerObject : dataServerObjects) {
            if (allInputs.containsKey(dataServerObject)) {
                outOfBounds.remove(allInputs.get(dataServerObject));
            }
        }

        outOfBounds.sort(new Comparator<JEVisObject>() {
            @Override
            public int compare(JEVisObject o1, JEVisObject o2) {
                DateTime o1ts = null;
                try {
                    JEVisObject o1tar = calcAndResult.get(o1);
                    if (o1tar != null) {
                        JEVisAttribute o1att = o1tar.getAttribute(VALUE_ATTRIBUTE_NAME);
                        if (o1att != null) {
                            o1ts = o1att.getTimestampFromLastSample();
                        }
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
                DateTime o2ts = null;
                try {
                    JEVisObject o2tar = calcAndResult.get(o2);
                    if (o2tar != null) {
                        JEVisAttribute o2att = o2tar.getAttribute(VALUE_ATTRIBUTE_NAME);
                        if (o2att != null) {
                            o2ts = o2att.getTimestampFromLastSample();
                        }
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                if ((o1ts != null && o2ts != null && o1ts.isBefore(o2ts))) return -1;
                else if ((o1ts != null && o2ts != null && o1ts.isAfter(o2ts))) return 1;
                else return 0;
            }
        });

        boolean odd = false;
        for (JEVisObject currentCalculation : outOfBounds) {
            String css = rowCss;
            if (odd) {
                css += highlight;
            }
            odd = !odd;

            String name = currentCalculation.getName() + ":" + currentCalculation.getID().toString();
            String nameResult = "";
            JEVisObject resultObject = null;
            try {
                resultObject = calcAndResult.get(currentCalculation);
            } catch (Exception e) {
            }
            if (resultObject != null) nameResult = resultObject.getName() + ":" + resultObject.getID().toString();

            sb.append("<tr>");
            /**
             * Organisation Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(getParentName(currentCalculation, getOrganizationClass()));
            sb.append("</td>");
            /**
             * Building Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(getParentName(currentCalculation, getBuildingClass()));
            sb.append("</td>");
            /**
             * Calculation
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(name);
            sb.append("</td>");
            /**
             * Result Target Data Point
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(nameResult);
            sb.append("</td>");
            /**
             * Last Time Stamp
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            if (resultObject != null) {
                JEVisAttribute resultAtt = resultObject.getAttribute(VALUE_ATTRIBUTE_NAME);
                if (resultAtt != null) {
                    if (resultAtt.hasSample()) {
                        sb.append(dtf.print(resultAtt.getLatestSample().getTimestamp()));
                    }
                }
            }
            sb.append("</td>");

            sb.append("</tr>");
        }

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<br>");

        setTableString(sb.toString());
    }

    private List<JEVisObject> getCalcObjects() throws JEVisException {
        return new ArrayList<>(ds.getObjects(getCalculationClass(), false));
    }
}
