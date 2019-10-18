package org.jevis.commons.alarm;

import org.apache.logging.log4j.LogManager;
import org.jevis.api.*;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.utils.AlphanumComparator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AlarmTable {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(AlarmTable.class);
    public static final String OLD_STANDARD_PATTERN = "yyyy-MM-dd HH:mm:ss";
    protected final String tableCSS = "background-color:#FFF;"
            + "text-color: #024457;"
            + "width: 100%;"
            + "table-layout: fixed;"
            + "outer-border: 1px solid #167F92;"
            + "empty-cells:show;"
            + "border-collapse:collapse;"
            //                + "border: 2px solid #D9E4E6;"
            + "cell-border: 1px solid #D9E4E6";
    protected final String headerCSS = "background-color: #1a719c;"
            + "color: #FFF;";
    protected final String rowCss = "text-color: #024457;padding: 5px;";//"border: 1px solid #D9E4E6;"
    protected final String highlight = "background-color: #EAF3F3";
    protected final DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
    protected final String VALUE_ATTRIBUTE_NAME = "Value";
    protected final String LAST_READOUT_ATTRIBUTE_NAME = "Last Readout";
    protected final String LATEST_REPORTED = "Latest reported";
    protected final String ENABLED = "Enabled";
    protected final String FURTHEST_REPORTED = "Furthest reported";
    protected final String OUTPUT_ATTRIBUTE_NAME = "Output";
    protected final String INPUT_DATA_ATTRIBUTE_NAME = "Input Data";
    protected final String STANDARD_TARGET_ATTRIBUTE_NAME = "Target";
    protected JEVisDataSource ds;
    private List<JEVisObject> listCheckedData = new ArrayList<>();
    private String tableString;
    private JEVisClass vida350ChannelClass;
    private JEVisClass loytecXMLDLChannelClass;
    private JEVisClass organizationClass;
    private JEVisClass buildingClass;
    private JEVisClass ftpChannelClass;
    private JEVisClass httpChannelClass;
    private JEVisClass sFtpChannelClass;
    private JEVisClass soapChannelClass;
    private JEVisClass csvDataPointClass;

    private JEVisClass dwdDataPointClass;
    private JEVisClass xmlDataPointClass;
    private JEVisClass dataPointClass;
    private JEVisClass rawDataClass;

    private JEVisClass cleanDataClass;
    private JEVisClass channelClass;

    private JEVisClass outputClass;
    private JEVisClass inputClass;
    private JEVisClass calculationClass;
    private Comparator<JEVisObject> objectComparator = (o1, o2) -> {
        DateTime o1ts = getDateTime(o1);
        DateTime o2ts = getDateTime(o2);

        if ((o1ts != null && o2ts != null && o1ts.isBefore(o2ts))) return -1;
        else if ((o1ts != null && o2ts != null && o1ts.isAfter(o2ts))) return 1;
        else {
            if (o1ts != null && o1ts.equals(o2ts)) {
                String o1Name = getObjectName(o1);

                String o2Name = getObjectName(o2);

                AlphanumComparator ac = new AlphanumComparator();
                return ac.compare(o1Name, o2Name);
            } else return 0;
        }
    };

    private boolean isJEVisClassOrInherit(JEVisObject obj, JEVisClass jclass2) {
        try {
            return isJEVisClassOrInherit(obj.getJEVisClass(), jclass2);
        } catch (JEVisException ex) {
            logger.error(ex);
            return false;
        }
    }

    private boolean isJEVisClassOrInherit(JEVisClass jclass, JEVisClass jclass2) {
        try {
            if (jclass.equals(jclass2)) {
                return true;
            }

            return jclass2.getHeirs().contains(jclass);

        } catch (JEVisException ex) {
            logger.error(ex);
            return false;
        }

    }

    public List<JEVisObject> getListCheckedData() {
        return listCheckedData;
    }

    protected String getParentName(JEVisObject obj, JEVisClass jclass) {
        StringBuilder name = new StringBuilder();
        try {
            for (JEVisObject parent : obj.getParents()) {
                if (isJEVisClassOrInherit(parent, jclass)) {
                    name.append(parent.getName());
                } else {
                    name.append(getParentName(parent, jclass));
                }
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        }

        return name.toString();
    }

    public String getTableString() {
        return tableString;
    }

    public void setTableString(String tableString) {
        this.tableString = tableString;
    }

    public AlarmTable(JEVisDataSource dataSource) {
        try {
            this.ds = dataSource;
            organizationClass = dataSource.getJEVisClass("Organization");
            buildingClass = dataSource.getJEVisClass("Monitored Object");
            loytecXMLDLChannelClass = dataSource.getJEVisClass("Loytec XML-DL Channel");
            vida350ChannelClass = dataSource.getJEVisClass("VIDA350 Channel");
            ftpChannelClass = dataSource.getJEVisClass("FTP Channel");
            httpChannelClass = dataSource.getJEVisClass("HTTP Channel");
            sFtpChannelClass = dataSource.getJEVisClass("sFTP Channel");
            soapChannelClass = dataSource.getJEVisClass("SOAP Channel");
            csvDataPointClass = dataSource.getJEVisClass("CSV Data Point");
            dwdDataPointClass = dataSource.getJEVisClass("DWD Data Point");
            xmlDataPointClass = dataSource.getJEVisClass("XML Data Point");
            dataPointClass = dataSource.getJEVisClass("Data Point");
            rawDataClass = dataSource.getJEVisClass("Data");
            cleanDataClass = dataSource.getJEVisClass("Clean Data");
            channelClass = dataSource.getJEVisClass("Channel");
            outputClass = dataSource.getJEVisClass("Output");
            inputClass = dataSource.getJEVisClass("Input");
            calculationClass = dataSource.getJEVisClass("Calculation");
        } catch (JEVisException e) {
            e.printStackTrace();
        }
    }

    private DateTime getDateTime(JEVisObject object) {
        DateTime objectTS = new DateTime(2001, 1, 1, 0, 0, 0);
        try {
            if (object.getJEVisClass().equals(loytecXMLDLChannelClass) || object.getJEVisClass().equals(vida350ChannelClass)) {
                JEVisAttribute objectAttribute = object.getAttribute(LAST_READOUT_ATTRIBUTE_NAME);
                if (objectAttribute != null) {
                    JEVisSample objectSample = objectAttribute.getLatestSample();
                    if (objectSample != null) {
                        try {
                            try {
                                objectTS = new DateTime(objectSample.getValueAsString());
                            } catch (Exception e) {
                                objectTS = DateTimeFormat.forPattern(OLD_STANDARD_PATTERN).parseDateTime(objectSample.getValueAsString());
                            }
                        } catch (IllegalArgumentException e) {
                            logger.error("Could not parse, invalid datetime format: " + objectSample.getValueAsString()
                                    + " from object: " + object.getName() + ":" + object.getID() + " of attribute: " + objectAttribute.getName());
                        } catch (UnsupportedOperationException e) {
                            logger.error("Could not parse, unsupported operation: " + objectSample.getValueAsString()
                                    + " from object: " + object.getName() + ":" + object.getID() + " of attribute: " + objectAttribute.getName());
                        } catch (Exception e) {
                            logger.error("Could not get timestamp from object: " + object.getName() + ":" + object.getID() + " of attribute: " + objectAttribute.getName());
                        }
                    }
                }
            } else if (object.getJEVisClass().equals(rawDataClass) || object.getJEVisClass().equals(cleanDataClass)) {
                JEVisAttribute objectAttribute = object.getAttribute("Value");
                if (objectAttribute != null) {
                    JEVisSample o1smp = objectAttribute.getLatestSample();
                    if (o1smp != null) {
                        try {
                            objectTS = o1smp.getTimestamp();
                        } catch (JEVisException e) {
                            logger.error("Could not get timestamp from latest sample of object: " + object.getName() + ":" + object.getID() + " of attribute: " + objectAttribute.getName());
                        }
                    }
                }
            } else if (object.getJEVisClass().equals(csvDataPointClass) || object.getJEVisClass().equals(xmlDataPointClass)) {
                JEVisAttribute targetAtt = null;
                JEVisSample lastSampleTarget = null;

                targetAtt = object.getAttribute(STANDARD_TARGET_ATTRIBUTE_NAME);

                if (targetAtt != null) lastSampleTarget = targetAtt.getLatestSample();

                TargetHelper th = null;
                if (lastSampleTarget != null) {
                    th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
                    JEVisObject target = null;
                    if (th.getObject() != null && !th.getObject().isEmpty()) target = th.getObject().get(0);
                    if (target != null) {

                        JEVisAttribute resultAtt = null;
                        if (th.getAttribute() != null && !th.getAttribute().isEmpty()) {
                            resultAtt = th.getAttribute().get(0);
                        } else resultAtt = target.getAttribute(VALUE_ATTRIBUTE_NAME);

                        if (resultAtt != null) {
                            if (resultAtt.hasSample()) {
                                JEVisSample lastSample = resultAtt.getLatestSample();
                                if (lastSample != null) {
                                    objectTS = lastSample.getTimestamp();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not get datetime for sorting object: " + object.getName() + ":" + object.getID());
        }
        return objectTS;
    }

    public Comparator<JEVisObject> getObjectComparator() {
        return objectComparator;
    }


    private String getObjectName(JEVisObject o1) {
        try {
            if (o1.getJEVisClass().equals(rawDataClass) || o1.getJEVisClass().equals(cleanDataClass)) {
                return getParentName(o1, getOrganizationClass()) +
                        getParentName(o1, getBuildingClass()) +
                        o1.getName() + ":" + o1.getID().toString();
            } else if (o1.getJEVisClass().equals(loytecXMLDLChannelClass) || o1.getJEVisClass().equals(vida350ChannelClass)) {
                JEVisAttribute targetAtt = null;
                JEVisSample lastSampleTarget = null;
                if (o1.getJEVisClass().equals(getLoytecXMLDLChannelClass()))
                    targetAtt = o1.getAttribute("Target ID");
                else if (o1.getJEVisClass().equals(getVida350ChannelClass())) {
                    targetAtt = o1.getAttribute("Target");
                }

                if (targetAtt != null) lastSampleTarget = targetAtt.getLatestSample();

                TargetHelper th = null;
                if (lastSampleTarget != null) {
                    th = new TargetHelper(ds, lastSampleTarget.getValueAsString());
                    if (!th.getObject().isEmpty()) {
                        JEVisObject target = th.getObject().get(0);
                        if (target != null) {
                            return getParentName(target, getOrganizationClass()) +
                                    getParentName(target, getBuildingClass()) +
                                    target.getName() + ":" + target.getID().toString();
                        }
                    }
                }
            }
        } catch (JEVisException e) {
            logger.error(e);
        }
        return "";
    }

    public JEVisClass getOrganizationClass() {
        return organizationClass;
    }

    public JEVisClass getBuildingClass() {
        return buildingClass;
    }

    public JEVisClass getFtpChannelClass() {
        return ftpChannelClass;
    }

    public JEVisClass getHttpChannelClass() {
        return httpChannelClass;
    }

    public JEVisClass getsFtpChannelClass() {
        return sFtpChannelClass;
    }

    public JEVisClass getSoapChannelClass() {
        return soapChannelClass;
    }

    public JEVisClass getCsvDataPointClass() {
        return csvDataPointClass;
    }

    public JEVisClass getDwdDataPointClass() {
        return dwdDataPointClass;
    }

    public JEVisClass getXmlDataPointClass() {
        return xmlDataPointClass;
    }

    public JEVisClass getDataPointClass() {
        return dataPointClass;
    }

    public JEVisClass getRawDataClass() {
        return rawDataClass;
    }

    public JEVisClass getCleanDataClass() {
        return cleanDataClass;
    }

    public JEVisClass getChannelClass() {
        return channelClass;
    }

    public JEVisClass getLoytecXMLDLChannelClass() {
        return loytecXMLDLChannelClass;
    }

    public JEVisClass getVida350ChannelClass() {
        return vida350ChannelClass;
    }

    public JEVisClass getOutputClass() {
        return outputClass;
    }

    public JEVisClass getInputClass() {
        return inputClass;
    }

    public JEVisClass getCalculationClass() {
        return calculationClass;
    }
}
