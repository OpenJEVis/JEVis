package org.jevis.commons.report;

import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;

import java.util.ArrayList;
import java.util.List;

public class ReportAggregation {

    public static List<String> values() {
        List<String> values = new ArrayList<>();

        for (AggregationPeriod ap : AggregationPeriod.values()) {
            for (ManipulationMode mm : ManipulationMode.values()) {
                values.add(ap.toString() + "_" + mm.toString());
            }
        }

        return values;
    }
}
