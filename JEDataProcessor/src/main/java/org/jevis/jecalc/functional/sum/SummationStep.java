/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.functional.sum;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jecalc.data.CleanDataAttributeJEVis;
import org.jevis.jecalc.data.ResourceManager;
import org.jevis.jecalc.functional.aggregation.AggregationJob;
import org.jevis.jecalc.functional.aggregation.Aggregator;
import org.jevis.jecalc.workflow.ProcessStep;

import java.util.List;

/**
 * @author broder
 */
public class SummationStep implements ProcessStep {
    private static final Logger logger = LogManager.getLogger(SummationStep.class);
    private final JEVisObject functionalObject;

    public SummationStep(JEVisObject functionalObject) {
        this.functionalObject = functionalObject;
    }

    @Override
    public void run(ResourceManager resourceManager) {
        logger.debug("Start Functional Step with id {}", functionalObject.getID());
        CleanDataAttributeJEVis jevisAttribute = (CleanDataAttributeJEVis) resourceManager.getCalcAttribute();
        JEVisObject cleanData = jevisAttribute.getObject();
        Aggregator aggr = new Aggregator();
        AggregationJob createAggregationJob = AggregationJob.createAggregationJob(functionalObject, cleanData, Aggregator.AggregationModus.total);
        List<JEVisSample> aggregatedData = aggr.getAggregatedData(createAggregationJob);
        SampleHandler sampleHandler = new SampleHandler();
        try {
            JEVisAttribute attribute = functionalObject.getAttribute("Value");
            sampleHandler.importDataAndReplaceSorted(aggregatedData, attribute);
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

}
