/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jevis.report3.data.attribute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.commons.database.JEVisObjectDataManager;
import org.jevis.commons.database.JEVisSampleDAO;
import org.jevis.report3.data.attribute.AttributeConfigurationFactory.ReportConfigurationName;

/**
 *
 * @author broder
 */
public class ReportAttributeProperty {

    private String attributeName;
    private List<AttributeConfiguration> attributeConfigurations;
    @Inject
    private AttributeConfigurationFactory attrFactory;
    @Inject
    private JEVisSampleDAO sampleDaoSut;
    @Inject
    private JEVisObjectDataManager objectDataManager;

    @Inject
    public ReportAttributeProperty(JEVisSampleDAO sampleDaoSut, JEVisObjectDataManager objectDataManager, AttributeConfigurationFactory attrFactory) {
        this.sampleDaoSut = sampleDaoSut;
        this.objectDataManager = objectDataManager;
        this.attrFactory = attrFactory;
    }

    public static ReportAttributeProperty buildDefault(String attributeName) {
        List<AttributeConfiguration> attributeConfigurations = new ArrayList<>();
        AttributeConfiguration attrConfig = new AttributeConfiguration(AttributeConfigurationFactory.ReportConfigurationName.SpecificValue, AttributeConfigurationFactory.ReportConfigurationType.SampleGenerator, new HashMap<>());
        attributeConfigurations.add(attrConfig);
        ReportAttributeProperty attributeProperty = new ReportAttributeProperty(attributeConfigurations, attributeName);
        return attributeProperty;
    }

    private ReportAttributeProperty(List<AttributeConfiguration> attributeConfigurations, String attributeName) {
        this.attributeConfigurations = attributeConfigurations;
        this.attributeName = attributeName;
    }

    public void initialize(JEVisObject reportAttributeObject) {
        initializeAttributes(reportAttributeObject);

        List<JEVisObject> configObjects = objectDataManager.getChildren(reportAttributeObject, ReportAttributeConfiguration.NAME);

        initializeConfigurations(configObjects);

    }

    public String getAttributeName() {
        return attributeName;
    }

    public List<AttributeConfiguration> getAttributeConfigurations() {
        return attributeConfigurations;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public void setAttributeConfigurations(List<AttributeConfiguration> attributeConfigurations) {
        this.attributeConfigurations = attributeConfigurations;
    }

    public AttributeConfiguration getAttributeConfiguration(ReportConfigurationName reportConfigurationName) {
        AttributeConfiguration attributeConfiguration = null;
        for (AttributeConfiguration curAttrConfig : attributeConfigurations) {
            if (curAttrConfig.getConfigName().equals(reportConfigurationName)) {
                attributeConfiguration = curAttrConfig;
                break;
            }
        }
        return attributeConfiguration;
    }

    public void initializeAttributes(JEVisObject dataObject) {
        JEVisSample sample = sampleDaoSut.getLastJEVisSample(dataObject, ReportAttribute.ATTRIBUTE_NAME);
        if (sample == null) {
            return;
        }
        try {
            attributeName = sample.getValueAsString();
        } catch (JEVisException ex) {
            Logger.getLogger(ReportAttributeProperty.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void initializeConfigurations(List<JEVisObject> configurationObjects) {
        attributeConfigurations = attrFactory.getAttributeConfigurations(configurationObjects);
    }

}
