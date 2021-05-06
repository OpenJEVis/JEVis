package org.jevis.jeconfig.plugin.dtrc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.UUID;

public class TemplateInput extends TemplateSelected {
    private static final Logger logger = LogManager.getLogger(TemplateInput.class);

    private String id;
    private String objectClass;
    private String attributeName;
    private String variableName;
    private String variableType;
    private String templateFormula;
    private String filter;
    private Boolean group;

    public TemplateInput() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getTemplateFormula() {
        return templateFormula;
    }

    public void setTemplateFormula(String templateFormula) {
        this.templateFormula = templateFormula;
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

        if (!getAttributeName().equals("name")) {
            JEVisAttribute attribute = ds.getObject(getObjectID()).getAttribute(getAttributeName());
            String returnValue = "-";

            if (getVariableType() == null
                    || (getVariableType() != null
                    && (getVariableType().equals(InputVariableType.AVG.toString()) || getVariableType().equals(InputVariableType.SUM.toString())
                    || getVariableType().equals(InputVariableType.MIN.toString()) || getVariableType().equals(InputVariableType.MAX.toString())))) {

                QuantityUnits quantityUnits = new QuantityUnits();
                boolean isQuantity = quantityUnits.isQuantityUnit(attribute.getInputUnit());
                isQuantity = quantityUnits.isQuantityIfCleanData(attribute, isQuantity);

                List<JEVisSample> samples = attribute.getSamples(start, end);
                double sum = 0d;
                double min = Double.MAX_VALUE;
                double max = -Double.MAX_VALUE;
                for (JEVisSample jeVisSample : samples) {
                    Double d = jeVisSample.getValueAsDouble();
                    sum += d;
                    min = Math.min(min, d);
                    max = Math.max(max, d);
                }

                if (!isQuantity || getVariableType().equals(InputVariableType.AVG.toString()))
                    sum = sum / samples.size();

                if (getVariableType().equals(InputVariableType.AVG.toString()) || getVariableType().equals(InputVariableType.SUM.toString())) {
                    returnValue = String.valueOf(sum);
                } else if (getVariableType().equals(InputVariableType.MIN.toString())) {
                    returnValue = String.valueOf(min);
                } else if (getVariableType().equals(InputVariableType.MAX.toString())) {
                    returnValue = String.valueOf(max);
                }
            } else if (getVariableType() != null
                    && getVariableType().equals(InputVariableType.NON_PERIODIC.toString())) {
                List<JEVisSample> samples = attribute.getSamples(new DateTime(2001, 1, 1, 0, 0, 0), start);
                returnValue = String.valueOf(samples.get(samples.size() - 1).getValueAsDouble());
            } else if (getVariableType() != null
                    && getVariableType().equals(InputVariableType.LAST.toString())) {
                JEVisSample sample = attribute.getLatestSample();
                returnValue = String.valueOf(sample.getValueAsDouble());
            } else if (getVariableType() != null
                    && getVariableType().equals(InputVariableType.YEARLY_VALUE.toString())) {
                JEVisSample sample = attribute.getLatestSample();
                LocalDate ld = new LocalDate(start.getYear(), 1, 1);
                int daysOfYear = Days.daysBetween(ld, ld.plusYears(1)).getDays();
                int daysOfInterval = Days.daysBetween(start.toLocalDate(), end.toLocalDate()).getDays();
                returnValue = String.valueOf(sample.getValueAsDouble() / daysOfYear * daysOfInterval);
            } else if (getVariableType() != null
                    && getVariableType().equals(InputVariableType.STRING.toString())) {
                JEVisSample latestSample = attribute.getLatestSample();
                returnValue = latestSample.getValueAsString();
            }

            if (returnValue.equals("NaN")) returnValue = "-";

            return returnValue;
        } else {
            return ds.getObject(getObjectID()).getName();
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof TemplateInput) {
            TemplateInput otherObj = (TemplateInput) obj;
            return this.getId().equals(otherObj.getId());
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
