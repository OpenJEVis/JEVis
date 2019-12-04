/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.process;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.SampleGenerator;
import org.jevis.report3.data.attribute.ReportAttributeProperty;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.reportlink.ReportLinkProperty;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author broder
 */
public class PeriodSampleGenerator extends SampleGenerator {

    boolean isValid = false;
    private Interval interval;
    private static final Logger logger = LogManager.getLogger(PeriodSampleGenerator.class);

    public PeriodSampleGenerator(JEVisDataSource ds, JEVisObject object, JEVisAttribute attribute, DateTime from, DateTime until, ManipulationMode manipulationMode, AggregationPeriod aggregationPeriod) {
        super(ds, object, attribute, from, until, manipulationMode, aggregationPeriod);
    }

    public PeriodSampleGenerator(JEVisDataSource ds, JEVisObject object, JEVisAttribute attribute, Interval interval, ManipulationMode manipulationMode, AggregationPeriod aggregationPeriod) {
        super(ds, object, attribute, interval, manipulationMode, aggregationPeriod);
    }

    public ConcurrentHashMap<String, Object> work(ReportLinkProperty linkData, ReportAttributeProperty attributeData, ReportProperty property) throws JEVisException {
        JEVisObject dataObject = linkData.getDataObject();
        JEVisAttribute attribute = dataObject.getAttribute(attributeData.getAttributeName());

        List<JEVisSample> aggregatedSamples = getAggregatedSamples();
        logger.debug("Generated " + aggregatedSamples.size() + " aggregated samples.");

        ConcurrentHashMap<String, Object> sampleMap = ProcessHelper.getAttributeSamples(aggregatedSamples, attribute, property.getTimeZone());
        logger.debug("Created sample map with " + sampleMap.size() + " entries.");

        return sampleMap;
    }
}
