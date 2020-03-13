/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.attribute;

/**
 *
 * @author broder
 */
public interface ReportAttributeConfiguration {

    String NAME = "Report Configuration";

//    public interface ReportAttributeAggregationConfiguration extends ReportAttributeConfiguration {
//
//        public static final String NAME = "Report Aggregation Configuration";
//    }

    interface ReportAttributePeriodConfiguration extends ReportAttributeConfiguration {

        String NAME = "Report Period Configuration";
        String PERIOD = "Period";
        String AGGREGATION = "Aggregation";
        String MANIPULATION = "Manipulation";
        String FIXED_PERIOD = "Fixed Period";
    }

    interface ReportAttributeSpecificValueConfiguration extends ReportAttributeConfiguration {

        String NAME = "Report Specific Value Configuration";
    }
}
