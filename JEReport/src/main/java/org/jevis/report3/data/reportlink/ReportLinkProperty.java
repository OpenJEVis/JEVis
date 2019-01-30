/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.database.JEVisAttributeDAO;
import org.jevis.commons.database.JEVisObjectDataManager;
import org.jevis.commons.database.JEVisSampleDAO;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.report3.data.DataHelper;
import org.jevis.report3.data.attribute.*;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.process.LastSampleGenerator;
import org.jevis.report3.process.PeriodSampleGenerator;
import org.joda.time.DateTime;
import org.joda.time.Interval;

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
            linkObject = reportLinkObject;
            templateVariableName = reportLinkObject.getAttribute(ReportLink.TEMPLATE_VARIABLE_NAME).getLatestSample().getValueAsString();
            Long jevisID = reportLinkObject.getAttribute(ReportLink.JEVIS_ID).getLatestSample().getValueAsLong();
            dataObject = reportLinkObject.getDataSource().getObject(jevisID);
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
        //attributeProperties.addAll(linkProperty.getDefaultAttributeProperties());
        attributeProperties.parallelStream().forEach(attributeProperty -> {
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
                                Interval interval = null;
                                IntervalCalculator.PeriodMode mode = null;
                                try {
                                    String modeName = config.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.PERIOD).getLatestSample().getValueAsString();
                                    mode = IntervalCalculator.PeriodMode.valueOf(modeName.toUpperCase());
                                    interval = intervalCalc.getInterval(mode);
                                    logger.info("interval: " + interval);
                                    logger.error("Mode name: " + modeName.toUpperCase());
                                } catch (JEVisException ex) {
                                    logger.error(ex);
                                }

                                AttributeConfiguration periodConfiguration = attributeProperty.getAttributeConfiguration(AttributeConfigurationFactory.ReportConfigurationName.Period);
                                JEVisObject dataObject = linkProperty.getDataObject();
                                JEVisAttribute attribute = null;
                                JEVisDataSource ds = null;
                                try {
                                    attribute = dataObject.getAttribute(attributeProperty.getAttributeName());
                                    ds = dataObject.getDataSource();
                                } catch (JEVisException ex) {
                                    logger.error(ex);
                                }

                                String modeName = null;
                                try {
                                    modeName = periodConfiguration.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.AGGREGATION).getLatestSample().getValueAsString();
                                    logger.error("Mode name: " + modeName);
                                } catch (JEVisException ex) {
                                    logger.error(ex);
                                }

                                ManipulationMode manipulationMode = ManipulationMode.get(modeName.toUpperCase());
                                logger.error("manipulationMode: " + manipulationMode.toString());
                                AggregationPeriod period = AggregationPeriod.get(modeName.toUpperCase());
                                logger.error("aggregationPeriod: " + period.toString());
                                Long duration = interval.toDurationMillis();
                                switch (period) {
                                    case HOURLY:
                                        long hourly = 3600000L;
                                        if (interval.toDurationMillis() < hourly) {
                                            DateTime start = new DateTime(interval.getEnd().getYear(), interval.getEnd().getMonthOfYear(), interval.getEnd().getDayOfMonth(), interval.getEnd().getHourOfDay(), 0, 0);
                                            DateTime end = interval.getEnd();
                                            interval = new Interval(start, end);
                                        }
                                        break;
                                    case DAILY:
                                        long daily = 86400000L;
                                        if (interval.toDurationMillis() < daily) {
                                            DateTime start = new DateTime(interval.getEnd().getYear(), interval.getEnd().getMonthOfYear(), interval.getEnd().getDayOfMonth(), 0, 0, 0);
                                            DateTime end = interval.getEnd();
                                            interval = new Interval(start, end);
                                        }
                                        break;
                                    case WEEKLY:
                                        long weekly = 604800000L;
                                        if (interval.toDurationMillis() < weekly) {
                                            DateTime end = interval.getEnd();
                                            int dayOfWeek = end.getDayOfWeek();

                                            DateTime start = new DateTime(interval.getEnd().getYear(), interval.getEnd().getMonthOfYear(), interval.getEnd().getDayOfMonth(), 0, 0, 0).minusDays(dayOfWeek);

                                            interval = new Interval(start, end);
                                        }
                                        break;
                                    case MONTHLY:
                                        long monthly = 16934400000L;
                                        if (interval.toDurationMillis() < monthly) {
                                            DateTime start = new DateTime(interval.getEnd().getYear(), interval.getEnd().getMonthOfYear(), 1, 0, 0, 0);
                                            DateTime end = interval.getEnd();
                                            interval = new Interval(start, end);
                                        }
                                        break;
                                    case QUARTERLY:
                                        long quarterly = 50803200000L;
                                        if (interval.toDurationMillis() < quarterly) {
                                            DateTime start;
                                            DateTime end = interval.getEnd();
                                            if (end.getMonthOfYear() < 4) {
                                                start = new DateTime(interval.getEnd().getYear(), 1, 1, 0, 0, 0);
                                            } else if (end.getMonthOfYear() < 7) {
                                                start = new DateTime(interval.getEnd().getYear(), 4, 1, 0, 0, 0);

                                            } else if (end.getMonthOfYear() < 10) {
                                                start = new DateTime(interval.getEnd().getYear(), 7, 1, 0, 0, 0);
                                            } else {
                                                start = new DateTime(interval.getEnd().getYear(), 10, 1, 0, 0, 0);
                                            }

                                            interval = new Interval(start, end);
                                        }
                                        break;
                                    case YEARLY:
                                        long yearly = 203212800000L;
                                        if (interval.toDurationMillis() < yearly) {
                                            DateTime start = new DateTime(interval.getEnd().getYear(), 1, 1, 0, 0, 0);
                                            DateTime end = interval.getEnd();
                                            interval = new Interval(start, end);
                                        }
                                        break;
                                }
                                PeriodSampleGenerator gen = new PeriodSampleGenerator(ds, dataObject, attribute, interval, manipulationMode, period);

                                try {
                                    linkMap.putAll(gen.work(linkProperty, attributeProperty, property));
                                    logger.debug("added link map " + linkMap.entrySet() + " to attribute map");
                                } catch (JEVisException e) {
                                    logger.error(e);
                                }
                            }
                            break;
                            case SpecificValue:
                                LastSampleGenerator sampleGenerator = new LastSampleGenerator();

                                try {
                                    linkMap.putAll(sampleGenerator.work(linkProperty, attributeProperty, property));
                                    logger.debug("added link map " + linkMap.entrySet() + " to attribute map");
                                } catch (JEVisException e) {
                                    logger.error(e);
                                }
                                break;
                        }
                    }
                }
            }
        });

        return linkMap;
    }

    @Override
    public LinkStatus getReportLinkStatus(DateTime end) {
        boolean optional = false;
        try {
            if (linkObject.getAttribute("Optional") != null && linkObject.getAttribute("Optional").getLatestSample() != null) {
                optional = linkObject.getAttribute("Optional").getLatestSample().getValueAsBoolean();
            }
        } catch (JEVisException ex) {
            logger.error(ex);
        }

        if (attributeProperties.isEmpty() || optional) {
            return new LinkStatus(true, "ok");
        }
        for (ReportAttributeProperty curProperty : attributeProperties) {
            String attributeName = curProperty.getAttributeName();
            if (attributeName.equals("Value")) {
                try {
                    if (dataObject.getAttribute("Value") != null) {
                        JEVisAttribute att = dataObject.getAttribute("Value");
                        if (att.getTimestampFromLastSample() != null) {
                            DateTime timestampFromLastSample = att.getTimestampFromLastSample();
                            if (timestampFromLastSample.isAfter(end)) {
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
                } catch (JEVisException ex) {
                    logger.error(ex);
                }
            }
        }
        return new LinkStatus(true, "ok"); //should not be reachable
    }

}
