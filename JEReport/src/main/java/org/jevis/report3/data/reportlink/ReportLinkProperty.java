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
import org.jevis.commons.dataprocessing.*;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.datetime.WorkDays;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.report.PeriodMode;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.report3.data.DataHelper;
import org.jevis.report3.data.attribute.*;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.report.intervals.IntervalCalculator;
import org.jevis.report3.process.LastSampleGenerator;
import org.jevis.report3.process.ProcessHelper;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author broder
 */
public class ReportLinkProperty implements ReportData {
    private static final Logger logger = LogManager.getLogger(ReportLinkProperty.class);
    //    private JEVisObject linkObject;
    private final List<JEVisObject> attributePropertyObjects = new ArrayList<>();
    private final List<ReportAttributeProperty> attributeProperties = new ArrayList<>();
    private final List<ReportAttributeProperty> defaultAttributeProperties = new ArrayList<>();
    private final LocalTime workdayStart = LocalTime.of(0, 0, 0, 0);
    private final LocalTime workdayEnd = LocalTime.of(23, 59, 59, 999999999);
    private String templateVariableName;
    //    private Long jevisID;
    private JEVisObject dataObject;
    private Boolean isCalculation;
    private WorkDays workDays = null;
    private JEVisObject linkObject;

    private ReportLinkProperty(JEVisObject reportLinkObject) {
        initialize(reportLinkObject);
        this.workDays = new WorkDays(reportLinkObject);
    }

    //    private DateTime latestTimestamp;
    public static ReportLinkProperty buildFromJEVisObject(JEVisObject reportLinkObject) {
        return new ReportLinkProperty(reportLinkObject);
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
            TargetHelper targetHelper = new TargetHelper(reportLinkObject.getDataSource(), reportLinkObject.getAttribute(ReportLink.JEVIS_ID));
            dataObject = targetHelper.getObject().get(0);
            isCalculation = sampleHandler.getLastSample(reportLinkObject, ReportLink.CALCULATION, false);
            if (!DataHelper.checkAllObjectsNotNull(linkObject, templateVariableName, dataObject)) {
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
    public Map<String, Object> getReportMap(ReportProperty property, IntervalCalculator intervalCalc) {
        Map<String, Object> templateMap = new ConcurrentHashMap<>();
        Map<String, Object> reportLinkMap = getMapFromReportLink(this, property, intervalCalc);
        templateMap.put(this.getTemplateVariableName(), reportLinkMap);
        return templateMap;
    }

    private Map<String, Object> getMapFromReportLink(ReportLinkProperty linkProperty, ReportProperty property, IntervalCalculator intervalCalc) {
        Map<String, Object> linkMap = new HashMap<>();
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
                                try {
                                    IntervalCalculator intervalCalculator = intervalCalc;
                                    org.jevis.commons.datetime.Period schedule = org.jevis.commons.datetime.Period.valueOf(intervalCalculator.getSchedule());

                                    AttributeConfiguration periodConfiguration = attributeProperty.getAttributeConfiguration(AttributeConfigurationFactory.ReportConfigurationName.Period);
                                    JEVisObject dataObject = linkProperty.getDataObject();

                                    JEVisAttribute attribute = dataObject.getAttribute(attributeProperty.getAttributeName());
                                    JEVisDataSource ds = dataObject.getDataSource();

                                    JEVisAttribute aggregationAttribute = periodConfiguration.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.AGGREGATION);
                                    String aggregationName = AggregationPeriod.NONE.toString();
                                    if (aggregationAttribute.hasSample()) {
                                        aggregationName = aggregationAttribute.getLatestSample().getValueAsString();
                                    } else {
                                        logger.warn("No aggregation configuration set, selecting no aggregation");
                                    }

                                    AggregationPeriod aggregationPeriod = AggregationPeriod.get(aggregationName.toUpperCase());

                                    logger.debug("aggregationPeriod: {}", aggregationPeriod.toString());

                                    JEVisAttribute manipulationAttribute = periodConfiguration.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.MANIPULATION);
                                    String manipulationName = ManipulationMode.NONE.toString();
                                    if (manipulationAttribute.hasSample()) {
                                        manipulationName = manipulationAttribute.getLatestSample().getValueAsString();
                                    } else {
                                        logger.warn("No manipulation configuration set, selecting no manipulation");
                                    }
                                    ManipulationMode manipulationMode = ManipulationMode.NONE;
                                    if (manipulationName != null) {
                                        manipulationMode = ManipulationMode.parseManipulation(manipulationName.toUpperCase());
                                    } else {
                                        manipulationMode = ManipulationMode.get(aggregationName.toUpperCase());
                                    }
                                    logger.debug("manipulationMode: {}", manipulationMode.toString());

                                    Interval interval = null;

                                    JEVisAttribute periodAttribute = config.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.PERIOD);
                                    String modeName = PeriodMode.CURRENT.toString();
                                    if (periodAttribute.hasSample()) {
                                        modeName = periodAttribute.getLatestSample().getValueAsString();
                                    } else {
                                        logger.warn("No period configuration set, selecting current");
                                    }
                                    PeriodMode mode = PeriodMode.valueOf(modeName.toUpperCase());

                                    JEVisAttribute fixedPeriodAttribute = config.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.FIXED_PERIOD);
                                    String fixedPeriodName = FixedPeriod.NONE.toString();
                                    if (fixedPeriodAttribute.hasSample()) {
                                        fixedPeriodName = fixedPeriodAttribute.getLatestSample().getValueAsString();
                                    } else {
                                        logger.warn("No fixed period configuration set, selecting none");
                                    }
                                    FixedPeriod fixedPeriod = FixedPeriod.valueOf(fixedPeriodName.toUpperCase());

                                    JEVisAttribute overrideScheduleAttribute = config.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.OVERRIDE_SCHEDULE);
                                    String overrideSchedule = "NONE";
                                    if (overrideScheduleAttribute.hasSample() || mode == PeriodMode.RELATIVE) {
                                        overrideSchedule = overrideScheduleAttribute.getLatestSample().getValueAsString();
                                        intervalCalculator = new IntervalCalculator(new SampleHandler());
                                        intervalCalculator.buildIntervals(intervalCalc.getReportObject());

                                        if (!overrideSchedule.equals("NONE")) {
                                            DateTime newStart = intervalCalculator.alignDateToSchedule(overrideSchedule, intervalCalculator.getStart());
                                            intervalCalculator.buildIntervals(overrideSchedule, newStart, true, schedule);
                                        }
                                    }

                                    switch (mode) {
                                        case CURRENT:
                                        case LAST:
                                        case ALL:
                                            interval = intervalCalculator.getInterval(mode.toString().toUpperCase());
                                            break;
                                        case FIXED:
                                        case FIXED_TO_REPORT_END:
                                        case RELATIVE:

                                            String name;
                                            if (mode == PeriodMode.FIXED) {
                                                name = PeriodMode.FIXED.toString().toUpperCase() + "_" + fixedPeriod.toString().toUpperCase();
                                            } else if (mode == PeriodMode.FIXED_TO_REPORT_END) {
                                                name = PeriodMode.FIXED_TO_REPORT_END.toString().toUpperCase() + "_" + fixedPeriod.toString().toUpperCase();
                                            } else {
                                                name = PeriodMode.RELATIVE.toString().toUpperCase() + "_" + fixedPeriod.toString().toUpperCase();
                                            }

                                            interval = intervalCalculator.getInterval(name);
                                            break;
                                    }

                                    logger.info("variable named {} for interval: {} getting data from object {}:{} of attribute {}",
                                            linkProperty.templateVariableName, interval, dataObject.getName(), dataObject.getID(), attribute.getName());

                                    if (!isCalculation) {
                                        List<JEVisSample> samples;
                                        if (aggregationPeriod != AggregationPeriod.NONE && mode == PeriodMode.RELATIVE) {
                                            DateTime newStart = intervalCalculator.alignDateToSchedule(aggregationPeriod.toPeriod().toString(), interval.getStart());
                                            samples = attribute.getSamples(newStart, interval.getEnd(), true, AggregationPeriod.NONE.toString(), manipulationMode.toString(), property.getTimeZone().getID());
                                            QuantityUnits qu = new QuantityUnits();
                                            JEVisUnit unit = attribute.getDisplayUnit();
                                            boolean isQuantity = qu.isQuantityUnit(attribute.getDisplayUnit());
                                            isQuantity = qu.isQuantityIfCleanData(attribute, isQuantity);

                                            double sum = 0d;
                                            for (JEVisSample jeVisSample : samples) {
                                                sum += jeVisSample.getValueAsDouble();
                                            }

                                            if (!isQuantity && !samples.isEmpty()) {
                                                sum = sum / samples.size();
                                            }

                                            JEVisSample resultSum = new VirtualSample(interval.getStart(), sum, unit, ds, attribute);
                                            samples.clear();
                                            samples.add(resultSum);
                                        } else {
                                            if (workDays.isEnabled()) {
                                                DateTime start = interval.getStart().withHourOfDay(workDays.getWorkdayStart().getHour()).withMinuteOfHour(workDays.getWorkdayStart().getMinute()).withSecondOfMinute(workDays.getWorkdayStart().getSecond());
                                                DateTime end = interval.getEnd().withHourOfDay(workDays.getWorkdayEnd().getHour()).withMinuteOfHour(workDays.getWorkdayEnd().getMinute()).withSecondOfMinute(workDays.getWorkdayEnd().getSecond());

                                                if (workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart())) {
                                                    start = start.minusDays(1);
                                                }

                                                interval = new Interval(start, end);
                                            }

                                            samples = attribute.getSamples(interval.getStart(), interval.getEnd(), true, aggregationPeriod.toString(), manipulationMode.toString(), property.getTimeZone().getID());
                                        }

                                        linkMap.putAll(ProcessHelper.getAttributeSamples(samples, attribute, property.getTimeZone()));
                                    } else {
                                        CalcJobFactory calcJobCreator = new CalcJobFactory();
                                        if (dataObject.getJEVisClassName().equals("Clean Data")) {

                                            JEVisObject parentDataObject = CommonMethods.getFirstParentalDataObject(dataObject);
                                            if (parentDataObject != null) {
                                                CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), ds, ReportLinkFactory.getEnPICalcMap(ds).get(parentDataObject),
                                                        interval.getStart(), interval.getEnd(), aggregationPeriod);

                                                linkMap.putAll(ProcessHelper.getAttributeSamples(calcJob.getResults(), attribute, property.getTimeZone()));
                                            }
                                        } else if (dataObject.getJEVisClassName().equals("Data")) {
                                            CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), ds, ReportLinkFactory.getEnPICalcMap(ds).get(dataObject),
                                                    interval.getStart(), interval.getEnd(), aggregationPeriod);

                                            linkMap.putAll(ProcessHelper.getAttributeSamples(calcJob.getResults(), attribute, property.getTimeZone()));
                                        }
                                    }
                                    logger.debug("added link map {} to attribute map", linkMap.entrySet());
                                } catch (Exception ex) {
                                    logger.error("Error while creating period sample generator for attribute {} of object {}:{}", attributeProperty.getAttributeName(), dataObject.getName(), dataObject.getID(), ex);
                                }
                            }
                            break;
                            case SpecificValue:
                                LastSampleGenerator sampleGenerator = new LastSampleGenerator();

                                try {
                                    linkMap.putAll(sampleGenerator.work(linkProperty, attributeProperty, property));
                                    logger.debug("added link map {}  to attribute map", linkProperty);
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

    private String getNoDataMessage() {

        String name = "";
        try {
            JEVisClass cleanDataClass = dataObject.getDataSource().getJEVisClass("Data");

            if (!dataObject.getJEVisClass().equals(cleanDataClass)) {
                JEVisObject firstParentalDataObject = CommonMethods.getFirstParentalDataObject(dataObject);
                name += firstParentalDataObject.getName() + " \\ ";
            }
        } catch (Exception e) {
            logger.error("Could not get parental data object name", e);
        }

        name += dataObject.getName();

        return "No data available for data object " + name + " with id " + dataObject.getID();
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
            return new LinkStatus(false, getNoDataMessage());
        }

        if (attributeProperties.isEmpty() || optional) {
            return new LinkStatus(true, "ok");
        }

        Period p = null;
        try {
            p = CleanDataObject.getPeriodForDate(dataObject, end);
        } catch (Exception e) {
            logger.error(e);
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
                                if (p != null && latestSample.getTimestamp().isAfter(end.minus(p))) {
                                    return new LinkStatus(true, "ok");
                                } else if (p == null && latestSample.getTimestamp().isAfter(end)) {
                                    return new LinkStatus(true, "ok");
                                } else if (workDays != null && workDays.getWorkdayEnd().isBefore(workDays.getWorkdayStart()) && p != null && PeriodHelper.isGreaterThenDays(p)) {
                                    end = end.withHourOfDay(workDays.getWorkdayEnd().getHour()).withMinuteOfHour(workDays.getWorkdayEnd().getMinute()).withSecondOfMinute(workDays.getWorkdayEnd().getSecond());
                                    end = end.minus(p);
                                    if (latestSample.getTimestamp().isAfter(end)) {
                                        return new LinkStatus(true, "ok");
                                    } else return new LinkStatus(false, getNoDataMessage());
                                } else {
                                    return new LinkStatus(false, getNoDataMessage());
                                }
                            } else {
                                return new LinkStatus(false, getNoDataMessage());
                            }
                        } else {
                            return new LinkStatus(false, getNoDataMessage());
                        }
                    } else {
                        return new LinkStatus(false, getNoDataMessage());
                    }
                } catch (Exception ex) {
                    logger.error(ex);
                    return new LinkStatus(false, getNoDataMessage());
                }
            }
        }
        if (!defaultAttributeProperties.isEmpty()) {
            return new LinkStatus(true, "No attributes, but there are default attributes, which are not checked for new data.");
        } else {
            return new LinkStatus(false, "should not be reachable"); //should not be reachable
        }
    }


    @Override
    public String toString() {
        return "ReportLinkProperty{" +
                "linkObject=" + linkObject + '\'' +
                ", isCalculation=" + isCalculation + '\'' +
                ", templateVariableName=" + templateVariableName +
                '}';
    }
}
