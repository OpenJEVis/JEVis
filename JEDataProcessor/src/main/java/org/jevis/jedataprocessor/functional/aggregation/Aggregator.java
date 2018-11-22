/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jedataprocessor.functional.aggregation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisDataSource;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.dataprocessing.BasicProcess;
import org.jevis.commons.dataprocessing.BasicProcessOption;
import org.jevis.commons.dataprocessing.ProcessOptions;
import org.jevis.commons.dataprocessing.function.AggregationFunktion;
import org.jevis.commons.dataprocessing.function.InputFunction;
import org.jevis.jeapi.ws.JEVisDataSourceWS;
import org.joda.time.Period;

import java.util.Arrays;
import java.util.List;

/**
 * @author broder
 */
public class Aggregator {

    private static final Logger logger = LogManager.getLogger(Aggregator.class);

    public static void main(String[] args) {
        Aggregator agg = new Aggregator();
        JEVisDataSource ds = null;
        try {
            ds = new JEVisDataSourceWS("http://openjevis.org:18090");
            ds.connect("Sys Admin", "XXXXXX");
        } catch (JEVisException ex) {
            System.exit(1);
        }
        try {
            JEVisObject object = ds.getObject(5343l);
            List<JEVisSample> allSamples = object.getAttribute("Value").getAllSamples();
            List<JEVisSample> aggregatedData = agg.getAggregatedData(ds, allSamples, Period.months(1), AggregationModus.average);
            logger.info(aggregatedData.size());
        } catch (JEVisException ex) {
            logger.error(ex);
        }
    }

    public List<JEVisSample> getAggregatedData(JEVisDataSource datasource, List<JEVisSample> cleanSamples, Period period, AggregationModus aggregationModus) {
        BasicProcess aggregate = new BasicProcess();
        aggregate.setJEVisDataSource(datasource);
        aggregate.setID("Dynamic");
        aggregate.setFunction(new AggregationFunktion(aggregationModus.name()));

        aggregate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, period.toString()));
        BasicProcess input = new BasicProcess();
        input.setJEVisDataSource(datasource);
        input.setID("Dynamic Input");
        input.setFunction(new InputFunction(cleanSamples));
        aggregate.setSubProcesses(Arrays.asList(input));
        List<JEVisSample> aggregatedSamples = aggregate.getResult();
        return aggregatedSamples;
    }

    public List<JEVisSample> getAggregatedData(AggregationJob aggregationJob) {
        JEVisDataSource dataSource = null;
        try {
            dataSource = aggregationJob.getCleanData().getDataSource();
        } catch (JEVisException ex) {
            logger.error(ex);
        }
        List<JEVisSample> aggregatedData = getAggregatedData(dataSource, aggregationJob.getCleanSamples(), aggregationJob.getPeriod(), aggregationJob.getAggregationModus());
        logger.debug("{} result samples calculated", aggregatedData.size());
        return aggregatedData;
    }

    public enum AggregationModus {

        total, average
    }
}
