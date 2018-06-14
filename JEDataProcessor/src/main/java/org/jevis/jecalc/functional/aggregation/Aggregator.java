/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.jecalc.functional.aggregation;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.jevis.jecalc.functional.avg.AverageStep;
import org.joda.time.Period;
import org.slf4j.LoggerFactory;

/**
 *
 * @author broder
 */
public class Aggregator {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Aggregator.class);

    public List<JEVisSample> getAggregatedData(JEVisDataSource datasource, List<JEVisSample> cleanSamples, Period period, AggregationModus aggregationModus) {
        BasicProcess aggrigate = new BasicProcess();
        aggrigate.setJEVisDataSource(datasource);
        aggrigate.setID("Dynamic");
        aggrigate.setFunction(new AggregationFunktion(aggregationModus.name()));

        aggrigate.getOptions().add(new BasicProcessOption(ProcessOptions.PERIOD, period.toString()));
        BasicProcess input = new BasicProcess();
        input.setJEVisDataSource(datasource);
        input.setID("Dynamic Input");
        input.setFunction(new InputFunction(cleanSamples));
        aggrigate.setSubProcesses(Arrays.asList(input));
        List<JEVisSample> aggregatedSamples = aggrigate.getResult();
        return aggregatedSamples;
    }

    public List<JEVisSample> getAggregatedData(AggregationJob aggregationJob) {
        JEVisDataSource dataSource = null;
        try {
            dataSource = aggregationJob.getCleanData().getDataSource();
        } catch (JEVisException ex) {
            Logger.getLogger(Aggregator.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<JEVisSample> aggregatedData = getAggregatedData(dataSource, aggregationJob.getCleanSamples(), aggregationJob.getPeriod(), aggregationJob.getAggregationModus());
        logger.debug("{} result samples calculated", aggregatedData.size());
        return aggregatedData;
    }

    public enum AggregationModus {

        total, average;
    }

    public static void main(String[] args) {
        Aggregator agg = new Aggregator();
        JEVisDataSource ds = null;
        try {
            ds = new JEVisDataSourceWS("http://openjevis.org:18090");
            ds.connect("Sys Admin", "MyJEV34Env");
        } catch (JEVisException ex) {
            System.exit(1);
        }
        try {
            JEVisObject object = ds.getObject(5343l);
            List<JEVisSample> allSamples = object.getAttribute("Value").getAllSamples();
            List<JEVisSample> aggregatedData = agg.getAggregatedData(ds, allSamples, Period.months(1), AggregationModus.average);
            System.out.println(aggregatedData.size());
        } catch (JEVisException ex) {
            Logger.getLogger(Aggregator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
