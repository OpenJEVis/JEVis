/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.context;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jevis.api.JEVisException;
import org.jevis.report3.data.attribute.AttributeConfiguration;
import org.jevis.report3.data.attribute.AttributeConfigurationFactory;
import org.jevis.report3.data.attribute.ReportAttributeConfiguration;
import org.jevis.report3.data.report.IntervalCalculator;
import org.jevis.report3.process.LastSampleGenerator;
import org.jevis.report3.process.PeriodSampleGenerator;
import org.jevis.report3.process.SampleGenerator;
import org.joda.time.Interval;

/**
 *
 * @author broder
 */
public class SampleFactory {

    public SampleFactory() {
    }

    public SampleGenerator getSampleGenerator(List<AttributeConfiguration> configurations, IntervalCalculator intervalCalc) {
        SampleGenerator sampleGenerator = null;
        boolean validSampleGenerator = false;
        //get the sampleGenerator
        for (AttributeConfiguration config : configurations) {
            if (config.getConfigType().equals(AttributeConfigurationFactory.ReportConfigurationType.SampleGenerator)) {
                if (validSampleGenerator) {
                    System.out.println(" 2 sample generators");
                } else {
                    validSampleGenerator = true;
                    sampleGenerator = getSampleGenerator(config, intervalCalc);
                }
            }
        }

        //wrap the sampleGenerator into the sampleAdjusters
        for (AttributeConfiguration config : configurations) {
            if (config.getConfigType().equals(AttributeConfigurationFactory.ReportConfigurationType.SampleAdjuster)) {
                sampleGenerator = getSampleAdjuster(config, sampleGenerator);
            }
        }

        return sampleGenerator;
    }

    private SampleGenerator getSampleGenerator(AttributeConfiguration config, IntervalCalculator intervalCalc) {
        SampleGenerator sampleGenerator = null;
        switch (config.getConfigName()) {
            case Period: {
                Interval interval = null;
                try {
                    String modusName = config.getAttribute(ReportAttributeConfiguration.ReportAttributePeriodConfiguration.PERIOD).getLatestSample().getValueAsString();
                    IntervalCalculator.PeriodModus modus = IntervalCalculator.PeriodModus.valueOf(modusName.toUpperCase());
                    interval = intervalCalc.getInterval(modus);
                } catch (JEVisException ex) {
                    Logger.getLogger(SampleFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
                sampleGenerator = new PeriodSampleGenerator(interval);
            }
            break;
            case SpecificValue:
                sampleGenerator = new LastSampleGenerator();
                break;
        }
        return sampleGenerator;
    }

    private SampleGenerator getSampleAdjuster(AttributeConfiguration config, SampleGenerator sampleGenerator) {
        SampleGenerator sampleAdjuster = null;
        switch (config.getConfigName()) {
//            case Aggregation:
//                sampleAdjuster = new AggregateSampleAdjuster(sampleGenerator);
//                break;
        }
        return sampleAdjuster;
    }
}
