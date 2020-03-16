package org.jevis.jeconfig.application.Chart.data;

import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.alarm.*;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.chart.BubbleType;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.SampleGenerator;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.*;

public class ChartDataModel {
    private static final Logger logger = LogManager.getLogger(ChartDataModel.class);
    private final JEVisDataSource dataSource;

    private String title;
    private DateTime selectedStart;
    private DateTime selectedEnd;
    private JEVisObject object;
    private JEVisAttribute attribute;
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
    private double timeFactor = 1.0;
    private Double scaleFactor = 1d;
    private double min = 0d;
    private double max = 0d;
    private double avg = 0d;
    private Double sum = 0d;
    private Map<DateTime, JEVisSample> userNoteMap;
    private Map<DateTime, Alarm> alarmMap;
    private boolean customWorkDay = true;
    private Image taskImage = JEConfig.getImage("Analysis.png");

    /**
     * Maximum number of parallel running getSamples(), not the Dashboard need multiple
     */
    private JEVisAttribute forecastDataAttribute;

    public ChartDataModel(JEVisDataSource dataSource) {
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

    public Map<DateTime, Alarm> getAlarms() {
        if (alarmMap != null) {
            return alarmMap;
        }

        Map<DateTime, Alarm> alarms = new TreeMap<>();

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
                                    operator = ">";
                                }
                                break;
                            case BIGGER_EQUALS:
                                if (value >= upperValue) {
                                    isAlarm = true;
                                    operator = "≥";
                                }
                                break;
                            case EQUALS:
                                if (value.equals(upperValue)) {
                                    isAlarm = true;
                                    operator = "=";
                                }
                                break;
                            case NOT_EQUALS:
                                if (!value.equals(upperValue)) {
                                    isAlarm = true;
                                    operator = "≠";
                                }
                                break;
                            case SMALLER:
                                if (value < lowerValue) {
                                    isAlarm = true;
                                    operator = "<";
                                }
                                upper = false;
                                break;
                            case SMALLER_EQUALS:
                                if (value <= lowerValue) {
                                    isAlarm = true;
                                    operator = "≤";
                                }
                                upper = false;
                                break;
                        }

                        if (isAlarm) {
                            int logVal = 0;

                            logVal = ScheduleService.getValueForLog(ts, usageSchedules);
                            JEVisSample alarmSample = new VirtualSample(ts, (long) logVal);

                            if (upper) {
                                alarms.put(ts, new Alarm(getDataProcessor(), getAttribute(), alarmSample, ts, value, operator, upperValue, sampleAlarmType, logVal));
                            } else {
                                alarms.put(ts, new Alarm(getDataProcessor(), getAttribute(), alarmSample, ts, value, operator, lowerValue, sampleAlarmType, logVal));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        alarmMap = alarms;
        return alarmMap;
    }

    public List<JEVisSample> getSamples() {
        if (this.somethingChanged) {
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
                            SampleGenerator sg = new SampleGenerator(
                                    attribute.getDataSource(),
                                    attribute.getObject(),
                                    attribute,
                                    selectedStart, selectedEnd,
                                    customWorkDay,
                                    manipulationMode,
                                    aggregationPeriod);

//                                    samples = sg.generateSamples();
                            samples = sg.getAggregatedSamples();

                            if (!isStringData) {
                                samples = factorizeSamples(samples);
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

            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        return samples;
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

                    SampleGenerator sg = new SampleGenerator(forecastDataAttribute.getDataSource(),
                            forecastDataAttribute.getObject(),
                            forecastDataAttribute,
                            selectedStart, selectedEnd,
                            customWorkDay,
                            manipulationMode, aggregationPeriod);

                    samples = sg.getAggregatedSamples();

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
            QuantityUnits qu = new QuantityUnits();
            scaleFactor = cu.scaleValue(inputUnit, outputUnit);
            timeFactor = 1.0;

            Double millisInput = null;
            Double millisOutput = null;
            try {
                if (scaleFactor != 1.0 && inputList.size() > 1 && aggregationPeriod != AggregationPeriod.NONE && !qu.isQuantityUnit(unit)) {
                    Period inputPeriod = attribute.getDisplaySampleRate();
                    if (inputPeriod.getYears() != 1 && inputPeriod.getMonths() != 3 && inputPeriod.getMonths() != 1) {
                        millisInput = (double) inputPeriod.toStandardDuration().getMillis();
                    } else if (inputPeriod.getMonths() == 1) {
                        /**
                         * TODO: change to on the fly duration of current month for exact values
                         */
                        millisInput = (double) Period.days(1).toStandardDuration().getMillis() * 30.4375;
                    } else if (inputPeriod.getMonths() == 3) {
                        millisInput = (double) Period.days(1).toStandardDuration().getMillis() * 30.4375 * 3;
                    } else if (inputPeriod.getYears() == 1) {
                        millisInput = (double) Period.days(1).toStandardDuration().getMillis() * 365.25;
                    }

                    Period outputPeriod = new Period(inputList.get(0).getTimestamp(), inputList.get(1).getTimestamp());

                    if (outputPeriod.getYears() != 1 && outputPeriod.getMonths() != 3 && outputPeriod.getMonths() != 1) {
                        millisOutput = (double) outputPeriod.toStandardDuration().getMillis();
                    } else if (outputPeriod.getMonths() == 1) {
                        /**
                         * TODO: change to on the fly duration of current month for exact values
                         */
                        millisOutput = (double) Period.days(1).toStandardDuration().getMillis() * 30.4375;
                    } else if (outputPeriod.getMonths() == 3) {
                        millisOutput = (double) Period.days(1).toStandardDuration().getMillis() * 30.4375 * 3;
                    } else if (outputPeriod.getYears() == 1) {
                        millisOutput = (double) Period.days(1).toStandardDuration().getMillis() * 365.25;
                    }

                    if (millisOutput != null && millisOutput > 0 && millisInput > 0) {
                        timeFactor = millisInput / millisOutput;
                    }
                }
            } catch (Exception e) {
                logger.error("Could not get calculate time scaling factor: ", e);
            }

            double finalTimeFactor1 = timeFactor;
            inputList.forEach(sample -> {
                try {
                    sample.setValue(sample.getValueAsDouble() * scaleFactor * finalTimeFactor1);
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

            return inputList;
        } else return inputList;
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
                if (jevisClassName.equals("Data") || jevisClassName.equals("Clean Data") || jevisClassName.equals("String Data")) {
                    if (dataProcessorObject == null) {
                        this.attribute = getObject().getAttribute("Value");
                    } else {
                        this.attribute = getDataProcessor().getAttribute("Value");
                    }

                    JEVisClass forecastData = this.dataSource.getJEVisClass("Forecast Data");
                    List<JEVisObject> children = this.attribute.getObject().getChildren(forecastData, false);
                    if (!children.isEmpty()) {
                        this.hasForecastData = true;
                        this.forecastDataAttribute = children.get(0).getAttribute("Value");
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
        return getAttribute() != null && getAttribute().hasSample();
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
    public ChartDataModel clone() {
        ChartDataModel newModel = new ChartDataModel(dataSource);
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
        newModel.setTimeFactor(this.getTimeFactor());
        newModel.setScaleFactor(this.getScaleFactor());
        newModel.setCustomWorkDay(this.isCustomWorkDay());
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

    public double getTimeFactor() {
        return timeFactor;
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

    public void setTimeFactor(double timeFactor) {
        this.timeFactor = timeFactor;
    }

    public void setScaleFactor(Double scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public boolean equals(ChartDataModel obj) {
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
}
