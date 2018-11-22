/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.reportlink;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisClass;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.commons.database.JEVisAttributeDAO;
import org.jevis.commons.database.JEVisObjectDataManager;
import org.jevis.commons.database.JEVisSampleDAO;
import org.jevis.report3.ReportLauncher;
import org.jevis.report3.context.SampleFactory;
import org.jevis.report3.data.DataHelper;
import org.jevis.report3.data.attribute.AttributeConfiguration;
import org.jevis.report3.data.attribute.AttributeConfigurationFactory;
import org.jevis.report3.data.attribute.ReportAttribute;
import org.jevis.report3.data.attribute.ReportAttributeProperty;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.process.SampleGenerator;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


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
    private final SampleFactory sampleFactory = new SampleFactory();

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
            dataObject = ReportLauncher.getDataSource().getObject(jevisID);
            if (!DataHelper.checkAllObjectsNotNull(linkObject, templateVariableName, jevisID, dataObject)) {
                throw new RuntimeException("One Sample missing for report link Object: id: " + reportLinkObject.getID() + " and name: " + reportLinkObject.getName());
            }
        } catch (JEVisException ex) {
            throw new RuntimeException("Error while parsing attributes for report Object: id: " + reportLinkObject.getID() + " and name: " + reportLinkObject.getName(), ex);
        }
    }

    private void initializeAttributeProperties(JEVisObject reportLinkObject) {
        try {
            JEVisClass reportAttributeClass = ReportLauncher.getDataSource().getJEVisClass(ReportAttribute.NAME);
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
        Map<String, Object> templateMap = new HashMap<>();
        Map<String, Object> reportLinkMap = getMapFromReportLink(this, property, intervalCalc);
        templateMap.put(this.getTemplateVariableName(), reportLinkMap);
        return templateMap;
    }

    private Map<String, Object> getMapFromReportLink(ReportLinkProperty linkProperty, ReportProperty property, IntervalCalculator intervalCalc) {
        Map<String, Object> linkMap = new ConcurrentHashMap<>();
        List<ReportAttributeProperty> attributeProperties = linkProperty.getAttributeProperties();
        //attributeProperties.addAll(linkProperty.getDefaultAttributeProperties());
        attributeProperties.parallelStream().forEach(attributeProperty -> {
            List<AttributeConfiguration> attributeConfigs = attributeProperty.getAttributeConfigurations();
            SampleGenerator sampleGenerator = sampleFactory.getSampleGenerator(attributeConfigs, intervalCalc);
            Map<String, Object> attributeMap = sampleGenerator.work(linkProperty, attributeProperty, property);
            addAttributeMapToLinkMap(linkMap, attributeMap);
            logger.debug("added link map " + linkMap.entrySet() + " to attribute map");
        });

        return linkMap;
    }

    //    public void setIntervalCalculator(IntervalCalculator intervalCalc) {
//        this.intervalCalc = intervalCalc;
//    }
    private void addAttributeMapToLinkMap(Map<String, Object> linkMap, Map<String, Object> attributeMap) {
        Lock lock = new ReentrantLock();
        lock.lock();
        linkMap.putAll(attributeMap);
        lock.unlock();
    }

    //    @Override
//    public JEVisObject getLinkObject() {
//        return linkObject
//    }
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
