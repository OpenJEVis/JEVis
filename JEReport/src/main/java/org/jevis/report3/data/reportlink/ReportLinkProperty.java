/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.JEVisAttributeDAO;
import org.jevis.commons.database.JEVisObjectDataManager;
import org.jevis.commons.database.JEVisSampleDAO;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.FixedPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.report.PeriodMode;
import org.jevis.report3.data.DataHelper;
import org.jevis.report3.data.attribute.*;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.process.LastSampleGenerator;
import org.jevis.report3.process.ProcessHelper;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author broder
 */
public class ReportLinkProperty implements ReportData {
    private static final Logger logger = LogManager.getLogger(ReportLinkProperty.class);

    private String templateVariableName;
    //    private JEVisObject linkObject;
    private final List<JEVisObject> attributePropertyObjects = new ArrayList<>();
    //    private Long jevisID;
    private JEVisObject dataObject;
    private final List<ReportAttributeProperty> attributeProperties = new ArrayList<>();
    private final List<ReportAttributeProperty> defaultAttributeProperties = new ArrayList<>();
    private final LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private final LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);
    private Boolean isCalculation;

    //    private DateTime latestTimestamp;
    public static ReportLinkProperty buildFromJEVisObject(JEVisObject reportLinkObject) {
        return new ReportLinkProperty(reportLinkObject);
    }

    private JEVisObject linkObject;

    private ReportLinkProperty(JEVisObject reportLinkObject) {
        initialize(reportLinkObject);
    }

    private void initialize(JEVisObject reportLinkObject) {

        //init Attributes
        initializeAttributes(reportLinkObject);

        //init Attribute Properties
        initializeAttributeProperties(reportLinkObject);

        //handle the attribute with no property object and set to default
        initDefaultProperties();
    }

    private void initializeAttributes(JEVisObject reportLinkObject) {
        try {
            SampleHandler sampleHandler = new SampleHandler();
            linkObject = reportLinkObject;
            templateVariableName = reportLinkObject.getAttribute(ReportLink.TEMPLATE_VARIABLE_NAME).getLatestSample().getValueAsString();
            Long jevisID = reportLinkObject.getAttribute(ReportLink.JEVIS_ID).getLatestSample().getValueAsLong();
            dataObject = reportLinkObject.getDataSource().getObject(jevisID);
            isCalculation = sampleHandler.getLastSample(reportLinkObject, ReportLink.CALCULATION, false);
            if (!DataHelper.checkAllObjectsNotNull(linkObject, templateVariableName, jevisID, dataObject)) {
                throw new RuntimeException("One Sample missing for report link Object: id: " + reportLinkObject.getID() + " and name: " + reportLinkObject.getName());
            }
        } catch (JEVisException ex) {
            throw new RuntimeException("Error while parsing attributes for report Object: id: " + reportLinkObject.getID() + " and name: " + reportLinkObject.getName(), ex);
        }
    }

    private void initializeAttributeProperties(JEVisObject reportLinkObject) {
        try {
            JEVisClass reportAttributeClass = reportLinkObject.getDataSource().getJEVisClass(ReportAttribute.NAME);
            attributePropertyObjects.addAll(reportLinkObject.getChildren(reportAttributeClass, true));

            //iterate over attribute property objects
            for (JEVisObject attributeObject : attributePropertyObjects) {
                ReportAttributeProperty attrProperty = new ReportAttributeProperty(new JEVisSampleDAO(new JEVisAttributeDAO()), new JEVisObjectDataManager(), new AttributeConfigurationFactory()); //Todo change constructor
                attrProperty.initialize(attributeObject);
                attributeProperties.add(attrProperty);
            }

        } catch (JEVisException ex) {
            throw new RuntimeException("Error while catching attribute property objects for report Object: id: " + reportLinkObject.getID() + " and name: " + reportLinkObject.getName(), ex);
        }
    }

    public List<ReportAttributeProperty> getAttributeProperties() {
        return attributeProperties;
    }

    public String getTemplateVariableName() {
        return templateVariableName;
    }

    //    public Long getJevisID() {
//        return jevisID;
//    }
    @Override
    public JEVisObject getDataObject() {
        return dataObject;
    }

    //    public JEVisObject getLinkObject() {
//        return linkObject;
//    }
//    public DateTime getLatestTimestamp() {
//        return latestTimestamp;
//    }
    public List<JEVisObject> getAttributePropertyObjects() {
        return attributePropertyObjects;
    }

    private void initDefaultProperties() {
        try {
            for (JEVisAttribute attribute : dataObject.getAttributes()) {
                //look in the raw attribute and identify missing attributes
                boolean found = false;
                for (ReportAttributeProperty attributeProp : attributeProperties) {
                    if (attributeProp.getAttributeName().equalsIgnoreCase(attribute.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    defaultAttributeProperties.add(ReportAttributeProperty.buildDefault(attribute.getName()));
                }
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
    }

    public List<ReportAttributeProperty> getDefaultAttributeProperties() {
        return defaultAttributeProperties;
    }

    @Override
    public ConcurrentHashMap<String, Object> getReportMap(ReportProperty property, IntervalCalculator intervalCalc) {
        ConcurrentHashMap<String, Object> templateMap = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, Object> reportLinkMap = getMapFromReportLink(this, property, intervalCalc);
        templateMap.put(this.getTemplateVariableName(), reportLinkMap);
        return templateMap;
    }

    private ConcurrentHashMap<String, Object> getMapFromReportLink(ReportLinkProperty linkProperty, ReportProperty property, IntervalCalculator intervalCalc) {
        ConcurrentHashMap<String, Object> linkMap = new ConcurrentHashMap<>();
        List<ReportAttributeProperty> attributeProperties = linkProperty.getAttributeProperties();
        attributeProperties.addAll(linkProperty.getDefaultAttributeProperties());
        for (ReportAttributeProperty attributeProperty : attributeProperties) {
            List<AttributeConfiguration> attributeConfigs = attributeProperty.getAttributeConfigurations();

            boolean validSampleGenerator = false;
            //get the sampleGenerator
            for (AttributeConfiguration config : attributeConfigs) {
                if (config.getConfigType().equals(AttributeConfigurationFactory.ReportConfigurationType.SampleGenerator)) {
                    if (validSampleGenerator) {
                        logger.info("valid sample generators");
                    } else {
                        validSampleGenerator = true;

                        switch (config.getConfigName()) {
                            case Period: {

                                AttributeConfiguration periodConfiguration = attributeProperty.getAttributeConfiguration(AttributeConfigurationFactory.ReportConfigurationName.Period);
                                JEVisObject dataObject = linkProperty.getDataObject();
                                JEVisAttribute attribute = null;
                                JEVisDataSource ds = null;
                                try {
                                    attribute = dataObject.getAttribute(attributeProperty.getAttributeName());
                                    ds = dataObject.getDataSource();
                                } catch (JEVisException ex) {
                                    logger.error("Could not get attribute or datasource with name {} of object {}:{}", attributeProperty.getAttributeName(), dataObject.getName(), dataObject.getID(), ex);
                                }

                                String aggregationName = null;
                                String manipulationName = null;
                                try {
                                    if (periodConfiguration.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.AGGREGATION).hasSample()) {
                                        aggregationName = periodConfiguration.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.AGGREGATION).getLatestSample().getValueAsString();
                                    }
                                } catch (Exception ex) {
                                    logger.error("Could not get aggregation name", ex);
                                }

                                try {
                                    if (periodConfiguration.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.MANIPULATION).hasSample()) {
                                        manipulationName = periodConfiguration.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.MANIPULATION).getLatestSample().getValueAsString();
                                    }
                                } catch (Exception ex) {
                                    logger.error("Could not get manipulation name", ex);
                                }

                                AggregationPeriod aggregationPeriod = AggregationPeriod.NONE;

                                if (aggregationName != null) {
                                    aggregationPeriod = AggregationPeriod.get(aggregationName.toUpperCase());
                                }
                                logger.debug("aggregationPeriod: {}", aggregationPeriod.toString());

                                ManipulationMode manipulationMode = ManipulationMode.NONE;
                                if (manipulationName != null) {
                                    manipulationMode = ManipulationMode.parseManipulation(manipulationName.toUpperCase());
                                } else if (aggregationName != null) {
                                    manipulationMode = ManipulationMode.get(aggregationName.toUpperCase());
                                }
                                logger.debug("manipulationMode: {}", manipulationMode.toString());

                                Interval interval = null;
                                PeriodMode mode = null;
                                try {
                                    String modeName = config.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.PERIOD).getLatestSample().getValueAsString();
                                    mode = PeriodMode.valueOf(modeName.toUpperCase());

                                    if (mode != PeriodMode.FIXED && mode != PeriodMode.FIXED_TO_REPORT_END) {
                                        interval = intervalCalc.getInterval(mode.toString().toUpperCase());
                                    } else {
                                        String fixedPeriodName = config.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.FIXED_PERIOD).getLatestSample().getValueAsString();
                                        FixedPeriod fixedPeriod = FixedPeriod.valueOf(fixedPeriodName.toUpperCase());
                                        String name;
                                        if (mode == PeriodMode.FIXED) {
                                            name = PeriodMode.FIXED.toString().toUpperCase() + "_" + fixedPeriod.toString().toUpperCase();
                                        } else {
                                            name = PeriodMode.FIXED_TO_REPORT_END.toString().toUpperCase() + "_" + fixedPeriod.toString().toUpperCase();
                                        }

                                        interval = intervalCalc.getInterval(name);
                                    }

                                    logger.info("variable named {} for interval: {} getting data from object {}:{} of attribute {}",
                                            linkProperty.templateVariableName, interval, dataObject.getName(), dataObject.getID(), attribute.getName());

                                } catch (JEVisException ex) {
                                    logger.error(ex);
                                }

                                if (!isCalculation) {
                                    List<JEVisSample> samples = attribute.getSamples(interval.getStart(), interval.getEnd(), true, aggregationPeriod.toString(), manipulationMode.toString(), property.getTimeZone().getID());
                                    linkMap.putAll(ProcessHelper.getAttributeSamples(samples, attribute, property.getTimeZone()));
                                } else {
                                    try {
                                        CalcJobFactory calcJobCreator = new CalcJobFactory();
                                        if (dataObject.getJEVisClassName().equals("Clean Data")) {
                                            List<JEVisObject> parents = dataObject.getParents();
                                            if (!parents.isEmpty()) {
                                                JEVisObject parentDataObject = parents.get(0);
                                                CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), ds, ReportLinkFactory.getEnPICalcMap(ds).get(parentDataObject),
                                                        interval.getStart(), interval.getEnd(), aggregationPeriod);

                                                linkMap.putAll(ProcessHelper.getAttributeSamples(calcJob.getResults(), attribute, property.getTimeZone()));
                                            }
                                        } else if (dataObject.getJEVisClassName().equals("Data")) {
                                            CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), ds, ReportLinkFactory.getEnPICalcMap(ds).get(dataObject),
                                                    interval.getStart(), interval.getEnd(), aggregationPeriod);

                                            linkMap.putAll(ProcessHelper.getAttributeSamples(calcJob.getResults(), attribute, property.getTimeZone()));
                                        }
                                    } catch (JEVisException e) {
                                        e.printStackTrace();
                                    }
                                }
                                logger.debug("added link map {} to attribute map", linkMap.entrySet());
                            }
                            break;
                            case SpecificValue:
                                LastSampleGenerator sampleGenerator = new LastSampleGenerator();

                                try {
                                    linkMap.putAll(sampleGenerator.work(linkProperty, attributeProperty, property));
                                    logger.debug("added link map {}  to attribute map", linkMap.entrySet());
                                } catch (JEVisException e) {
                                    logger.error(e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        String objectName = linkProperty.dataObject.getName();
        if (objectName != null) {
            ConcurrentHashMap<String, Object> objectname = new ConcurrentHashMap<>();
            objectname.put("value", objectName);
            linkMap.put("objectname", objectname);
        }

        return linkMap;
    }

    @Override
    public LinkStatus getReportLinkStatus(DateTime end) {

        boolean optional = false;
        try {
            if (linkObject.getAttribute(ReportLink.OPTIONAL) != null && linkObject.getAttribute(ReportLink.OPTIONAL).getLatestSample() != null) {
                optional = linkObject.getAttribute(ReportLink.OPTIONAL).getLatestSample().getValueAsBoolean();
            }
        } catch (Exception ex) {
            logger.error(ex);
            return new LinkStatus(false, "No data available for jevis data object with id " + dataObject.getID());
        }

        if (attributeProperties.isEmpty() || optional) {
            return new LinkStatus(true, "ok");
        }
        for (ReportAttributeProperty curProperty : attributeProperties) {
            String attributeName = curProperty.getAttributeName();
            if (attributeName.equals("Value") || attributeName.equals("value")) {
                try {
                    if (dataObject.getAttribute("Value") != null) {
                        JEVisAttribute att = dataObject.getAttribute("Value");
                        if (att != null) {
                            JEVisSample latestSample = att.getLatestSample();
                            if (latestSample != null) {
                                if (latestSample.getTimestamp().isAfter(end)) {
                                    return new LinkStatus(true, "ok");
                                } else {
                                    return new LinkStatus(false, "No data available for jevis data object with id " + dataObject.getID());
                                }
                            } else {
                                return new LinkStatus(false, "No data available for jevis data object with id " + dataObject.getID());
                            }
                        } else {
                            return new LinkStatus(false, "No data available for jevis data object with id " + dataObject.getID());
                        }
                    } else {
                        return new LinkStatus(false, "No data available for jevis data object with id " + dataObject.getID());
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                    return new LinkStatus(false, "No data available for jevis data object with id " + dataObject.getID());
                }
            }
        }
        if (!defaultAttributeProperties.isEmpty()) {
            return new LinkStatus(true, "No attributes, but there are default attributes, which are not checked for new data.");
        } else {
            return new LinkStatus(false, "should not be reachable"); //should not be reachable
        }
    }

}
