package org.jevis.commons.chart;

import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.*;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.AggregationPeriod;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.dataprocessing.SampleGenerator;
import org.jevis.commons.dataprocessing.VirtualSample;
import org.jevis.commons.object.plugin.TargetHelper;
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.List;

public class ChartDataModel {
    private static final Logger logger = LogManager.getLogger(ChartDataModel.class);
    private final JEVisDataSource dataSource;

    private String title;
    private DateTime selectedStart;
    private DateTime selectedEnd;
    private JEVisObject object;
    private JEVisAttribute attribute;
    private Color color = Color.LIGHTBLUE;
    private AggregationPeriod aggregationPeriod = AggregationPeriod.NONE;
    private ManipulationMode manipulationMode = ManipulationMode.NONE;
    private JEVisObject dataProcessorObject = null;
    private List<JEVisSample> samples = new ArrayList<>();
    private boolean somethingChanged = true;
    private JEVisUnit unit;
    private List<Integer> selectedCharts = new ArrayList<>();
    private Integer axis;
    private Double minValue;
    private Double maxValue;
    private Boolean isEnPI = false;
    private JEVisObject calculationObject;

    public ChartDataModel(JEVisDataSource dataSource) {
        this.dataSource = dataSource;
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

    public List<JEVisSample> getSamples() {
        if (somethingChanged) {
            getAttribute();
            dataSource.reloadAttribute(attribute);

            somethingChanged = false;

            setSamples(new ArrayList<>());

            if (getSelectedStart().isBefore(getSelectedEnd())) {
                try {

                    if (!isEnPI || aggregationPeriod.equals(AggregationPeriod.NONE)) {
                        SampleGenerator sg;
                        if (aggregationPeriod.equals(AggregationPeriod.NONE))
                            sg = new SampleGenerator(attribute.getDataSource(), attribute.getObject(), attribute, selectedStart, selectedEnd, manipulationMode, aggregationPeriod);
                        else
                            sg = new SampleGenerator(attribute.getDataSource(), attribute.getObject(), attribute, selectedStart, selectedEnd, ManipulationMode.TOTAL, aggregationPeriod);

                        samples = sg.generateSamples();
                        samples = sg.getAggregatedSamples(samples);
                        samples = factorizeSamples(samples);

                        AddZerosForMissingValues();
                    } else {
                        CalcJobFactory calcJobCreator = new CalcJobFactory();

                        CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), dataSource, calculationObject,
                                selectedStart, selectedEnd, aggregationPeriod);
                        samples = calcJob.getResults();
                    }


                    /**
                     * Checking for data inconsistencies
                     */


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
        }

        return samples;
    }

    public void setSamples(List<JEVisSample> samples) {
        this.samples = samples;
    }

    private void AddZerosForMissingValues() throws JEVisException {
        if (samples.size() > 0 && manipulationMode.equals(ManipulationMode.NONE)) {
            Period displaySampleRate = getAttribute().getDisplaySampleRate();
            if (displaySampleRate != null && displaySampleRate != Period.ZERO && displaySampleRate.toStandardDuration().getMillis() > 0) {
                DateTime startTS = samples.get(0).getTimestamp();
                while (startTS.isAfter(selectedStart)) {
                    startTS = startTS.minus(getAttribute().getDisplaySampleRate());
                    if (startTS.isAfter(selectedStart)) {
                        JEVisSample smp = new VirtualSample(startTS, 0.0);
                        smp.setNote("Empty");
                        samples.add(0, smp);
                    }
                }

                DateTime endTS = samples.get(samples.size() - 1).getTimestamp();
                while (endTS.isBefore(selectedEnd)) {
                    endTS = endTS.plus(getAttribute().getDisplaySampleRate());
                    if (endTS.isBefore(selectedEnd)) {
                        JEVisSample smp = new VirtualSample(endTS, 0.0);
                        smp.setNote("Empty");
                        samples.add(smp);
                    }
                }
            }
        }
    }

    private List<JEVisSample> factorizeSamples(List<JEVisSample> inputList) throws JEVisException {
        if (unit != null) {
            String outputUnit = UnitManager.getInstance().format(unit).replace("·", "");
            if (outputUnit.equals("")) outputUnit = unit.getLabel();

            String inputUnit = UnitManager.getInstance().format(attribute.getDisplayUnit()).replace("·", "");
            if (inputUnit.equals("")) inputUnit = attribute.getDisplayUnit().getLabel();

            ChartUnits cu = new ChartUnits();
            Double finalFactor = cu.scaleValue(inputUnit, outputUnit);

//            Double millisInput = null;
//            Double millisOutput = null;
//            try {
//                if (inputList.size() > 1) {
//                    Period inputPeriod = attribute.getDisplaySampleRate();
//                    if (!inputPeriod.equals(Period.years(1)) || !inputPeriod.equals(Period.months(3)) || !inputPeriod.equals(Period.months(1))) {
//                        millisInput = (double) inputPeriod.toStandardDuration().getMillis();
//                    } else throw new Exception("Input Period is greater than days, could not calculate duration.");
//
//                    Period outputPeriod = new Period(inputList.get(0).getTimestamp(), inputList.get(1).getTimestamp());
//                    if (!outputPeriod.equals(Period.years(1)) || !outputPeriod.equals(Period.months(3)) || !outputPeriod.equals(Period.months(1))) {
//                        millisOutput = (double) outputPeriod.toStandardDuration().getMillis();
//                    } else throw new Exception("Output Period is greater than days, could not calculate duration.");
//                }
//            } catch (Exception e) {
//                logger.error("Could not get calculate time scaling factor: " + e);
//            }

            double finalTimeFactor = 1.0;
//            if (millisOutput != null && millisOutput > 0 && millisInput > 0) {
//                finalTimeFactor = millisInput / millisOutput;
//            }

            double finalTimeFactor1 = finalTimeFactor;
            inputList.forEach(sample -> {
                try {
                    sample.setValue(sample.getValueAsDouble() * finalFactor * finalTimeFactor1);
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
        return title;
    }

    public void setTitle(String _title) {
        this.title = _title;
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
                JEVisAttribute attribute = null;
                String jevisClassName = getObject().getJEVisClassName();
                if (jevisClassName.equals("Data") || jevisClassName.equals("Clean Data")) {
                    if (dataProcessorObject == null) attribute = getObject().getAttribute("Value");
                    else attribute = getDataProcessor().getAttribute("Value");

                    this.attribute = attribute;
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color _color) {
        this.color = _color;
    }

    public boolean isSelectable() {
        return getAttribute() != null && getAttribute().hasSample();
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
}
