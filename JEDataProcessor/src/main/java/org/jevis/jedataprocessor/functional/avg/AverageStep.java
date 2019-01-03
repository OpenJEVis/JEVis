/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.functional.avg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.SampleHandler;
import org.jevis.jedataprocessor.data.CleanDataObjectJEVis;
import org.jevis.jedataprocessor.data.ResourceManager;
import org.jevis.jedataprocessor.functional.aggregation.AggregationJob;
import org.jevis.jedataprocessor.functional.aggregation.Aggregator;
import org.jevis.jedataprocessor.workflow.ProcessStep;

import java.util.List;

/**
 * @author broder
 */
public class AverageStep implements ProcessStep {

    private static final Logger logger = LogManager.getLogger(AverageStep.class);
    private final JEVisObject functionalObject;

    public AverageStep(JEVisObject functionalObject) {
        this.functionalObject = functionalObject;
    }

    @Override
    public void run(ResourceManager resourceManager) throws Exception {
        logger.debug("Start Functional Step with id {}", functionalObject.getID());
        CleanDataObjectJEVis jevisAttribute = (CleanDataObjectJEVis) resourceManager.getCalcAttribute();
        JEVisObject cleanData = jevisAttribute.getObject();
        Aggregator aggr = new Aggregator();
        AggregationJob createAggregationJob = AggregationJob.createAggregationJob(functionalObject, cleanData, Aggregator.AggregationModus.average);
        List<JEVisSample> aggregatedData = aggr.getAggregatedData(createAggregationJob);
        SampleHandler sampleHandler = new SampleHandler();

        JEVisAttribute attribute = functionalObject.getAttribute("Value");
        sampleHandler.importDataAndReplaceSorted(aggregatedData, attribute);

    }

}
