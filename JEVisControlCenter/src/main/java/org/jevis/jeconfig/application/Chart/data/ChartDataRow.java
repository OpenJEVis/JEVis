package org.jevis.jeconfig.application.Chart.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.alarm.*;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.constants.AlarmConstants;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.database.ObjectHandler;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartType;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.*;

public class ChartDataRow {
    private static final Logger logger = LogManager.getLogger(ChartDataRow.class);
    private final JEVisDataSource dataSource;

    private String title;
    private DateTime selectedStart;
    private DateTime selectedEnd;
    private JEVisObject object;
    private JEVisAttribute attribute;
    private ChartType chartType = ChartType.DEFAULT;
    private String color = "#1FBED6";
    private AggregationPeriod aggregationPeriod = AggregationPeriod.NONE;
    private ManipulationMode manipulationMode = ManipulationMode.NONE;
    private JEVisObject dataProcessorObject = null;
    private List<JEVisSample> samples = new ArrayList<>();
    private List<JEVisSample> forecastSamples = new ArrayList<>();
    private boolean somethingChanged = true;
    private JEVisUnit unit;
    private List<Integer> selectedCharts = new ArrayList<>();
    private Integer axis;
    private Double minValue;
    private Double maxValue;
    private Boolean isEnPI = false;
    private JEVisObject calculationObject;
    private Boolean absolute = false;
    private BubbleType bubbleType = BubbleType.NONE;
    private boolean isStringData = false;
    private boolean hasForecastData = false;
    private Double scaleFactor = 1d;
    private double min = 0d;
    private double max = 0d;
    private double avg = 0d;
    private Double sum = 0d;
    private Map<DateTime, JEVisSample> userNoteMap = new TreeMap<>();
    private Map<DateTime, JEVisSample> userUserDataMap = new TreeMap<>();
    private final Map<DateTime, Alarm> alarmMap = new TreeMap<>();
    private boolean customWorkDay = true;
    private String customCSS;

    /**
     * Maximum number of parallel running getSamples(), not the Dashboard need multiple
     */
    private JEVisAttribute forecastDataAttribute;
    private String formatString = "yyyy-MM-dd HH:mm:ss";

    public ChartDataRow(JEVisDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getUnitLabel() {
        JEVisUnit jUnit = getUnit();

        String unit = UnitManager.getInstance().format(jUnit);
        if (jUnit != null && jUnit.getLabel() != null && !jUnit.getLabel().isEmpty()) {
            unit = UnitManager.getInstance().format(jUnit.getLabel());

        }


        return unit;
    }

    public JEVisUnit getUnit() {
        try {
            if (unit == null) {
                if (getAttribute() != null) {
                    unit = getAttribute().getDisplayUnit();
                }
            }
        } catch (JEVisException ex) {
            logger.fatal(ex);
        }
        return unit;
    }

    public void setUnit(JEVisUnit _unit) {
        somethingChanged = true;
        this.unit = _unit;
    }

    public Map<DateTime, JEVisSample> getNoteSamples() {
        if (userNoteMap != null) {
            return userNoteMap;
        }

        Map<DateTime, JEVisSample> noteSample = new TreeMap<>();
        try {
            JEVisClass noteclass = getObject().getDataSource().getJEVisClass("Data Notes");
            for (JEVisObject jeVisObject : getObject().getChildren(noteclass, false)) {
                try {
                    jeVisObject.getAttribute("User Notes").getSamples(getSelectedStart(), getSelectedEnd()).forEach(jeVisSample -> {
                        try {
                            noteSample.put(jeVisSample.getTimestamp(), jeVisSample);
                        } catch (Exception ex) {

                        }
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        userNoteMap = noteSample;
        return noteSample;
    }

    public Map<DateTime, JEVisSample> getUserDataSamples() {
        if (userUserDataMap == null) {
            Map<DateTime, JEVisSample> userDataSamples = new TreeMap<>();
            try {
                final JEVisClass userDataClass = attribute.getDataSource().getJEVisClass("User Data");
                for (JEVisObject obj : attribute.getObject().getParents().get(0).getChildren(userDataClass, true)) {
                    if (obj.getName().contains(attribute.getObject().getName())) {
                        JEVisAttribute userDataValueAttribute = obj.getAttribute("Value");
                        if (userDataValueAttribute.hasSample()) {
                            for (JEVisSample smp : userDataValueAttribute.getAllSamples()) {
                                userDataSamples.put(smp.getTimestamp(), smp);
                            }
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            userUserDataMap = userDataSamples;
        }

        return userUserDataMap;
    }

    public Map<DateTime, Alarm> getAlarms() {
        if (!somethingChanged) {
            return alarmMap;
        }

        alarmMap.clear();
        try {
            CleanDataAlarm cleanDataAlarm = new CleanDataAlarm(getDataProcessor());
            if (cleanDataAlarm.isValidAlarmConfiguration()) {
                Double tolerance = cleanDataAlarm.getTolerance();
                AlarmType alarmType = cleanDataAlarm.getAlarmType();
                Double limit = null;
                List<UsageSchedule> usageSchedules = cleanDataAlarm.getUsageSchedules();
                List<JEVisSample> comparisonSamples;
                Map<DateTime, JEVisSample> compareMap = new HashMap<>();

                boolean dynamicAlarm = alarmType.equals(AlarmType.DYNAMIC);

                DateTime firstTimeStamp = getSelectedEnd();
                if (dynamicAlarm) {
                    comparisonSamples = cleanDataAlarm.getSamples(getSelectedStart(), getSelectedEnd());
                    for (JEVisSample sample : comparisonSamples) {
                        if (sample.getTimestamp().isBefore(firstTimeStamp)) firstTimeStamp = sample.getTimestamp();
                        compareMap.put(sample.getTimestamp(), sample);
                    }
                } else limit = cleanDataAlarm.getLimit();


                if (!getSamples().isEmpty()) {
                    for (JEVisSample valueSample : getSamples()) {
                        try {
                            DateTime ts = valueSample.getTimestamp();
                            JEVisSample compareSample = null;
                            Double value = valueSample.getValueAsDouble();
                            AlarmType sampleAlarmType;

                            Double diff = null;
                            Double lowerValue = null;
                            Double upperValue = null;
                            if (dynamicAlarm) {
                                compareSample = compareMap.get(ts);

                                if (compareSample == null) {

                                    DateTime dt = ts.minusSeconds(1);
                                    while (compareSample == null && (dt.equals(firstTimeStamp) || dt.isAfter(firstTimeStamp))) {
                                        compareSample = compareMap.get(dt);
                                        dt = dt.minusSeconds(1);
                                    }

                                    if (compareSample == null) {
                                        logger.error("Could not find sample to compare with value." + ts);
                                        continue;
                                    }
                                }

                                diff = compareSample.getValueAsDouble() * (tolerance / 100);
                                lowerValue = compareSample.getValueAsDouble() - diff;
                                upperValue = compareSample.getValueAsDouble() + diff;
                                sampleAlarmType = AlarmType.DYNAMIC;
                            } else {
                                diff = limit * (tolerance / 100);
                                lowerValue = limit - diff;
                                upperValue = limit + diff;
                                sampleAlarmType = AlarmType.STATIC;
                            }

                            boolean isAlarm = false;
                            boolean upper = true;
                            String operator = "";
                            switch (cleanDataAlarm.getOperator()) {
                                case BIGGER:
                                    if (value > upperValue) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.BIGGER);
                                    }
                                    break;
                                case BIGGER_EQUALS:
                                    if (value >= upperValue) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.BIGGER_EQUALS);
                                    }
                                    break;
                                case EQUALS:
                                    if (value.equals(upperValue)) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.EQUALS);
                                    }
                                    break;
                                case NOT_EQUALS:
                                    if (!value.equals(upperValue)) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.NOT_EQUALS);
                                    }
                                    break;
                                case SMALLER:
                                    if (value < lowerValue) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.SMALLER);
                                    }
                                    upper = false;
                                    break;
                                case SMALLER_EQUALS:
                                    if (value <= lowerValue) {
                                        isAlarm = true;
                                        operator = AlarmConstants.Operator.getValue(AlarmConstants.Operator.SMALLER_EQUALS);
                                    }
                                    upper = false;
                                    break;
                            }

                            if (isAlarm) {
                                int logVal = 0;

                                logVal = ScheduleService.getValueForLog(ts, usageSchedules);
                                JEVisSample alarmSample = new VirtualSample(ts, (long) logVal);

                                if (upper) {
                                    alarmMap.put(ts, new Alarm(getDataProcessor(), getAttribute(), alarmSample, ts, value, operator, upperValue, sampleAlarmType, logVal));
                                } else {
                                    alarmMap.put(ts, new Alarm(getDataProcessor(), getAttribute(), alarmSample, ts, value, operator, lowerValue, sampleAlarmType, logVal));
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Could not create alarm for sample {} of object {}:{}", valueSample.getTimestamp(), getDataProcessor().getName(), getDataProcessor().getID(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error while creating alarm map for object {}:{}", getDataProcessor().getName(), getDataProcessor().getID(), e);
        }

        return alarmMap;
    }

    public List<JEVisSample> getSamples() {
        if (this.somethingChanged || this.samples.isEmpty()) {
            try {
                List<JEVisSample> samples = new ArrayList<>();
                userNoteMap = null;//reset userNote list
                getAttribute();

                somethingChanged = false;

                if (getSelectedStart() == null || getSelectedEnd() == null) {
                    this.samples = samples;
                    return this.samples;
                }

                if (getSelectedStart().isBefore(getSelectedEnd()) || getSelectedStart().equals(getSelectedEnd())) {
                    try {
                        if (!isEnPI || (aggregationPeriod.equals(AggregationPeriod.NONE) && !absolute)) {
                            List<JEVisSample> unmodifiedSamples = attribute.getSamples(selectedStart, selectedEnd, customWorkDay, aggregationPeriod.toString(), manipulationMode.toString());
                            if (!isStringData) {
                                applyUserData(unmodifiedSamples);
                                samples = factorizeSamples(unmodifiedSamples);
                            } else {
                                samples = unmodifiedSamples;
                            }

                        } else {
                            CalcJobFactory calcJobCreator = new CalcJobFactory();

                            CalcJob calcJob;

                            if (!getAbsolute()) {
                                calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), dataSource, calculationObject,
                                        selectedStart, selectedEnd, aggregationPeriod);
                            } else {
                                calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), dataSource, calculationObject,
                                        selectedStart, selectedEnd, true);
                            }

                            samples = calcJob.getResults();

                            if (!getAbsolute() && dataProcessorObject != null) {
                                CleanDataObject cleanDataObject = new CleanDataObject(dataProcessorObject, new ObjectHandler(dataSource));
                                if (cleanDataObject.getLimitsEnabled()) {
                                    List<JsonLimitsConfig> limitsConfig = cleanDataObject.getLimitsConfig();
                                    Double maxDouble = null;
                                    for (JsonLimitsConfig jsonLimitsConfig : limitsConfig) {
                                        if (limitsConfig.indexOf(jsonLimitsConfig) == 1) {
                                            String maxString = jsonLimitsConfig.getMax();
                                            try {
                                                maxDouble = Double.parseDouble(maxString);
                                            } catch (Exception e) {
                                                logger.error("Could not parse string {} to double", maxString, e);
                                            }
                                        }
                                    }

                                    if (maxDouble != null) {
                                        Double replacementValue = null;
                                        List<JsonGapFillingConfig> gapFillingConfig = cleanDataObject.getGapFillingConfig();

                                        for (JsonGapFillingConfig jsonGapFillingConfig : gapFillingConfig) {
                                            if (gapFillingConfig.indexOf(jsonGapFillingConfig) == 1) {
                                                String defaultValue = jsonGapFillingConfig.getDefaultvalue();
                                                try {
                                                    replacementValue = Double.parseDouble(defaultValue);
                                                } catch (Exception e) {
                                                    logger.error("Could not parse string {} to double", defaultValue, e);
                                                }
                                            }
                                        }

                                        if (replacementValue != null) {
                                            Double finalMaxDouble = maxDouble;
                                            Double finalReplacementValue = replacementValue;
                                            samples.forEach(jeVisSample -> {
                                                try {
                                                    if (jeVisSample.getValueAsDouble() > finalMaxDouble) {
                                                        jeVisSample.setValue(finalReplacementValue);
                                                        String note = "";
                                                        note += jeVisSample.getNote();
                                                        note += "," + NoteConstants.Limits.LIMIT_DEFAULT;
                                                        jeVisSample.setNote(note);
                                                    }
                                                } catch (JEVisException e) {
                                                    e.printStackTrace();
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Exception ex) {
                        logger.error(ex);
                    }

                    try {
                        userNoteMap = getNoteSamples();
                    } catch (Exception ex) {
                        logger.error(ex);
                    }
                } else {
                    if (getDataProcessor() != null) {
                        logger.error("No interval between timestamps for object {}:{}. The end instant must be greater the start. ",
                                getDataProcessor().getName(), getDataProcessor().getID());
                    } else {
                        logger.error("No interval between timestamps for object {}:{}. The end instant must be greater the start. ",
                                getObject().getName(), getObject().getID());
                    }
                }

                this.samples = samples;
                updateFormatString(samples);

            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        return samples;
    }

    private void applyUserData(List<JEVisSample> unmodifiedSamples) throws JEVisException {
        if (!getUserDataSamples().isEmpty()) {
            List<JEVisSample> samplesToRemove = new ArrayList<>();
            for (JEVisSample sample : unmodifiedSamples) {
                if (getUserDataSamples().containsKey(sample.getTimestamp())) {
                    samplesToRemove.add(sample);
                }
            }

            unmodifiedSamples.removeAll(samplesToRemove);
            unmodifiedSamples.addAll(getUserDataSamples().values());
            unmodifiedSamples.sort(Comparator.comparing(sample -> {
                try {
                    return sample.getTimestamp();
                } catch (JEVisException e) {
                    logger.error("Could not compare sample {}", sample, e);
                }
                return null;
            }));
        }
    }

    private void updateFormatString(List<JEVisSample> samples) {
        if (samples.size() > 0) {
            try {
                boolean isCounter = false;
                if (samples.get(0).getAttribute() != null) {
                    isCounter = CleanDataObject.isCounter(samples.get(0).getAttribute().getObject(), samples.get(0));
                }

                if (samples.size() > 1) {
                    this.formatString = PeriodHelper.getFormatString(new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp()), isCounter);
                }
            } catch (Exception e) {
                logger.error("Could not get format string", e);
            }
        }
    }

    public Map<DateTime, JEVisSample> getSamplesMap() throws JEVisException {
        Map<DateTime, JEVisSample> sampleMap = new HashMap<>();
        for (JEVisSample sample : getSamples()) {
            sampleMap.put(sample.getTimestamp(), sample);
        }
        return sampleMap;
    }

    public List<JEVisSample> getForecastSamples() {

        try {
            List<JEVisSample> samples = new ArrayList<>();
            getAttribute();


            if (getSelectedStart() == null || getSelectedEnd() == null) {
                this.forecastSamples = samples;
                return this.forecastSamples;
            }
            if (getSelectedStart().isBefore(getSelectedEnd()) || getSelectedStart().equals(getSelectedEnd())) {
                try {
                    samples = forecastDataAttribute.getSamples(selectedStart, selectedEnd, customWorkDay, aggregationPeriod.toString(), manipulationMode.toString());

                    if (!isStringData) {
                        samples = factorizeSamples(samples);
                    }

                } catch (Exception ex) {
                    logger.error(ex);
                }
            } else {
                if (getDataProcessor() != null) {
                    logger.error("No interval between timestamps for object {}:{}. The end instant must be greater the start. ",
                            getDataProcessor().getName(), getDataProcessor().getID());
                } else {
                    logger.error("No interval between timestamps for object {}:{}. The end instant must be greater the start. ",
                            getObject().getName(), getObject().getID());
                }
            }

            forecastSamples = samples;
        } catch (Exception ex) {
            logger.error(ex);
        }

        return forecastSamples;
    }

    public void setSamples(List<JEVisSample> samples) {
        this.samples = samples;
    }

    /**
     * Workaround from FS, Gerrit find the right solution.
     * This workaround is for the chart Sum function because it never calls the geSample with change function
     * the scaleFactor is never set.
     * <p>
     * calling the factorizeSamples will add factor and will aso not work
     *
     * @throws JEVisException
     */
    public void updateScaleFactor() throws JEVisException {
        String outputUnit = UnitManager.getInstance().format(unit).replace("·", "");
        if (outputUnit.equals("")) outputUnit = unit.getLabel();

        String inputUnit = UnitManager.getInstance().format(attribute.getDisplayUnit()).replace("·", "");
        if (inputUnit.equals("")) inputUnit = attribute.getDisplayUnit().getLabel();

        ChartUnits cu = new ChartUnits();
        scaleFactor = cu.scaleValue(inputUnit, outputUnit);
    }

    private List<JEVisSample> factorizeSamples(List<JEVisSample> inputList) throws JEVisException {
        if (unit != null) {
            String outputUnit = UnitManager.getInstance().format(unit).replace("·", "");
            if (outputUnit.equals("")) outputUnit = unit.getLabel();

            String inputUnit = UnitManager.getInstance().format(attribute.getDisplayUnit()).replace("·", "");
            if (inputUnit.equals("")) inputUnit = attribute.getDisplayUnit().getLabel();

            ChartUnits cu = new ChartUnits();
            scaleFactor = cu.scaleValue(inputUnit, outputUnit);

            inputList.forEach(sample -> {
                try {
                    sample.setValue(sample.getValueAsDouble() * scaleFactor);
                } catch (Exception e) {
                    try {
                        logger.error("Error in sample: " + sample.getTimestamp() + " : " + sample.getValue()
                                + " of attribute: " + getAttribute().getName()
                                + " of object: " + getObject().getName() + ":" + getObject().getID());
                    } catch (Exception e1) {
                        logger.fatal(e1);
                    }
                }
            });

        }
        return inputList;
    }

    public JEVisObject getDataProcessor() {
        return dataProcessorObject;
    }

    public void setDataProcessor(JEVisObject _dataProcessor) {
        somethingChanged = true;
        this.dataProcessorObject = _dataProcessor;
    }

    public AggregationPeriod getAggregationPeriod() {
        return aggregationPeriod;
    }

    public void setAggregationPeriod(AggregationPeriod aggregationPeriod) {
        somethingChanged = true;
        this.aggregationPeriod = aggregationPeriod;
    }

    public ManipulationMode getManipulationMode() {
        return manipulationMode;
    }

    public void setManipulationMode(ManipulationMode manipulationMode) {
        somethingChanged = true;
        this.manipulationMode = manipulationMode;
    }

    public String getTitle() {
        if (title != null && !title.equals("")) {
            return title;
        } else {
            return getObject().getName();
        }
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public DateTime getSelectedStart() {

        if (selectedStart != null) {
            return selectedStart;
        } else if (getAttribute() != null) {
            DateTime timeStampFromLastSample = getAttribute().getTimestampFromLastSample();
            if (timeStampFromLastSample != null) {
                timeStampFromLastSample = timeStampFromLastSample.minusDays(7);

                DateTime timeStampFromFirstSample = getAttribute().getTimestampFromFirstSample();
                if (timeStampFromFirstSample != null) {
                    if (timeStampFromFirstSample.isBefore(timeStampFromLastSample))
                        selectedStart = timeStampFromLastSample;
                } else selectedStart = timeStampFromFirstSample;

            } else {
                return null;
            }

            return selectedStart;
        } else {
            return null;
        }
    }

    public void setSelectedStart(DateTime selectedStart) {
        if (selectedEnd == null || !selectedEnd.equals(selectedStart)) {
            somethingChanged = true;
        }
        this.selectedStart = selectedStart;
    }

    public DateTime getSelectedEnd() {
        if (selectedEnd != null) {
            return selectedEnd;
        } else if (getAttribute() != null) {
            DateTime timeStampFromLastSample = getAttribute().getTimestampFromLastSample();
            if (timeStampFromLastSample == null) selectedEnd = DateTime.now();
            else selectedEnd = timeStampFromLastSample;
            return selectedEnd;
        } else {
            return null;
        }
    }

    public void setSelectedEnd(DateTime selectedEnd) {
        if (this.selectedEnd == null || !this.selectedEnd.equals(selectedEnd)) {
            somethingChanged = true;
        }
        this.selectedEnd = selectedEnd;
    }

    public JEVisObject getObject() {
        return object;
    }

    public void setObject(JEVisObject _object) {
        this.object = _object;
    }

    public JEVisAttribute getAttribute() {
        if (attribute == null || somethingChanged) {
            try {

                String jevisClassName = getObject().getJEVisClassName();
                if (jevisClassName.equals("Data") || jevisClassName.equals("Clean Data") || jevisClassName.equals("String Data") || jevisClassName.equals("Base Data")) {
                    if (dataProcessorObject == null) {
                        this.attribute = getObject().getAttribute("Value");
                    } else {
                        this.attribute = getDataProcessor().getAttribute("Value");
                    }

                    if (this.attribute != null) {
                        JEVisClass forecastData = this.dataSource.getJEVisClass("Forecast Data");
                        List<JEVisObject> children = this.attribute.getObject().getChildren(forecastData, false);
                        if (!children.isEmpty()) {
                            this.hasForecastData = true;
                            this.forecastDataAttribute = children.get(0).getAttribute("Value");
                        }
                    }

                    if (jevisClassName.equals("String Data")) {
                        this.isStringData = true;
                    }
                }
            } catch (Exception ex) {
                logger.fatal(ex);
            }
        }

        return attribute;
    }

    public void setAttribute(JEVisAttribute _attribute) {
        this.attribute = _attribute;
    }

    public String getColor() {
        if (color.contains("0x")) {
            color = color.replace("0x", "#");
        }
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isSelectable() {
        return getAttribute() != null;
    }

    public boolean hasForecastData() {
        return hasForecastData;
    }

    public List<Integer> getSelectedcharts() {
        return selectedCharts;
    }

    public void setSelectedCharts(List<Integer> selectedCharts) {

        somethingChanged = true;
        this.selectedCharts = selectedCharts;
    }

    public void setSomethingChanged(boolean _somethingChanged) {
        this.somethingChanged = _somethingChanged;
    }

    @Override
    public String toString() {
        return "ChartDataModel{" +

                " title='" + title + '\'' +
                ", selectedStart=" + selectedStart +
                ", selectedEnd=" + selectedEnd +
                ", object=" + object +
                ", attribute=" + attribute +
                ", color=" + color +
                ", somethingChanged=" + somethingChanged +
                ", unit=" + unit +
                ", selectedCharts=" + selectedCharts +
                '}';
    }

    public Integer getAxis() {
        if (axis == null) return 0;
        return axis;
    }

    public void setAxis(Integer axis) {
        this.axis = axis;
    }

    public Double getMinValue() {
        return minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void calcMinAndMax() {
        minValue = Double.MAX_VALUE;
        maxValue = -Double.MAX_VALUE;

        samples.forEach(sample -> {
            try {
                minValue = Math.min(minValue, sample.getValueAsDouble());
                maxValue = Math.max(maxValue, sample.getValueAsDouble());
            } catch (JEVisException e) {
                logger.error("Could not calculate min and max.");
            }
        });
    }

    public Boolean getEnPI() {
        return isEnPI;
    }

    public void setEnPI(Boolean enPI) {
        isEnPI = enPI;
    }

    public JEVisObject getCalculationObject() {
        return calculationObject;
    }

    public void setCalculationObject(String calculationObject) {
        TargetHelper th = new TargetHelper(dataSource, calculationObject);
        if (th.getObject() != null && !th.getObject().isEmpty()) {
            this.calculationObject = th.getObject().get(0);
        }
    }

    public void setCalculationObject(JEVisObject calculationObject) {
        this.calculationObject = calculationObject;
    }

    @Override
    public ChartDataRow clone() {
        ChartDataRow newModel = new ChartDataRow(dataSource);
        newModel.setManipulationMode(this.getManipulationMode());
        newModel.setAggregationPeriod(this.getAggregationPeriod());
        newModel.setObject(this.getObject());
        newModel.setDataProcessor(this.getDataProcessor());
        newModel.setAttribute(this.getAttribute());
        newModel.setSelectedEnd(this.getSelectedEnd());
        newModel.setSelectedStart(this.getSelectedStart());
        newModel.setEnPI(this.getEnPI());
        newModel.setCalculationObject(getCalculationObject());
        newModel.setAxis(this.getAxis());
        newModel.setColor(this.getColor());
        newModel.setSelectedCharts(this.getSelectedcharts());
        newModel.setTitle(this.getTitle());
        newModel.setSamples(this.getSamples());
        newModel.setUnit(this.getUnit());
        newModel.setBubbleType(this.getBubbleType());
        newModel.setAbsolute(this.getAbsolute());
        newModel.setScaleFactor(this.getScaleFactor());
        newModel.setCustomWorkDay(this.isCustomWorkDay());
        newModel.setChartType(this.getChartType());
        newModel.setSomethingChanged(false);

        return newModel;
    }

    public Boolean getAbsolute() {
        return absolute;
    }

    public JEVisAttribute getForecastDataAttribute() {
        return forecastDataAttribute;
    }

    public void setAbsolute(Boolean absolute) {
        this.absolute = absolute;
    }

    public BubbleType getBubbleType() {
        return bubbleType;
    }

    public void setBubbleType(BubbleType bubbleType) {
        this.bubbleType = bubbleType;
    }

    public boolean isStringData() {
        return isStringData;
    }

    public Double getScaleFactor() {
        return scaleFactor;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public Double getSum() {
        return sum;
    }

    public void setSum(Double sum) {
        this.sum = sum;
    }

    public void setScaleFactor(Double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public boolean equals(ChartDataRow obj) {
        if (this.getDataProcessor() != null && obj.getDataProcessor() != null) {
            return this.getObject().getID().equals(obj.getObject().getID())
                    && this.getDataProcessor().getID().equals(obj.getDataProcessor().getID());
        } else if (this.getDataProcessor() == null && obj.getDataProcessor() == null) {
            return this.getObject().getID().equals(obj.getObject().getID());
        } else {
            return false;
        }
    }

    public boolean isCustomWorkDay() {
        return customWorkDay;
    }

    public void setCustomWorkDay(boolean customWorkDay) {
        this.customWorkDay = customWorkDay;
    }

    public ChartType getChartType() {
        return chartType;
    }

    public void setChartType(ChartType chartType) {
        this.chartType = chartType;
    }

    public Period getPeriod() {
        Period p = Period.ZERO;
        JEVisObject object = null;
        if (dataProcessorObject != null) {
            object = this.dataProcessorObject;
        } else {
            object = this.object;
        }

        try {
            JEVisAttribute periodAttribute = object.getAttribute("Period");
            if (periodAttribute != null) {
                JEVisSample latestSample = periodAttribute.getLatestSample();

                if (latestSample != null) {
                    p = new Period(latestSample.getValueAsString());
                }
            }

        } catch (JEVisException e) {
            e.printStackTrace();
        }

        return p;
    }

    public String getCustomCSS() {
        return customCSS;
    }

    public void setCustomCSS(String customCSS) {
        this.customCSS = customCSS;
    }

    public String getFormatString() {
        return formatString;
    }
}
