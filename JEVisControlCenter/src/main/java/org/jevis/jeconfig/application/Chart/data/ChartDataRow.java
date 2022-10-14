package org.jevis.jeconfig.application.Chart.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.alarm.*;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.constants.AlarmConstants;
import org.jevis.commons.constants.GapFillingType;
import org.jevis.commons.constants.NoteConstants;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.datetime.PeriodComparator;
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.jeconfig.application.Chart.ChartTools;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;

import java.util.*;

public class ChartDataRow extends ChartData {
    private static final Logger logger = LogManager.getLogger(ChartDataRow.class);
    private final JEVisDataSource dataSource;
    private DateTime selectedStart;
    private DateTime selectedEnd;
    private JEVisObject object;
    private JEVisAttribute attribute;
    private AggregationPeriod aggregationPeriod = AggregationPeriod.NONE;
    private ManipulationMode manipulationMode = ManipulationMode.NONE;
    private JEVisObject dataProcessorObject = null;
    private List<JEVisSample> samples = new ArrayList<>();
    private List<JEVisSample> forecastSamples = new ArrayList<>();
    private boolean somethingChanged = true;
    private List<Integer> selectedCharts = new ArrayList<>();
    private Double minValue;
    private Double maxValue;
    private Boolean isEnPI = false;
    private JEVisObject calculationObject;
    private Boolean absolute = false;
    private boolean isStringData = false;
    private boolean hasForecastData = false;
    private Double scaleFactor = 1d;
    private Double timeFactor = 1d;
    private Double min = 0d;
    private Double max = 0d;
    private Double avg = 0d;
    private Double sum = 0d;
    private Map<DateTime, JEVisSample> userNoteMap = new TreeMap<>();
    private Map<DateTime, JEVisSample> userDataMap = new TreeMap<>();
    private Map<DateTime, Alarm> alarmMap = new TreeMap<>();
    private boolean customWorkDay = true;
    private String customCSS;
    private JEVisAttribute forecastDataAttribute;
    private String formatString = "yyyy-MM-dd HH:mm:ss";

    public ChartDataRow(JEVisDataSource dataSource) {
        this(dataSource, null);
    }

    public ChartDataRow(JEVisDataSource ds, ChartData chartData) {
        this.dataSource = ds;

        if (chartData != null) {
            setId(chartData.getId());
            try {
                setObjectName(ds.getObject(chartData.getId()));
            } catch (Exception ignored) {
            }
            setAttributeString(chartData.getAttributeString());
            setUnit(chartData.getUnit());
            setName(chartData.getName());
            setColor(chartData.getColor());
            setAxis(chartData.getAxis());
            setCalculation(chartData.isCalculation());
            if (isCalculation() && getCalculationId() == -1) {
                setCalculationId(ChartTools.isObjectCalculated(getObject()));
            } else {
                setCalculationId(chartData.getCalculationId());
            }
            setBubbleType(chartData.getBubbleType());
            setChartType(chartData.getChartType());
            setIntervalStart(chartData.getIntervalStart());
            setIntervalEnd(chartData.getIntervalEnd());
            setIntervalEnabled(chartData.isIntervalEnabled());
            setCss(chartData.getCss());

            setSelectedStart(chartData.getIntervalStartDateTime());
            setSelectedEnd(chartData.getIntervalEndDateTime());

        }
    }

    public String getUnitLabel() {
        JEVisUnit jUnit = getUnit();

        String unit = UnitManager.getInstance().format(jUnit);
        if (jUnit != null && jUnit.getLabel() != null && !jUnit.getLabel().isEmpty()) {
            unit = UnitManager.getInstance().format(jUnit.getLabel());

        }

        return unit;
    }

    public Map<DateTime, JEVisSample> getNoteSamples() {
        if (!somethingChanged) {
            return userNoteMap;
        }

        userNoteMap.clear();
        try {
            final JEVisClass noteclass = getObject().getDataSource().getJEVisClass("Data Notes");
            for (JEVisObject obj : attribute.getObject().getParents().get(0).getChildren(noteclass, true)) {
                if (obj.getName().contains(attribute.getObject().getName())) {
                    JEVisAttribute userNotesAttribute = obj.getAttribute("User Notes");
                    if (userNotesAttribute.hasSample()) {
                        for (JEVisSample jeVisSample : userNotesAttribute.getSamples(selectedStart, selectedEnd)) {
                            userNoteMap.put(jeVisSample.getTimestamp(), jeVisSample);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error while creating note map for object {}:{}", getDataProcessor().getName(), getDataProcessor().getID(), e);
        }
        return userNoteMap;
    }

    public Map<DateTime, JEVisSample> getUserDataMap() {
        if (!somethingChanged) {
            return userDataMap;
        }

        userDataMap.clear();
        try {
            final JEVisClass userDataClass = attribute.getDataSource().getJEVisClass("User Data");
            for (JEVisObject obj : attribute.getObject().getParents().get(0).getChildren(userDataClass, true)) {
                if (obj.getName().contains(attribute.getObject().getName())) {
                    JEVisAttribute userDataValueAttribute = obj.getAttribute("Value");
                    if (userDataValueAttribute.hasSample()) {
                        for (JEVisSample smp : userDataValueAttribute.getSamples(selectedStart, selectedEnd)) {
                            userDataMap.put(smp.getTimestamp(), smp);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error while creating user data map for object {}:{}", getDataProcessor().getName(), getDataProcessor().getID(), e);
        }

        return userDataMap;
    }

    public void setUserDataMap(Map<DateTime, JEVisSample> map) {
        userDataMap = map;
    }

    public void setUserNoteMap(Map<DateTime, JEVisSample> userNoteMap) {
        this.userNoteMap = userNoteMap;
    }

    public void setAlarmMap(Map<DateTime, Alarm> alarmMap) {
        this.alarmMap = alarmMap;
    }

    public Map<DateTime, Alarm> getAlarms(boolean force) {
        if (!somethingChanged && !force) {
            return alarmMap;
        }

        alarmMap.clear();
        if (getDataProcessor() != null) {
            try {
                CleanDataAlarm cleanDataAlarm = new CleanDataAlarm(getDataProcessor());
                if (cleanDataAlarm.isEnabled() && cleanDataAlarm.isValidAlarmConfiguration()) {
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


                    if (!samples.isEmpty()) {
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

                                    if (logVal != 4) {
                                        JEVisSample alarmSample = new VirtualSample(ts, (long) logVal);

                                        if (upper) {
                                            alarmMap.put(ts, new Alarm(getDataProcessor(), getAttribute(), alarmSample, ts, value, operator, upperValue, sampleAlarmType, logVal));
                                        } else {
                                            alarmMap.put(ts, new Alarm(getDataProcessor(), getAttribute(), alarmSample, ts, value, operator, lowerValue, sampleAlarmType, logVal));
                                        }
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
        }

        return alarmMap;
    }

    public List<JEVisSample> getSamples() {
        if (this.somethingChanged || this.samples.isEmpty()) {
            try {
                List<JEVisSample> samples = new ArrayList<>();
                getAttribute();

                if (getSelectedStart() == null || getSelectedEnd() == null) {
                    this.samples = samples;
                    return this.samples;
                }

                getNoteSamples();
                getUserDataMap();

                somethingChanged = false;

                if (getSelectedStart().isBefore(getSelectedEnd()) || getSelectedStart().equals(getSelectedEnd())) {
                    try {
                        if (!isEnPI || (aggregationPeriod.equals(AggregationPeriod.NONE) && !absolute)) {
                            List<JEVisSample> unmodifiedSamples = attribute.getSamples(selectedStart, selectedEnd, customWorkDay, aggregationPeriod.toString(), manipulationMode.toString(), DateTimeZone.getDefault().getID());
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
                                logger.debug("Getting calc job not absolute for object {}:{} from {} to {} with period {}",
                                        calculationObject.getName(), calculationObject.getID(),
                                        selectedStart.toString("yyyy-MM-dd HH:mm:ss"), selectedEnd.toString("yyyy-MM-dd HH:mm:ss"),
                                        aggregationPeriod.toString());
                                calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), dataSource, calculationObject,
                                        selectedStart, selectedEnd, aggregationPeriod);
                            } else {
                                logger.debug("Getting calc job absolute for object {}:{} from {} to {} with period {}",
                                        calculationObject.getName(), calculationObject.getID(),
                                        selectedStart.toString("yyyy-MM-dd HH:mm:ss"), selectedEnd.toString("yyyy-MM-dd HH:mm:ss"),
                                        aggregationPeriod.toString());
                                calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), dataSource, calculationObject,
                                        selectedStart, selectedEnd, true);
                            }

                            samples = calcJob.getResults();

                            if (!getAbsolute() && dataProcessorObject != null) {
                                CleanDataObject cleanDataObject = new CleanDataObject(dataProcessorObject);
                                if (cleanDataObject.getLimitsEnabled()) {
                                    List<JsonLimitsConfig> limitsConfig = cleanDataObject.getLimitsConfig();
                                    Double maxDouble = null;
                                    Double minDouble = null;
                                    for (JsonLimitsConfig jsonLimitsConfig : limitsConfig) {
                                        if (limitsConfig.indexOf(jsonLimitsConfig) == 1) {
                                            String maxString = jsonLimitsConfig.getMax();
                                            try {
                                                maxDouble = Double.parseDouble(maxString);
                                            } catch (Exception e) {
                                                logger.error("Could not parse string {} to double", maxString, e);
                                            }

                                            String minString = jsonLimitsConfig.getMin();
                                            try {
                                                minDouble = Double.parseDouble(minString);
                                            } catch (Exception e) {
                                                logger.error("Could not parse string {} to double", maxString, e);
                                            }
                                        }
                                    }

                                    if (maxDouble != null || minDouble != null) {
                                        Double replacementValue = null;
                                        List<JsonGapFillingConfig> gapFillingConfig = cleanDataObject.getGapFillingConfig();

                                        for (JsonGapFillingConfig jsonGapFillingConfig : gapFillingConfig) {
                                            if (gapFillingConfig.indexOf(jsonGapFillingConfig) == 1 && jsonGapFillingConfig.getType().equals(GapFillingType.DEFAULT_VALUE.toString())) {
                                                String defaultValue = jsonGapFillingConfig.getDefaultvalue();
                                                try {
                                                    replacementValue = Double.parseDouble(defaultValue);
                                                } catch (Exception e) {
                                                    logger.error("Could not parse string {} to double", defaultValue, e);
                                                }
                                            }
                                        }

                                        if (replacementValue != null) {
                                            for (JEVisSample jeVisSample : samples) {
                                                try {
                                                    if ((maxDouble != null && jeVisSample.getValueAsDouble() > maxDouble) || (minDouble != null && jeVisSample.getValueAsDouble() < minDouble)) {
                                                        jeVisSample.setValue(replacementValue);
                                                        String note = "";
                                                        note += jeVisSample.getNote();
                                                        note += "," + NoteConstants.Limits.LIMIT_DEFAULT;
                                                        jeVisSample.setNote(note);
                                                    }
                                                } catch (Exception e) {
                                                    logger.error("Could not get replacement value for ts {} of sample for object {}:{}", jeVisSample.getTimestamp(), dataProcessorObject.getName(), dataProcessorObject.getID());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Exception ex) {
                        logger.error(ex);
                    }

                    try {
                        getNoteSamples();
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

                getAlarms(true);
                updateFormatString(samples);

            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        return samples;
    }

    private void applyUserData(List<JEVisSample> unmodifiedSamples) {
        try {
            //TODO make aggregation check for congruent data periods of clean data row and user data row
            if (!getUserDataMap().isEmpty()) {
                List<JEVisSample> samplesToRemove = new ArrayList<>();
                List<JEVisSample> samplesToAdd = new ArrayList<>();
                for (JEVisSample sample : unmodifiedSamples) {
                    if (getUserDataMap().containsKey(sample.getTimestamp())) {
                        samplesToRemove.add(sample);
                        JEVisSample userDataSample = getUserDataMap().get(sample.getTimestamp());
                        userDataSample.setNote(sample.getNote());
                        samplesToAdd.add(userDataSample);
                    }
                }

                if (!samplesToRemove.isEmpty()) {
                    unmodifiedSamples.removeAll(samplesToRemove);
                    unmodifiedSamples.addAll(samplesToAdd);
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
        } catch (Exception e) {
            logger.error("Could not apply user data correctly for object {}:{}", dataProcessorObject.getName(), dataProcessorObject.getID(), e);
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
                    samples = forecastDataAttribute.getSamples(selectedStart, selectedEnd, customWorkDay, aggregationPeriod.toString(), manipulationMode.toString(), DateTimeZone.getDefault().getID());

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

    public void setForecastSamples(List<JEVisSample> forecastSamples) {
        this.forecastSamples = forecastSamples;
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
        String outputUnit = UnitManager.getInstance().format(getUnit()).replace("·", "");
        if (outputUnit.equals("")) outputUnit = getUnit().getLabel();

        String inputUnit = UnitManager.getInstance().format(attribute.getDisplayUnit()).replace("·", "");
        if (inputUnit.equals("")) inputUnit = attribute.getDisplayUnit().getLabel();

        ChartUnits cu = new ChartUnits();
        scaleFactor = cu.scaleValue(inputUnit, outputUnit);
    }

    private List<JEVisSample> factorizeSamples(List<JEVisSample> inputList) {
        if (getUnit() != null) {
            try {
                String outputUnit = UnitManager.getInstance().format(getUnit()).replace("·", "");
                if (outputUnit.equals("")) outputUnit = getUnit().getLabel();

                String inputUnit = UnitManager.getInstance().format(attribute.getDisplayUnit()).replace("·", "");
                if (inputUnit.equals("")) inputUnit = attribute.getDisplayUnit().getLabel();

                ChartUnits cu = new ChartUnits();
                scaleFactor = cu.scaleValue(inputUnit, outputUnit);

                if ((inputUnit.equals("kWh") || inputUnit.equals("Wh") || inputUnit.equals("MWh") || inputUnit.equals("GWh"))
                        && (outputUnit.equals("kW") || outputUnit.equals("W") || outputUnit.equals("MW") || outputUnit.equals("GW"))) {
                    Period rowPeriod = CleanDataObject.getPeriodForDate(attribute.getObject(), selectedStart);
                    if (inputList.size() > 1) {
                        Period currentPeriod = new Period(inputList.get(0).getTimestamp(), inputList.get(1).getTimestamp());
                        PeriodComparator periodComparator = new PeriodComparator();
                        int compare = periodComparator.compare(currentPeriod, rowPeriod);

                        if (!currentPeriod.equals(rowPeriod) && compare > 0) {
                            if (currentPeriod.equals(Period.hours(1))) {
                                timeFactor *= 1 / 4d;
                            } else if (currentPeriod.equals(Period.days(1))) {
                                timeFactor *= 1 / 4d / 24;
                            } else if (currentPeriod.equals(Period.weeks(1))) {
                                timeFactor *= 1 / 4d / 24 / 7;
                            } else if (currentPeriod.equals(Period.months(1))) {
                                timeFactor *= 1 / 4d / 24 / 30.25;
                            } else if (currentPeriod.equals(Period.months(3))) {
                                timeFactor *= 1 / 4d / 24 / 30.25 / 3;
                            } else if (currentPeriod.equals(Period.years(1))) {
                                timeFactor *= 1 / 4d / 24 / 365.25;
                            }
                        }
                    } else {
                        logger.debug("Can not determine time factor for fewer than two samples");
                    }
                }

                inputList.forEach(sample -> {
                    try {
                        sample.setValue(sample.getValueAsDouble() * scaleFactor * timeFactor);
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

            } catch (Exception e) {
                logger.error("Could not factorize samples correctly for object {}:{}", dataProcessorObject.getName(), dataProcessorObject.getID(), e);
            }
        }
        return inputList;
    }

    public JEVisObject getDataProcessor() {
        if (dataProcessorObject == null && getId() != -1) {
            getObjects();
        }
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
        if (object == null && getId() != -1) {
            getObjects();
        }
        return object;
    }

    private void getObjects() {
        try {
            JEVisObject object = dataSource.getObject(getId());
            if (object.getJEVisClassName().equals("Clean Data")) {
                dataProcessorObject = object;
                this.object = CommonMethods.getFirstParentalDataObject(object);
            } else {
                this.object = object;
            }
        } catch (JEVisException e) {
            logger.error("No object selected", e);
        }
    }

//    public void setObject(JEVisObject _object) {
//        this.setId(_object.getID());
//        this.object = _object;
//    }

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

                " title='" + getName() + '\'' +
                ", selectedStart=" + selectedStart +
                ", selectedEnd=" + selectedEnd +
                ", object=" + object +
                ", attribute=" + attribute +
                ", color=" + getColor() +
                ", somethingChanged=" + somethingChanged +
                ", unit=" + getUnit() +
                ", selectedCharts=" + selectedCharts +
                '}';
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
        newModel.setDataProcessor(this.getDataProcessor());
        newModel.setAttribute(this.getAttribute());
        newModel.setSelectedEnd(this.getSelectedEnd());
        newModel.setSelectedStart(this.getSelectedStart());
        newModel.setEnPI(this.getEnPI());
        newModel.setCalculationObject(getCalculationObject());
        newModel.setAxis(this.getAxis());
        newModel.setColor(this.getColor());
        newModel.setName(this.getName());
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

    public boolean isStringData() {
        return isStringData;
    }

    public Double getScaleFactor() {
        return scaleFactor;
    }

    public Double getTimeFactor() {
        return timeFactor;
    }

    public void setTimeFactor(Double timeFactor) {
        this.timeFactor = timeFactor;
    }

    public Double getMin() {
        return min;
    }

    public void setMin(Double min) {
        this.min = min;
    }

    public Double getMax() {
        return max;
    }

    public void setMax(Double max) {
        this.max = max;
    }

    public Double getAvg() {
        return avg;
    }

    public void setAvg(Double avg) {
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
