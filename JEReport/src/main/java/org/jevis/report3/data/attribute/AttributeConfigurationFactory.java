/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.attribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;

import java.security.InvalidParameterException;
import java.util.*;

import static org.jevis.report3.data.attribute.AttributeConfigurationFactory.ReportConfigurationName.Period;
import static org.jevis.report3.data.attribute.AttributeConfigurationFactory.ReportConfigurationName.SpecificValue;
import static org.jevis.report3.data.attribute.AttributeConfigurationFactory.ReportConfigurationType.SampleGenerator;

/**
 * @author broder
 */
public class AttributeConfigurationFactory {
    private static final Logger logger = LogManager.getLogger(AttributeConfigurationFactory.class);

    /**
     * no duplicaed names possible
     *
     * @param attributeConfigObjects
     * @return
     */
    public List<AttributeConfiguration> getAttributeConfigurations(List<JEVisObject> attributeConfigObjects) {
        List<AttributeConfiguration> attributes = new ArrayList<>();
        for (JEVisObject attributeObject : attributeConfigObjects) {
            AttributeConfiguration attributeConfiguration = getAttributeConfiguration(attributeObject);
            attributes.add(attributeConfiguration);
        }
        if (checkForDuplicates(attributes)) {
            throw new RuntimeException("Duplicated Configurations under ReportAttribute Object");
        }
        return attributes;
    }

    private AttributeConfiguration getAttributeConfiguration(JEVisObject configObject) {
        AttributeConfiguration config = null;
        try {
            //get the name of the configuration (eg period, last...)
            String className = configObject.getJEVisClass().getName();
            ReportConfigurationName configName = ReportConfigurationName.getEnum(className);

            //look in the mapping for the config type of the config name
            ReportConfigurationType configType = getAttributeType(configName.toString());

            //set the whole configuration object 
            Map<String, JEVisAttribute> attributeMap = new HashMap<>();
            for (JEVisAttribute currentAttr : configObject.getAttributes()) {
                attributeMap.put(currentAttr.getName(), currentAttr);
            }
            config = new AttributeConfiguration(configName, configType, attributeMap);
        } catch (JEVisException ex) {
            logger.error("Error while parsing the attribute configuration with name: {} and id {}", configObject.getName(), configObject.getID(), ex);
            throw new InvalidParameterException("Error while parsing the attribute configuration with name:" + configObject.getName() + " and id " + configObject.getID());
        }
        return config;
    }

    ReportConfigurationType getAttributeType(String configName) {
        Map<ReportConfigurationName, ReportConfigurationType> configMapping = getConfigMapping();
        if (configMapping.containsKey(ReportConfigurationName.valueOf(configName))) {
            return configMapping.get(ReportConfigurationName.valueOf(configName));
        } else {
            throw new InvalidParameterException(configName + " is not a valid configuration type");
        }
    }

    //configuration of the valid types and names and the mapping between 
    //the mapping from configuration name to configuration type
    private Map<ReportConfigurationName, ReportConfigurationType> getConfigMapping() {
        Map<ReportConfigurationName, ReportConfigurationType> configMapping = new HashMap<>();
        configMapping.put(Period, SampleGenerator);
        configMapping.put(SpecificValue, SampleGenerator);
        return configMapping;
    }

    private boolean checkForDuplicates(List<AttributeConfiguration> attributes) {
        boolean duplicates = false;
        Set<ReportConfigurationName> configNames = new HashSet<>();
        for (AttributeConfiguration attrConfig : attributes) {
            ReportConfigurationName configName = attrConfig.getConfigName();
            if (configNames.contains(configName)) {
                duplicates = true;
                break;
            } else {
                configNames.add(configName);
            }
        }
        return duplicates;
    }

    //declare the enum types
    public enum ReportConfigurationType {

        SampleGenerator, SampleAdjuster
    }

    //declare the enum names
    public enum ReportConfigurationName {

        Period, SpecificValue;

        public static ReportConfigurationName getEnum(String value) {
            switch (value) {
                case ReportAttributeConfiguration.ReportAttributePeriodConfiguration.NAME:
                    return Period;
                case ReportAttributeConfiguration.ReportAttributeSpecificValueConfiguration.NAME:
                    return SpecificValue;
                default:
                    logger.error("{} is no valid report attribute configuration", value);
                    throw new InvalidParameterException(value + " is no valid report attribute configuration");
            }
        }
    }
}
