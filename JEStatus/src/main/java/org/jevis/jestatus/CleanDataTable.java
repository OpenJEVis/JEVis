package org.jevis.jestatus;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.alarm.AlarmTable;
import org.joda.time.DateTime;

import java.util.*;

public class CleanDataTable extends AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(CleanDataTable.class);
    private final JEVisDataSource ds;
    private final List<JEVisObject> dataServerObjects;
    private final List<JEVisObject> calcObjects;
    private final DateTime furthestReported;
    private final DateTime latestReported;

    public CleanDataTable(JEVisDataSource ds, DateTime furthestReported, DateTime latestReported, List<JEVisObject> calcObjects, List<JEVisObject> dataServerObjects) {
        this.ds = ds;
        this.calcObjects = calcObjects;
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

        List<JEVisObject> outOfBounds = new ArrayList<>();
        List<JEVisObject> cleanDataObjects = getCleanDataObjects();
        Map<JEVisObject, JEVisObject> dataAndCleanData = new HashMap<>();
        for (JEVisObject cleanDataObject : cleanDataObjects) {
            List<JEVisObject> parents = cleanDataObject.getParents();
            if (parents.size() > 0) {
                dataAndCleanData.put(parents.get(0), cleanDataObject);
            }
        }

        for (JEVisObject calcObject : calcObjects) {
            JEVisObject obj = dataAndCleanData.get(calcObject);
            if (obj != null) {
                cleanDataObjects.remove(obj);
            }
        }

        for (JEVisObject calcObject : dataServerObjects) {
            JEVisObject obj = dataAndCleanData.get(calcObject);
            if (obj != null) {
                cleanDataObjects.remove(obj);
            }
        }

        for (JEVisObject obj : cleanDataObjects) {
            JEVisSample lastSample = obj.getAttribute("Value").getLatestSample();
            if (lastSample != null) {
                if (lastSample.getTimestamp().isBefore(latestReported) && lastSample.getTimestamp().isAfter(furthestReported)) {
                    outOfBounds.add(obj);
                }
            }
        }

        outOfBounds.sort(new Comparator<JEVisObject>() {
            @Override
            public int compare(JEVisObject o1, JEVisObject o2) {
                DateTime o1ts = null;
                try {
                    JEVisAttribute o1att = o1.getAttribute("Value");
                    if (o1att != null) {
                        o1ts = o1att.getTimestampFromLastSample();
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
                DateTime o2ts = null;
                try {
                    JEVisAttribute o2att = o2.getAttribute("Value");
                    if (o2att != null) {
                        o2ts = o2att.getTimestampFromLastSample();
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }

                if ((o1ts != null && o2ts != null && o1ts.isBefore(o2ts))) return -1;
                else if ((o1ts != null && o2ts != null && o1ts.isAfter(o2ts))) return 1;
                else return 0;
            }
        });

        sb.append("<br>");
        sb.append("<br>");

        sb.append("<h2>Clean Data</h2>");

        /**
         * Start of Table
         */
        sb.append("<table style=\"");
        sb.append(tableCSS);
        sb.append("\" border=\"1\" >");
        sb.append("<tr style=\"");
        sb.append(headerCSS);
        sb.append("\" >");
        sb.append("    <th>Organisation</th>");
        sb.append("    <th>Building</th>");
        sb.append("    <th>Raw Datapoint</th>");
        sb.append("    <th>Last Raw Value</th>");
        sb.append("    <th>Clean Datapoint Class</th>");
        sb.append("    <th>Last Clean Value</th>");
        sb.append("  </tr>");//border=\"0\"

        JEVisClass organizationClass = ds.getJEVisClass("Organization");
        JEVisClass buildingClass = ds.getJEVisClass("Monitored Object");
        JEVisClass rawDataClass = ds.getJEVisClass("Data");

        boolean odd = false;
        for (JEVisObject currentCleanDataObject : outOfBounds) {
            String name = currentCleanDataObject.getName() + ":" + currentCleanDataObject.getID().toString();
            String nameRaw = "";

            boolean hasRawDataObject = false;
            JEVisObject currentRawDataObject = null;
            for (JEVisObject parent : currentCleanDataObject.getParents()) {
                try {
                    JEVisClass parentClass = parent.getJEVisClass();
                    if (parentClass != null && parentClass.equals(rawDataClass)) {
                        hasRawDataObject = true;
                        currentRawDataObject = parent;
                        break;
                    }
                } catch (JEVisException e) {
                    e.printStackTrace();
                }
            }

            if (hasRawDataObject) {
                nameRaw = currentRawDataObject.getName() + ":" + currentRawDataObject.getID().toString();
            }

            String css = rowCss;
            if (odd) {
                css += highlight;
            }
            odd = !odd;

            sb.append("<tr>");
            /**
             * Organisation Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(getParentName(currentCleanDataObject, organizationClass));
            sb.append("</td>");
            /**
             * Building Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(getParentName(currentCleanDataObject, buildingClass));
            sb.append("</td>");
            /**
             * Raw Datapoint Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            if (hasRawDataObject) {
                sb.append(nameRaw);
            }
            sb.append("</td>");
            /**
             * Last Raw Value Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            if (hasRawDataObject) {
                JEVisSample smp = currentRawDataObject.getAttribute("Value").getLatestSample();
                if (smp != null) {
                    sb.append(dtf.print(smp.getTimestamp()));
                }
            }
            sb.append("</td>");
            /**
             * Clean Datapoint Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(name);
            sb.append("</td>");
            /**
             * Last Clean Value Column
             */
            sb.append("<td style=\"");
            sb.append(css);
            sb.append("\">");
            sb.append(dtf.print(currentCleanDataObject.getAttribute("Value").getLatestSample().getTimestamp()));
            sb.append("</td>");

            sb.append("</tr>");// style=\"border: 1px solid #D9E4E6;\">");

        }

        sb.append("</tr>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");

        setTableString(sb.toString());
    }

    private List<JEVisObject> getCleanDataObjects() throws JEVisException {
        JEVisClass cleanDataClass = ds.getJEVisClass("Clean Data");
        return new ArrayList<>(ds.getObjects(cleanDataClass, false));
    }
}
