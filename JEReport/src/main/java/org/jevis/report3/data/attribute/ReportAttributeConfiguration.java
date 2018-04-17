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

    public static final String NAME = "Report Configuration";

//    public interface ReportAttributeAggregationConfiguration extends ReportAttributeConfiguration {
//
//        public static final String NAME = "Report Aggregation Configuration";
//    }

    public interface ReportAttributePeriodConfiguration extends ReportAttributeConfiguration {

        public static final String NAME = "Report Period Configuration";
        public static final String PERIOD = "Period";
        public static final String AGGREGATION = "Aggregation";
    }

    public interface ReportAttributeSpecificValueConfiguration extends ReportAttributeConfiguration {

        public static final String NAME = "Report Specific Value Configuration";
    }
}
