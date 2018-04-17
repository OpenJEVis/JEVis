/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.process;

import java.util.Map;
import org.jevis.report3.data.attribute.ReportAttributeProperty;
import org.jevis.report3.data.report.ReportProperty;
import org.jevis.report3.data.reportlink.ReportLinkProperty;
import org.joda.time.Interval;

/**
 *
 * @author broder
 */
public interface SampleGenerator {

//    public boolean isValid(ReportProperty reportData, ReportLinkProperty linkData, ReportAttributeProperty attributeData);
    public Map<String, Object> work(ReportLinkProperty linkData, ReportAttributeProperty attributeData, ReportProperty property);
}
