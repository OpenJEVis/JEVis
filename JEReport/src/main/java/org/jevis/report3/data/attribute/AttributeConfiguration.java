/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.attribute;

import java.util.Map;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisObject;
import org.jevis.report3.data.attribute.AttributeConfigurationFactory.ReportConfigurationName;
import org.jevis.report3.data.attribute.AttributeConfigurationFactory.ReportConfigurationType;

/**
 *
 * @author broder
 */
public class AttributeConfiguration {

    private final ReportConfigurationType configType;
    private final ReportConfigurationName configName;
    private final Map<String, JEVisAttribute> attributeMap;

    public AttributeConfiguration(ReportConfigurationName configName, ReportConfigurationType configType, Map<String, JEVisAttribute> attributeMap) {
        this.configName = configName;
        this.configType = configType;
        this.attributeMap = attributeMap;
    }

    public ReportConfigurationType getConfigType() {
        return configType;
    }

    public ReportConfigurationName getConfigName() {
        return configName;
    }

    public JEVisAttribute getAttribute(String attrName) {
        return attributeMap.get(attrName);
    }

}
