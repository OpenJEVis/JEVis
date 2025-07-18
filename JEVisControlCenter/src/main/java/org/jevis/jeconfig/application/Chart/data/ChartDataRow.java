package org.jevis.jeconfig.application.Chart.data;

import com.ibm.icu.text.NumberFormat;
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
import org.jevis.commons.datetime.PeriodHelper;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.json.JsonGapFillingConfig;
import org.jevis.commons.json.JsonLimitsConfig;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.commons.utils.CommonMethods;
import org.jevis.commons.ws.json.JsonSample;
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
    private Period period;
    private AggregationPeriod aggregationPeriod = AggregationPeriod.NONE;
    private ManipulationMode manipulationMode = ManipulationMode.NONE;
    private JEVisObject dataProcessorObject = null;
    private List<JEVisSample> samples = new ArrayList<>();
    private List<JEVisSample> forecastSamples = new ArrayList<>();
    private boolean somethingChanged = true;
    private List<Integer> selectedCharts = new ArrayList<>();
    private JEVisObject calculationObject;
    private Boolean absolute = false;
    private boolean isStringData = false;
    private boolean hasForecastData = false;
    private Double scaleFactor = 1d;
    private Double timeFactor = 1d;
    private ValueWithDateTime min = new ValueWithDateTime(0d, NumberFormat.getInstance(I18n.getInstance().getLocale()));
    private ValueWithDateTime max = new ValueWithDateTime(0d, NumberFormat.getInstance(I18n.getInstance().getLocale()));
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

        this.calculationProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                long id = ChartTools.isObjectCalculated(getObject());
                setCalculationId(id);
                if (id == -1) {
                    setCalculation(false);
                }
            } else {
                setCalculationId(-1);
            }
        });

        if (chartData != null) {
            setId(chartData.getId());
            try {
                setObjectName(ds.getObject(chartData.getId()));
            } catch (Exception ignored) {
            }
            setAttributeString(chartData.getAttributeString());
            setAggregationPeriod(chartData.getAggregationPeriod());
            setManipulationMode(chartData.getManipulationMode());
            setUnitPrefix(chartData.getUnitPrefix());
            setUnitFormula(chartData.getUnitFormula());
            setUnitLabel(chartData.getUnitLabel());
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
            setDecimalDigits(chartData.getDecimalDigits());
            setCss(chartData.getCss());

            setSelectedStart(chartData.getIntervalStartDateTime());
            setSelectedEnd(chartData.getIntervalEndDateTime());

        }
    }

    public Map<DateTime, JEVisSample> getNoteSamples() {
        if (!somethingChanged) {
            return userNoteMap;
        }

        userNoteMap.clear();
        try {
            final JEVisClass noteClass = getObject().getDataSource().getJEVisClass("Data Notes");
            for (JEVisObject obj : attribute.getObject().getParents().get(0).getChildren(noteClass, true)) {
                if (obj.getName().contains(attribute.getObject().getName())) {
                    JEVisAttribute userNotesAttribute = obj.getAttribute("Value");
                    if (userNotesAttribute.hasSample()) {
                        for (JEVisSample jeVisSample : userNotesAttribute.getSamples(selectedStart, selectedEnd)) {
                            userNoteMap.put(jeVisSample.getTimestamp(), jeVisSample);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            if (getDataProcessor() != null) {
                logger.error("Error while creating note map for clean data object {}:{}", getDataProcessor().getName(), getDataProcessor().getID(), e);
            } else {
                logger.error("Error while creating note map for object {}:{}", getObject().getName(), getObject().getID(), e);
            }
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
            if (getDataProcessor() != null) {
                logger.error("Error while creating user data map for clean data object {}:{}", getDataProcessor().getName(), getDataProcessor().getID(), e);
            } else {
                logger.error("Error while creating user data map for object {}:{}", getObject().getName(), getObject().getID(), e);
            }
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
                        for (JEVisSample valueSample : samples) {
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
                                            logger.warn("Could not find sample to compare with value for ts: {}", ts);
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
                                if (getDataProcessor() != null) {
                                    logger.error("Could not create alarm for sample {} of clean data object {}:{}", valueSample.getTimestamp(), getDataProcessor().getName(), getDataProcessor().getID(), e);
                                } else {
                                    logger.error("Could not create alarm for sample {} of object {}:{}", valueSample.getTimestamp(), getObject().getName(), getObject().getID(), e);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                if (getDataProcessor() != null) {
                    logger.error("Error while creating alarm map for clean data object {}:{}", getDataProcessor().getName(), getDataProcessor().getID(), e);
                } else {
                    logger.error("Error while creating alarm map for object {}:{}", getObject().getName(), getObject().getID(), e);
                }
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
                        if (!isCalculation() || (aggregationPeriod.equals(AggregationPeriod.NONE) && !absolute)) {
                            List<JEVisSample> unmodifiedSamples = attribute.getSamples(selectedStart, selectedEnd, customWorkDay, aggregationPeriod.toString(), manipulationMode.toString(), DateTimeZone.getDefault().getID());
                            if (!isStringData) {
                                applyUserData(unmodifiedSamples);
                                samples = factorizeSamples(unmodifiedSamples);

                                if (absolute && !samples.isEmpty()) {
                                    logger.debug("Getting manipulated data");
                                    Double manipulatedData = getManipulatedData(samples);
                                    JEVisSample virtualSample = new VirtualSample(samples.get(0).getTimestamp(), manipulatedData, getUnit());
                                    samples.clear();
                                    samples.add(virtualSample);
                                }
                            } else {
                                samples = unmodifiedSamples;
                            }

                        } else {
                            CalcJobFactory calcJobCreator = new CalcJobFactory();

                            CalcJob calcJob;

                            if (!getAbsolute()) {
                                if (getCalculationObject() == null) {
                                    if (ChartTools.getCalculationId(dataSource, getCalculationId()) == -1) {
                                        logger.warn("This is not a calculation, getting normal samples");
                                        setCalculation(false);
                                        samples = getSamples();
                                    }
                                }
                                logger.debug("Getting calc job not absolute for object {}:{} from {} to {} with period {}",
                                        getCalculationObject().getName(), getCalculationObject().getID(),
                                        selectedStart.toString("yyyy-MM-dd HH:mm:ss"), selectedEnd.toString("yyyy-MM-dd HH:mm:ss"),
                                        aggregationPeriod.toString());
                                calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), dataSource, getCalculationObject(),
                                        selectedStart, selectedEnd, aggregationPeriod);
                            } else {
                                logger.debug("Getting calc job absolute for object {}:{} from {} to {} with period {}",
                                        getCalculationObject().getName(), getCalculationObject().getID(),
                                        selectedStart.toString("yyyy-MM-dd HH:mm:ss"), selectedEnd.toString("yyyy-MM-dd HH:mm:ss"),
                                        aggregationPeriod.toString());
                                calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), dataSource, getCalculationObject(),
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
                                            GapFillingType fillingType = GapFillingType.parse(jsonGapFillingConfig.getType());
                                            if (gapFillingConfig.indexOf(jsonGapFillingConfig) == 1 && fillingType.equals(GapFillingType.DEFAULT_VALUE)) {
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
                                                    if (getDataProcessor() != null) {
                                                        logger.error("Could not get replacement value for ts {} of sample for clean data object {}:{}", jeVisSample.getTimestamp(), getDataProcessor().getName(), getDataProcessor().getID());
                                                    } else {
                                                        logger.error("Could not get replacement value for ts {} of sample for object {}:{}", jeVisSample.getTimestamp(), getObject().getName(), getObject().getID());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    } catch (Exception ex) {
                        logger.error("Error getting samples", ex);
                    }

                    try {
                        getNoteSamples();
                    } catch (Exception ex) {
                        logger.error("Error while getting note samples", ex);
                    }
                } else {
                    if (getDataProcessor() != null) {
                        logger.error("No interval between timestamps for clean data object {}:{}. The end instant must be greater the start. ",
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

    public void setSamples(List<JEVisSample> samples) {
        this.samples = samples;
    }

    private Double getManipulatedData(List<JEVisSample> samples) {
        Double value = 0d;
        if (samples.size() == 1) {
            try {
                value = samples.get(0).getValueAsDouble();
            } catch (Exception e) {
                logger.error("Could not get value for data row {}:{}", getObject().getName(), getObject().getID(), e);
            }
        } else if (samples.size() > 1) {
            try {
                QuantityUnits qu = new QuantityUnits();
                boolean isQuantity = qu.isQuantityUnit(getUnit());
                isQuantity = qu.isQuantityIfCleanData(getAttribute(), isQuantity);

                Double min = Double.MAX_VALUE;
                Double max = Double.MIN_VALUE;
                List<Double> listMedian = new ArrayList<>();

                DateTime dateTime = null;

                List<JsonSample> listManipulation = new ArrayList<>();
                for (JEVisSample sample : samples) {
                    Double currentValue = sample.getValueAsDouble();
                    value += currentValue;
                    min = Math.min(min, currentValue);
                    max = Math.max(max, currentValue);
                    listMedian.add(currentValue);

                    if (dateTime == null) dateTime = new DateTime(sample.getTimestamp());
                }
                if (!isQuantity) {
                    value = value / samples.size();
                }

                switch (getManipulationMode()) {
                    case AVERAGE:
                        value = value / (double) samples.size();
                        break;
                    case MIN:
                        value = min;
                        break;
                    case MAX:
                        value = max;
                        break;
                    case MEDIAN:
                        if (listMedian.size() > 1)
                            listMedian.sort(Comparator.naturalOrder());
                        value = listMedian.get((listMedian.size() - 1) / 2);
                        break;
                }

            } catch (Exception ex) {
                logger.error("Error in quantity check: {}", ex, ex);
            }
        }

        return value;

    }

    private void applyUserData(List<JEVisSample> unmodifiedSamples) {
        try {
            if (!getUserDataMap().isEmpty() && aggregationPeriod == AggregationPeriod.NONE) {

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
            if (getDataProcessor() != null) {
                logger.error("Could not apply user data correctly for object {}:{}", getDataProcessor().getName(), getDataProcessor().getID(), e);
            } else {
                logger.error("Could not apply user data correctly for object {}:{}", getObject().getName(), getObject().getID(), e);
            }
        }
    }

    private void updateFormatString(List<JEVisSample> samples) {
        if (!samples.isEmpty()) {
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
                        factorizeSamples(samples);
                    }

                } catch (Exception ex) {
                    logger.error(ex);
                }
            } else {
                if (getDataProcessor() != null) {
                    logger.error("No interval between timestamps for object {}:{}. The end instant must be greater the start. ",
                            getDataProcessor().getName(), getDataProcessor().getID());
                } else {
                    logger.error("No interval between timestamps for clean data object {}:{}. The end instant must be greater the start. ",
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

    public void updateScaleFactor() throws JEVisException {
        String outputUnit = UnitManager.getInstance().format(getUnit()).replace("·", "");
        if (outputUnit.isEmpty()) outputUnit = getUnit().getLabel();

        String inputUnit = UnitManager.getInstance().format(attribute.getDisplayUnit()).replace("·", "");
        if (inputUnit.isEmpty()) inputUnit = attribute.getDisplayUnit().getLabel();

        ChartUnits cu = new ChartUnits();
        Period currentPeriod = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
        Period rawPeriod = CleanDataObject.getPeriodForDate(attribute.getObject(), selectedStart);

        scaleFactor = cu.scaleValue(rawPeriod, inputUnit, currentPeriod, outputUnit);
    }

    private List<JEVisSample> factorizeSamples(List<JEVisSample> inputList) {
        if (getUnit() != null && !inputList.isEmpty()) {
            try {
                String outputUnit = UnitManager.getInstance().format(getUnit()).replace("·", "");
                if (outputUnit.isEmpty()) outputUnit = getUnit().getLabel();

                String inputUnit = UnitManager.getInstance().format(attribute.getDisplayUnit()).replace("·", "");
                if (inputUnit.isEmpty()) inputUnit = attribute.getDisplayUnit().getLabel();

                ChartUnits cu = new ChartUnits();

                Period rawPeriod = CleanDataObject.getPeriodForDate(attribute.getObject(), selectedStart);
                Period currentPeriod;
                if (inputList.size() > 1) {
                    currentPeriod = new Period(inputList.get(0).getTimestamp(), inputList.get(1).getTimestamp());
                } else currentPeriod = rawPeriod;

                scaleFactor = cu.scaleValue(rawPeriod, inputUnit, currentPeriod, outputUnit);

                inputList.forEach(sample -> {
                    try {
                        sample.setValue(sample.getValueAsDouble() * scaleFactor * timeFactor);
                    } catch (Exception e) {
                        try {
                            if (getDataProcessor() != null) {
                                logger.error("Error in sample: {}:{} of attribute: {} of clean data object: {}:{}", sample.getTimestamp(), sample.getValue(), getAttribute().getName(), getDataProcessor().getName(), getDataProcessor().getID());
                            } else {
                                logger.error("Error in sample: {}:{} of attribute: {} of object: {}:{}", sample.getTimestamp(), sample.getValue(), getAttribute().getName(), getObject().getName(), getObject().getID());
                            }
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
            DateTime timeStampFromLastSample = getAttribute().getTimestampOfLastSample();
            if (timeStampFromLastSample != null) {
                timeStampFromLastSample = timeStampFromLastSample.minusDays(7);

                DateTime timeStampOfFirstSample = getAttribute().getTimestampOfFirstSample();
                if (timeStampOfFirstSample != null) {
                    if (timeStampOfFirstSample.isBefore(timeStampFromLastSample))
                        selectedStart = timeStampFromLastSample;
                } else selectedStart = null;

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
            DateTime timeStampFromLastSample = getAttribute().getTimestampOfLastSample();
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
                this.dataProcessorObject = null;
            }
        } catch (Exception e) {
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
                if (jevisClassName.equals("Data") || jevisClassName.equals("Clean Data") || jevisClassName.equals("String Data")
                        || jevisClassName.equals("Base Data") || jevisClassName.equals("Math Data") || jevisClassName.equals("Data Notes")
                        || jevisClassName.equals("Forecast Data")) {
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

                    if (jevisClassName.equals("String Data") || jevisClassName.equals("Data Notes")) {
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

    public List<Integer> getSelectedCharts() {
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

    public void calcMinAndMax() {
        min.setValue(Double.MAX_VALUE);
        max.setValue(-Double.MAX_VALUE);
        avg = 0d;
        sum = 0d;

        for (JEVisSample sample : samples) {
            try {
                DateTime ts = sample.getTimestamp();
                Double value = sample.getValueAsDouble();
                min.minCheck(ts, value);
                max.maxCheck(ts, value);
                sum += value;
            } catch (Exception e) {
                logger.error("Could not calculate min and max.");
            }
        }
        QuantityUnits qu = new QuantityUnits();
        if (!samples.isEmpty()) {
            avg = sum / samples.size();
        }

        if (!qu.isQuantityUnit(getUnit()) && qu.isSumCalculable(getUnit()) && getManipulationMode().equals(ManipulationMode.NONE)) {
            try {
                JEVisUnit sumUnit = qu.getSumUnit(getUnit());
                ChartUnits cu = new ChartUnits();

                Period currentPeriod = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
                Period rawPeriod = CleanDataObject.getPeriodForDate(attribute.getObject(), selectedStart);

                double newScaleFactor = cu.scaleValue(rawPeriod, getUnit().toString(), currentPeriod, sumUnit.toString());
                JEVisUnit inputUnit = getAttribute().getInputUnit();
                JEVisUnit sumUnitOfInputUnit = qu.getSumUnit(inputUnit);

                if (qu.isDiffPrefix(sumUnitOfInputUnit, sumUnit)) {
                    sum = sum * newScaleFactor / getTimeFactor();
                } else {
                    sum = sum / getScaleFactor() / getTimeFactor();
                }
            } catch (Exception e) {
                logger.error("Couldn't calculate sum");
            }
        }

    }

    public JEVisObject getCalculationObject() {
        if (calculationObject == null && getCalculationId() != -1) {
            try {
                calculationObject = dataSource.getObject(getCalculationId());
            } catch (Exception e) {
                logger.error("Could not get object for id {}", getCalculationId(), e);
            }
        }

        return calculationObject;
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
        newModel.setCalculation(this.isCalculation());
        newModel.setAxis(this.getAxis());
        newModel.setColor(this.getColor());
        newModel.setName(this.getName());
        newModel.setSamples(this.getSamples());
        newModel.setUnitPrefix(this.getUnitPrefix());
        newModel.setUnitLabel(this.getUnitLabel());
        newModel.setUnitFormula(this.getUnitFormula());
        newModel.setBubbleType(this.getBubbleType());
        newModel.setAbsolute(this.getAbsolute());
        newModel.setScaleFactor(this.getScaleFactor());
        newModel.setCustomWorkDay(this.isCustomWorkDay());
        newModel.setChartType(this.getChartType());
        newModel.setPeriod(this.getPeriod());
        newModel.setSomethingChanged(false);
        newModel.setCss(this.getCss());
        newModel.setDecimalDigits(this.getDecimalDigits());

        return newModel;
    }

    public Boolean getAbsolute() {
        return absolute;
    }

    public void setAbsolute(Boolean absolute) {
        this.absolute = absolute;
    }

    public JEVisAttribute getForecastDataAttribute() {
        return forecastDataAttribute;
    }

    public boolean isStringData() {
        return isStringData;
    }

    public Double getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(Double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public Double getTimeFactor() {
        return timeFactor;
    }

    public void setTimeFactor(Double timeFactor) {
        this.timeFactor = timeFactor;
    }

    public ValueWithDateTime getMin() {
        return min;
    }

    public void setMin(ValueWithDateTime min) {
        this.min = min;
    }

    public ValueWithDateTime getMax() {
        return max;
    }

    public void setMax(ValueWithDateTime max) {
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
        if (period == null) {
            Period p = Period.ZERO;
            this.getObjects();
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

            } catch (Exception e) {
                if (getDataProcessor() != null) {
                    logger.error("Error while getting Period from clean data object {}:{}", getDataProcessor().getName(), getDataProcessor().getID(), e);
                } else {
                    logger.error("Error while getting Period from object {}:{}", object.getName(), object.getID(), e);
                }
            }

            period = p;
        }

        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
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

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    @Override
    public String getName() {
        if (super.getName() == null || super.getName().isEmpty()) {
            return getObject().getName();
        } else {
            return super.getName();
        }
    }
}
