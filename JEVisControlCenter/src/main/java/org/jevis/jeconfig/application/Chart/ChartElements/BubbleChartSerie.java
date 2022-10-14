package org.jevis.jeconfig.application.Chart.ChartElements;


import de.gsi.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.CleanDataObject;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.XYChart;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class BubbleChartSerie extends XYChartSerie {
    private static final Logger logger = LogManager.getLogger(BubbleChartSerie.class);
    Integer yAxis;
    DoubleDataSet valueDataSet;
    DoubleDataSet noteDataSet;
    TableEntry tableEntry;
    ChartDataRow singleRow;
    Boolean showIcons;
    TreeMap<DateTime, JEVisSample> sampleMap;
    DateTime timeStampFromFirstSample = DateTime.now();
    DateTime timeStampFromLastSample = new DateTime(1990, 1, 1, 0, 0, 0);
    Double minValue = Double.MAX_VALUE;
    Double maxValue = -Double.MAX_VALUE;
    private double sortCriteria;

    public BubbleChartSerie(ChartModel chartModelSetting, ChartDataRow singleRow, Boolean showIcons, boolean forecast) throws JEVisException {
        super(chartModelSetting, singleRow, showIcons, forecast);
    }

    public void generateSeriesFromSamples() throws JEVisException {
        timeStampFromFirstSample = DateTime.now();
        timeStampFromLastSample = new DateTime(1990, 1, 1, 0, 0, 0);
        Color color = singleRow.getColor().deriveColor(0, 1, 1, 0.9);
        Color brighter = ColorHelper.colorToBrighter(singleRow.getColor());

        List<JEVisSample> samples = new ArrayList<>();
        if (!forecast) {
            this.tableEntry = new TableEntry(getTableEntryName());
            this.valueDataSet.setName(getTableEntryName());
            this.tableEntry.setColor(color);
            this.valueDataSet.setStyle("strokeColor=" + ColorHelper.toRGBCode(color) + "; fillColor=" + ColorHelper.toRGBCode(color));

            samples = singleRow.getSamples();
        } else {
            this.tableEntry = new TableEntry(getTableEntryName() + " - " + I18n.getInstance().getString("plugin.graph.chart.forecast.title"));
            this.valueDataSet.setName(getTableEntryName() + " - " + I18n.getInstance().getString("plugin.graph.chart.forecast.title"));
            this.tableEntry.setColor(brighter);
            this.valueDataSet.setStyle("strokeColor=" + ColorHelper.toRGBCode(brighter) + "; fillColor=" + ColorHelper.toRGBCode(brighter));

            samples = singleRow.getForecastSamples();
        }

        JEVisUnit unit = singleRow.getUnit();

        valueDataSet.clearData();

        int samplesSize = samples.size();

        if (samplesSize > 0) {
            try {

                if (samples.get(0).getTimestamp().isBefore(getTimeStampFromFirstSample()))
                    setTimeStampFromFirstSample(samples.get(0).getTimestamp());

                if (samples.get(samples.size() - 1).getTimestamp().isAfter(getTimeStampFromLastSample()))
                    setTimeStampFromLastSample(samples.get(samples.size() - 1).getTimestamp());

            } catch (Exception e) {
                logger.error("Couldn't get timestamps from samples. " + e);
            }
        }

        sampleMap = new TreeMap<>();

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double avg = 0.0;
        Double sum = 0.0;
        long zeroCount = 0;

        int noteIndex = 0;
        for (int i = 0, size = samples.size(); i < size; i++) {
            JEVisSample sample = samples.get(i);
            try {

                DateTime dateTime = sample.getTimestamp();
                Double currentValue = sample.getValueAsDouble();

                min = Math.min(min, currentValue);
                max = Math.max(max, currentValue);
                sum += currentValue;

                Double timestamp = dateTime.getMillis() / 1000d;

                valueDataSet.add(timestamp, currentValue);

                String noteString = generateNote(sample);
                if (noteString != null && showIcons) {
                    noteDataSet.add(timestamp, currentValue);
                    noteDataSet.addDataLabel(noteIndex, noteString);
                    if (!forecast) {
                        noteDataSet.addDataStyle(noteIndex, "strokeColor=" + ColorHelper.toRGBCode(color) + "; fillColor= " + ColorHelper.toRGBCode(color) + ";strokeDashPattern=0");
                    } else {
                        noteDataSet.addDataStyle(noteIndex, "strokeColor=" + ColorHelper.toRGBCode(brighter) + "; fillColor= " + ColorHelper.toRGBCode(brighter) + ";strokeDashPattern=0");
                    }
                    noteIndex++;
                }

                sampleMap.put(dateTime, sample);

            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        if (singleRow.getManipulationMode().equals(ManipulationMode.CUMULATE)) {
            avg = max / samples.size();
            sum = max;
        }

        updateTableEntry(samples, unit, min, max, avg, sum, zeroCount);

        JEConfig.getStatusBar().progressProgressJob(XYChart.JOB_NAME, 1, FINISHED_SERIE);
    }

    public void updateTableEntry(List<JEVisSample> samples, JEVisUnit unit, double min, double max, double avg, Double sum, long zeroCount) throws JEVisException {

        DateTime firstTS = null;
        DateTime secondTS = null;
        DateTime lastTS = null;
        if (!samples.isEmpty()) {
            firstTS = samples.get(0).getTimestamp();
            if (samples.size() > 1) {
                secondTS = samples.get(1).getTimestamp();
            }
            lastTS = samples.get(samples.size() - 1).getTimestamp();
        }

        if (firstTS != null && secondTS != null) {
            DateTime finalFirstTS = firstTS;
            DateTime finalSecondTS = secondTS;
            try {
                String s = "";
                try {
                    JEVisAttribute attribute = samples.get(0).getAttribute();
                    if (attribute != null && attribute.getObject().getAttribute("Period") != null) {
                        Period periodForDate = CleanDataObject.getPeriodForDate(attribute.getObject(), finalFirstTS);

                        if (!periodForDate.equals(Period.ZERO)) {
                            s = new Period(finalFirstTS, finalSecondTS).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                        } else if (periodForDate.equals(Period.ZERO)) {
                            s = I18n.getInstance().getString("plugin.unit.samplingrate.async");
                        }
                    } else if (attribute == null) {
                        s = new Period(finalFirstTS, finalSecondTS).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                    }
                } catch (Exception e) {
                    s = "-";
                }
                String finalS = s;
                Platform.runLater(() -> tableEntry.setPeriod(finalS));
            } catch (Exception e) {
                logger.error("Couldn't calculate period word-based");
            }
        }

        QuantityUnits qu = new QuantityUnits();
        boolean isQuantity = qu.isQuantityUnit(unit);

        if (!singleRow.getManipulationMode().equals(ManipulationMode.CUMULATE) && samples.size() > 0) {
            avg = sum / (samples.size() - zeroCount);
            sortCriteria = avg;
        }

        if (min == Double.MAX_VALUE || samples.size() == 0) {
            Platform.runLater(() -> tableEntry.setMin("- " + getUnit()));
        } else {
            Platform.runLater(() -> tableEntry.setMin(nf.format(min) + " " + getUnit()));
        }

        if (max == -Double.MAX_VALUE || samples.size() == 0) {
            Platform.runLater(() -> tableEntry.setMax("- " + getUnit()));
        } else {
            Platform.runLater(() -> tableEntry.setMax(nf.format(max) + " " + getUnit()));
        }

        if (samples.size() == 0) {
            Platform.runLater(() -> tableEntry.setAvg("- " + getUnit()));
            Platform.runLater(() -> tableEntry.setSum("- " + getUnit()));
        } else {
            if (!singleRow.getEnPI()) {
                double finalAvg = avg;
                Platform.runLater(() -> tableEntry.setAvg(nf.format(finalAvg) + " " + getUnit()));
            } else {
                DateTime finalFirstTS1 = firstTS;
                DateTime finalLastTS = lastTS;
                double finalAvg2 = avg;
                Task task = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            CalcJobFactory calcJobCreator = new CalcJobFactory();

                            CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), singleRow.getObject().getDataSource(), singleRow.getCalculationObject(),
                                    finalFirstTS1, finalLastTS, true);
                            List<JEVisSample> results = calcJob.getResults();

                            if (results.size() == 1) {
                                Platform.runLater(() -> {
                                    try {
                                        tableEntry.setAvg(nf.format(results.get(0).getValueAsDouble()) + " " + getUnit());
                                    } catch (JEVisException e) {
                                        logger.error("Couldn't get calculation result");
                                    }
                                });
                            } else {
                                Platform.runLater(() -> tableEntry.setAvg("- " + getUnit()));
                            }
                            double finalAvg1 = finalAvg2;
                            Platform.runLater(() -> tableEntry.setEnpi(nf.format(finalAvg1) + " " + getUnit()));
                        } catch (Exception e) {
                            failed();
                        } finally {
                            succeeded();
                        }
                        return null;
                    }
                };
                JEConfig.getStatusBar().addTask(XYChart.class.getName(), task, XYChart.taskImage, true);
            }
            if (isQuantity) {
//                tableEntry.setSum(nf_out.format(sum / singleRow.getScaleFactor() / singleRow.getTimeFactor()) + " " + getUnit());
                Double finalSum = sum;
                Platform.runLater(() -> tableEntry.setSum(nf.format(finalSum) + " " + getUnit()));
            } else {
                if (qu.isSumCalculable(unit) && singleRow.getManipulationMode().equals(ManipulationMode.NONE)) {
                    try {
//                        Period period = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
//                        if (period.getMonths() < 1 && period.getYears() < 1) {
//                            long periodMillis = period.toStandardDuration().getMillis();
//                            long hourMillis = Period.hours(1).toStandardDuration().getMillis();
//                            Double factor = (double) hourMillis / (double) periodMillis;
//                            tableEntry.setSum(nf_out.format(sum / factor) + " " + qu.getSumUnit(unit));
                        sum = sum / singleRow.getScaleFactor();
                        Double finalSum1 = sum;
                        Platform.runLater(() -> tableEntry.setSum(nf.format(finalSum1) + " " + qu.getSumUnit(unit)));
//                        } else {
//                            double periodMillis = 0.0;

//                            if (period.getMonths() == 1) {
//                                periodMillis = (double) Period.days(1).toStandardDuration().getMillis() * 30.4375;
//                            } else if (period.getMonths() == 3) {
//                                periodMillis = (double) Period.days(1).toStandardDuration().getMillis() * 30.4375 * 3;
//                            } else if (period.getYears() == 1) {
//                                periodMillis = (double) Period.days(1).toStandardDuration().getMillis() * 365.25;
//                            }

//                            long hourMillis = Period.hours(1).toStandardDuration().getMillis();
//                            Double factor = (double) hourMillis / periodMillis;
//                            tableEntry.setSum(nf_out.format(sum / factor) + " " + qu.getSumUnit(unit));
//                            sum = sum / singleRow.getScaleFactor();
//                            Double finalSum2 = sum;
//                            Platform.runLater(() -> tableEntry.setSum(nf_out.format(finalSum2) + " " + qu.getSumUnit(unit)));
//                        }
                    } catch (Exception e) {
                        logger.error("Couldn't calculate periods");
                        Platform.runLater(() -> tableEntry.setSum("- " + getUnit()));
                    }
                } else {
                    Platform.runLater(() -> tableEntry.setSum("- " + getUnit()));
                }
            }
        }

        singleRow.setMin(min);
        singleRow.setMax(max);
        singleRow.setAvg(avg);
        singleRow.setSum(sum);
    }

    public String generateNote(JEVisSample sample) throws JEVisException {
        Note note = new Note(sample, singleRow.getNoteSamples().get(sample.getTimestamp()), singleRow.getAlarms(false).get(sample.getTimestamp()));

        return note.getNoteAsString();
//        if (note.getNote() != null && hideShowIcons) {
//            if (sample.getNote().contains("Zeros")) {
//                return null;
//            }
//            note.getNote().setVisible(true);
//            return note.getNoteAsString();
//        } else return null;
    }

    public DoubleDataSet getValueDataSet() {
        return valueDataSet;
    }

    public DoubleDataSet getNoteDataSet() {
        return noteDataSet;
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }

    public DateTime getTimeStampFromFirstSample() {
        return this.timeStampFromFirstSample;
    }

    public void setTimeStampFromFirstSample(DateTime timeStampFromFirstSample) {
        this.timeStampFromFirstSample = timeStampFromFirstSample;
    }

    public DateTime getTimeStampFromLastSample() {
        return this.timeStampFromLastSample;
    }

    public void setTimeStampFromLastSample(DateTime timeStampFromLastSample) {
        this.timeStampFromLastSample = timeStampFromLastSample;
    }

    public ChartDataRow getSingleRow() {
        return singleRow;
    }

    public void setSingleRow(ChartDataRow singleRow) {
        this.singleRow = singleRow;
        this.yAxis = singleRow.getAxis();
    }

    public String getTableEntryName() {
        if (singleRow.getName() == null || singleRow.getName().equals("")) {
            return singleRow.getObject().getName();
        } else {
            return singleRow.getName();
        }
    }

    public String getUnit() {

        String unit = "" + singleRow.getUnit();

        if (unit.equals("")) unit = singleRow.getUnit().getLabel();
//        if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        return unit;
    }

    public TreeMap<DateTime, JEVisSample> getSampleMap() {
        return sampleMap;
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getyAxis() {
        return yAxis;
    }

    public double getSortCriteria() {
        return sortCriteria;
    }
}
