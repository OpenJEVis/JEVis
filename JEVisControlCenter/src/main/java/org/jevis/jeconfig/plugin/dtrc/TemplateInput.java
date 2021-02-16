package org.jevis.jeconfig.plugin.dtrc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.joda.time.DateTime;

import java.util.List;

public class TemplateInput extends TemplateSelected {
    private static final Logger logger = LogManager.getLogger(TemplateInput.class);

    private String objectClass;
    private String attributeName;
    private String variableName;
    private String variableType;
    private String filter;
    private Boolean group;

    public String getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(String objectClass) {
        this.objectClass = objectClass;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableType() {
        return variableType;
    }

    public void setVariableType(String variableType) {
        this.variableType = variableType;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Boolean getGroup() {
        return group;
    }

    public void setGroup(Boolean group) {
        this.group = group;
    }

    public void buildVariableName(JEVisClass jeVisClass, JEVisType jeVisType) {
        try {
            setVariableName(CalculationNameFormatter.createVariableName(jeVisClass, jeVisType));
        } catch (JEVisException e) {
            logger.error("Could not create variable name", e);
        }
    }

    public String getValue(JEVisDataSource ds, DateTime start, DateTime end) throws JEVisException {
        if (getObjectID() == -1L) {
            throw new JEVisException("No selected object for class " + getObjectClass(), 4573895);
        }

        JEVisAttribute attribute = ds.getObject(getObjectID()).getAttribute(getAttributeName());

        if (getVariableType() == null
                || (getVariableType() != null
                && (getVariableType().equals(InputVariableType.AVG.toString()) || getVariableType().equals(InputVariableType.SUM.toString())))) {

            QuantityUnits quantityUnits = new QuantityUnits();
            boolean isQuantity = quantityUnits.isQuantityUnit(attribute.getInputUnit());
            isQuantity = quantityUnits.isQuantityIfCleanData(attribute, isQuantity);

            List<JEVisSample> samples = attribute.getSamples(start, end);
            double sum = 0d;
            for (JEVisSample jeVisSample : samples) {
                sum += jeVisSample.getValueAsDouble();
            }

            if (!isQuantity || getVariableType().equals(InputVariableType.AVG.toString())) sum = sum / samples.size();

            return String.valueOf(sum);
        } else if (getVariableType() != null
                && getVariableType().equals(InputVariableType.LAST.toString())) {
            List<JEVisSample> samples = attribute.getSamples(new DateTime(2001, 1, 1, 0, 0, 0, 0), start);

            return String.valueOf(samples.get(samples.size() - 1).getValueAsDouble());
        } else if (getVariableType() != null
                && getVariableType().equals(InputVariableType.STRING.toString())) {
            List<JEVisSample> samples = attribute.getSamples(new DateTime(2001, 1, 1, 0, 0, 0, 0), start);
            return samples.get(samples.size() - 1).getValueAsString();
        } else return String.valueOf(0d);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof TemplateInput) {
            TemplateInput otherObj = (TemplateInput) obj;

            return this.getVariableName().equals(otherObj.getVariableName());
        }

        return false;
    }

    public void clone(TemplateInput input) {
        setObjectID(input.getObjectID());
        setObjectClass(input.getObjectClass());
        setAttributeName(input.getAttributeName());
        setVariableType(input.getVariableType());
        setFilter(input.getFilter());
        setGroup(input.getGroup());
    }
}
