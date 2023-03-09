package org.jevis.jeconfig.plugin.dtrc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.object.plugin.RangingValues;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jeconfig.application.tools.CalculationNameFormatter;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrame;
import org.jevis.jeconfig.plugin.dashboard.timeframe.TimeFrameFactory;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.util.*;

public class TemplateInput extends TemplateSelected {
    private static final Logger logger = LogManager.getLogger(TemplateInput.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private String id;
    private Boolean quantity = false;
    private String objectClass;
    private String attributeName;
    private String variableName;
    private String variableType;
    private String templateFormula;
    @JsonIgnore
    private final Map<DateTime, Double> resultMap = new HashMap<>();
    private String filter;
    private Boolean group;
    private String dependency;
    private double sum = 0d;
    private double min = Double.MAX_VALUE;
    private double max = -Double.MAX_VALUE;
    private Boolean timeRestrictionEnabled = false;
    private String fixedTimeFrame;
    private String reducingTimeFrame;

    public TemplateInput() {
        this.id = UUID.randomUUID().toString();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean isQuantity() {
        return quantity;
    }

    public void setQuantity(Boolean isQuantity) {
        quantity = isQuantity;
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

    public String getDependency() {
        return dependency;
    }

    public void setDependency(String dependency) {
        this.dependency = dependency;
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

    public Boolean getTimeRestrictionEnabled() {
        return timeRestrictionEnabled;
    }

    public void setTimeRestrictionEnabled(Boolean timeRestrictionEnabled) {
        this.timeRestrictionEnabled = timeRestrictionEnabled;
    }

    public String getFixedTimeFrame() {
        return fixedTimeFrame;
    }

    public void setFixedTimeFrame(String fixedTimeFrame) {
        this.fixedTimeFrame = fixedTimeFrame;
    }

    public String getReducingTimeFrame() {
        return reducingTimeFrame;
    }

    public void setReducingTimeFrame(String reducingTimeFrame) {
        this.reducingTimeFrame = reducingTimeFrame;
    }

    public Map<DateTime, Double> getResultMap() {
        return resultMap;
    }

    public void buildVariableName(JEVisClass jeVisClass, JEVisType jeVisType) {
        try {
            setVariableName(CalculationNameFormatter.createVariableName(jeVisClass, jeVisType));
        } catch (JEVisException e) {
            logger.error("Could not create variable name", e);
        }
    }

    public void CreateValues(JEVisDataSource ds, IntervalSelector intervalSelector, DateTime start, DateTime end) {
        try {
            resultMap.clear();

            if (getTimeRestrictionEnabled()) {
                TimeFrame fixedTimeFrame = null;
                TimeFrame reducingTimeFrame = null;

                for (TimeFrame timeFrame : intervalSelector.getTimeFactoryBox().getItems()) {
                    if (getFixedTimeFrame().equals(timeFrame.getID())) {
                        fixedTimeFrame = timeFrame;
                    } else if (getReducingTimeFrame().equals(timeFrame.getID())) {
                        reducingTimeFrame = timeFrame;
                    }
                }

                if (fixedTimeFrame != null && reducingTimeFrame != null && !fixedTimeFrame.equals(TimeFrameFactory.NONE)) {
                    start = fixedTimeFrame.getInterval(start).getStart();
                    end = end;

                    Period p = null;
                    DateTime previousEndDate = null;
                    if (!reducingTimeFrame.equals(TimeFrameFactory.NONE)) {
                        try {
                            p = new Period(reducingTimeFrame.getID());
                        } catch (Exception ignored) {
                        }

                        if (p != null) {
                            previousEndDate = PeriodHelper.minusPeriodToDate(end, p);
                        } else {
                            previousEndDate = end.minus(reducingTimeFrame.getInterval(start).toDuration());
                        }
                    }

                    if (previousEndDate != null && previousEndDate.isAfter(start)) {
                        end = previousEndDate;
                    }
                }
            }

            if (!getAttributeName().equals("name")) {
                JEVisAttribute attribute = ds.getObject(getObjectID()).getAttribute(getAttributeName());
                String returnValue = "-";

                if (getVariableType() == null
                        || (getVariableType() != null
                        && (getVariableType().equals(InputVariableType.AVG.toString()) || getVariableType().equals(InputVariableType.SUM.toString())
                        || getVariableType().equals(InputVariableType.MIN.toString()) || getVariableType().equals(InputVariableType.MAX.toString())))) {

                    List<JEVisSample> samples = attribute.getSamples(start, end);

                    sum = 0d;
                    min = Double.MAX_VALUE;
                    max = -Double.MAX_VALUE;

                    for (JEVisSample jeVisSample : samples) {
                        Double d = jeVisSample.getValueAsDouble();
                        sum += d;
                        min = Math.min(min, d);
                        max = Math.max(max, d);
                    }

                    if ((!isQuantity() || getVariableType().equals(InputVariableType.AVG.toString())) && !samples.isEmpty())
                        sum = sum / samples.size();

                    if (getVariableType().equals(InputVariableType.AVG.toString()) || getVariableType().equals(InputVariableType.SUM.toString())) {
                        resultMap.put(start, sum);
                    } else if (getVariableType().equals(InputVariableType.MIN.toString())) {
                        if (min == Double.MAX_VALUE) {
                            min = 0d;
                        }
                        resultMap.put(start, min);
                    } else if (getVariableType().equals(InputVariableType.MAX.toString())) {
                        if (max == -Double.MAX_VALUE) {
                            max = 0d;
                        }
                        resultMap.put(start, max);
                    }
                } else if (getVariableType() != null
                        && getVariableType().equals(InputVariableType.NON_PERIODIC.toString())) {
                    List<JEVisSample> samples = attribute.getSamples(new DateTime(1990, 1, 1, 0, 0, 0), end);
                    List<JEVisSample> filteredList = new ArrayList<>();

                    for (int i = samples.size() - 1; i > -1; i--) {
                        JEVisSample sample = samples.get(i);
                        filteredList.add(sample);
                        if (sample.getTimestamp().isBefore(start)) {
                            break;
                        }
                    }

                    for (JEVisSample sample : filteredList) {
                        resultMap.put(sample.getTimestamp(), sample.getValueAsDouble());
                    }

                } else if (getVariableType() != null
                        && getVariableType().equals(InputVariableType.LAST.toString())) {
                    JEVisSample sample = attribute.getLatestSample();
                    resultMap.put(start, sample.getValueAsDouble());
                } else if (getVariableType() != null
                        && getVariableType().equals(InputVariableType.YEARLY_VALUE.toString())) {
                    DateTime ydt = new DateTime(start.getYear(), 1, 1, 0, 0, 0, 0);
                    List<JEVisSample> samples = attribute.getSamples(ydt, ydt.plusYears(1).minusMillis(1));
                    JEVisSample sample = samples.get(samples.size() - 1);
                    LocalDate ld = new LocalDate(start.getYear(), 1, 1);
                    int daysOfYear = Days.daysBetween(ld, ld.plusYears(1)).getDays();
                    int daysOfInterval = Days.daysBetween(start.toLocalDate(), end.toLocalDate()).getDays();
                    double value = sample.getValueAsDouble() / daysOfYear * daysOfInterval;

                    resultMap.put(ydt, value);
                }
            }
        } catch (Exception e) {
            logger.error("Could not get template input value for {}", getVariableName(), e);
        }
    }

    public String getValue(JEVisDataSource ds, DateTime start, DateTime end) {
        return this.getValue(ds, start, end, null);
    }

    public String getValue(JEVisDataSource ds, DateTime start, DateTime end, Double determinationValue) {
        try {
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
                        if (min == Double.MAX_VALUE) {
                            min = 0d;
                        }
                        returnValue = String.valueOf(min);
                    } else if (getVariableType().equals(InputVariableType.MAX.toString())) {
                        if (max == -Double.MAX_VALUE) {
                            max = 0d;
                        }
                        returnValue = String.valueOf(max);
                    }
                } else if (getVariableType() != null
                        && getVariableType().equals(InputVariableType.NON_PERIODIC.toString())) {
                    List<JEVisSample> samples = attribute.getSamples(new DateTime(1990, 1, 1, 0, 0, 0), start);
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
                } else if (getVariableType() != null
                        && getVariableType().equals(InputVariableType.RANGING_VALUE.toString()) && determinationValue != null) {
                    JEVisSample latestSample = attribute.getLatestSample();
                    RangingValues rangingValues = new RangingValues();
                    try {
                        JsonNode jsonNode = this.mapper.readTree(latestSample.getValueAsString());
                        this.mapper.readerForUpdating(rangingValues).treeToValue(jsonNode, RangingValues.class);
                    } catch (Exception e) {
                        logger.error("Could not parse json model", e);
                    }

                    Double value = rangingValues.getValue(determinationValue);

                    if (value != null) {
                        returnValue = String.valueOf(value);
                    }
                }

                if (returnValue.equals("NaN") || returnValue.equals("")) returnValue = "0";

                return returnValue;
            } else {
                return ds.getObject(getObjectID()).getName();
            }
        } catch (Exception e) {
            logger.error("Could not get template input value for {}", getVariableName(), e);
        }
        return "0";
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
