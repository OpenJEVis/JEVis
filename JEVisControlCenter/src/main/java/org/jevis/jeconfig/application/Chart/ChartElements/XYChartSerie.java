package org.jevis.jeconfig.application.Chart.ChartElements;


import com.ibm.icu.text.NumberFormat;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.renderer.Renderer;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
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
import org.jevis.commons.unit.ChartUnits.ChartUnits;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.XYChart;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.jevis.jeconfig.application.Chart.data.ValueWithDateTime;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class XYChartSerie {
    private static final Logger logger = LogManager.getLogger(XYChartSerie.class);
    public final String FINISHED_SERIE;
    final boolean forecast;
    final NumberFormat nf = NumberFormat.getNumberInstance();
    Integer yAxis;
    DoubleDataSet valueDataSet;
    DoubleDataSet noteDataSet;
    TableEntry tableEntry;
    ChartDataRow singleRow;
    Boolean showIcons;
    TreeMap<DateTime, JEVisSample> sampleMap;
    DateTime timeStampFromFirstSample = DateTime.now();
    DateTime timeStampFromLastSample = new DateTime(1990, 1, 1, 0, 0, 0);
    private final SimpleBooleanProperty shownInRenderer = new SimpleBooleanProperty();
    ValueWithDateTime minValue = new ValueWithDateTime(Double.MAX_VALUE);
    ChartModel chartModel;
    Axis xAxis;
    ValueWithDateTime maxValue = new ValueWithDateTime(-Double.MAX_VALUE);
    private double sortCriteria;


    public XYChartSerie(ChartModel chartModel, ChartDataRow singleRow, Boolean showIcons, boolean forecast) throws JEVisException {
        this.chartModel = chartModel;
        this.singleRow = singleRow;
        this.FINISHED_SERIE = I18n.getInstance().getString("graph.progress.finishedserie") + " " + singleRow.getName();
        this.yAxis = singleRow.getAxis();
        this.showIcons = showIcons;
        this.valueDataSet = new DoubleDataSet(singleRow.getName());
        this.noteDataSet = new DoubleDataSet(singleRow.getName());
        this.forecast = forecast;
        this.nf.setMinimumFractionDigits(chartModel.getMinFractionDigits());
        this.nf.setMaximumFractionDigits(chartModel.getMaxFractionDigits());

        generateSeriesFromSamples();
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
            this.valueDataSet.setStyle("strokeColor=" + color + "; fillColor=" + color);

            samples = singleRow.getSamples();
        } else {
            this.tableEntry = new TableEntry(getTableEntryName() + " - " + I18n.getInstance().getString("plugin.graph.chart.forecast.title"));
            this.valueDataSet.setName(getTableEntryName() + " - " + I18n.getInstance().getString("plugin.graph.chart.forecast.title"));
            this.tableEntry.setColor(brighter);
            this.valueDataSet.setStyle("strokeColor=" + brighter + "; fillColor=" + brighter);

            samples = singleRow.getForecastSamples();
        }

        JEVisUnit unit = singleRow.getUnit();
        minValue.setUnit(unit);
        maxValue.setUnit(unit);
        valueDataSet.clearData();

        int samplesSize = samples.size();

        if (samplesSize > 0) {
            try {

                if (samples.get(0).getTimestamp().isBefore(getTimeStampFromFirstSample()))
                    setTimeStampFromFirstSample(samples.get(0).getTimestamp());

                if (samples.get(samples.size() - 1).getTimestamp().isAfter(getTimeStampFromLastSample()))
                    setTimeStampFromLastSample(samples.get(samples.size() - 1).getTimestamp());

            } catch (Exception e) {
                logger.error("Couldn't get timestamps from samples. ", e);
            }
        }

        sampleMap = new TreeMap<>();

        double avg = 0.0;
        double sum = 0.0;
        long zeroCount = 0;

        int noteIndex = 0;
        for (JEVisSample sample : samples) {
            try {

                DateTime dateTime = sample.getTimestamp();
                Double currentValue = sample.getValueAsDouble();

                minValue.minCheck(dateTime, currentValue);
                maxValue.maxCheck(dateTime, currentValue);
                sum += currentValue;

                double timestamp = dateTime.getMillis() / 1000d;

                valueDataSet.add(timestamp, currentValue);

                String noteString = generateNote(sample);
                if (noteString != null && showIcons) {
                    noteDataSet.add(timestamp, currentValue);
                    noteDataSet.addDataLabel(noteIndex, noteString);
                    if (!forecast) {
                        noteDataSet.addDataStyle(noteIndex, "strokeColor=" + color + "; fillColor= " + color + ";strokeDashPattern=0");
                    } else {
                        noteDataSet.addDataStyle(noteIndex, "strokeColor=" + brighter + "; fillColor= " + brighter + ";strokeDashPattern=0");
                    }
                    noteIndex++;
                }

                sampleMap.put(dateTime, sample);

            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        if (singleRow.getManipulationMode().equals(ManipulationMode.CUMULATE)) {
            avg = maxValue.getValue() / samples.size();
            sum = maxValue.getValue();
        }

        updateTableEntry(samples, unit, minValue, maxValue, avg, sum, zeroCount, false);

        JEConfig.getStatusBar().progressProgressJob(XYChart.JOB_NAME, 1, FINISHED_SERIE);
    }

    public void updateTableEntry(List<JEVisSample> samples, JEVisUnit unit, ValueWithDateTime min, ValueWithDateTime max, double avg, Double sum, long zeroCount, boolean later) throws JEVisException {

        StringBuilder finalAvg = new StringBuilder();
        StringBuilder finalSum = new StringBuilder();
        StringBuilder finalPeriod = new StringBuilder();

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
            try {
                String s = "";
                try {
                    Period period;
                    switch (singleRow.getAggregationPeriod()) {
                        case QUARTER_HOURLY:
                            s = Period.minutes(15).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case HOURLY:
                            s = Period.hours(1).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case DAILY:
                            s = Period.days(1).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case WEEKLY:
                            s = Period.weeks(1).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case MONTHLY:
                            s = Period.months(1).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case QUARTERLY:
                            s = Period.hours(3).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case YEARLY:
                            s = Period.years(1).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case THREEYEARS:
                            s = Period.years(3).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case FIVEYEARS:
                            s = Period.years(5).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case TENYEARS:
                            s = Period.years(10).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            break;
                        case NONE:
                        default:
                            JEVisAttribute attribute = samples.get(0).getAttribute();

                            if (attribute != null && attribute.getObject().getAttribute("Period") != null) {
                                Period periodForDate = CleanDataObject.getPeriodForDate(attribute.getObject(), firstTS);

                                if (!periodForDate.equals(Period.ZERO)) {
                                    s = new Period(firstTS, secondTS).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                                } else if (periodForDate.equals(Period.ZERO)) {
                                    s = I18n.getInstance().getString("plugin.unit.samplingrate.async");
                                }
                            } else if (attribute == null) {
                                s = new Period(firstTS, secondTS).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()));
                            }
                            break;
                    }
                } catch (Exception e) {
                    s = "-";
                }
                finalPeriod.append(s);
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

        if (samples.size() == 0) {
            finalAvg.append("- ").append(getUnit());
            finalSum.append("- ").append(getUnit());
        } else {
            if (!singleRow.getEnPI()) {
                finalAvg.append(nf.format(avg)).append(" ").append(getUnit());
            } else {
                DateTime finalFirstTS1 = firstTS;
                DateTime finalLastTS = lastTS;
                double finalAvg1 = avg;
                Task task = new Task() {
                    @Override
                    protected Object call() throws Exception {
                        try {
                            CalcJobFactory calcJobCreator = new CalcJobFactory();

                            CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), singleRow.getObject().getDataSource(), singleRow.getCalculationObject(),
                                    finalFirstTS1, finalLastTS, true);
                            List<JEVisSample> results = calcJob.getResults();

                            StringBuilder teAvg = new StringBuilder();

                            if (results.size() == 1) {
                                try {
                                    teAvg.append(nf.format(results.get(0).getValueAsDouble())).append(" ").append(getUnit());
                                } catch (Exception e) {
                                    logger.error("Couldn't get calculation result");
                                }
                            } else {
                                teAvg.append("- ").append(getUnit());
                            }
                            String teEnPI = nf.format(finalAvg1) + " " + getUnit();

                            if (later) {
                                Platform.runLater(() -> {
                                    tableEntry.setAvg(teAvg.toString());
                                    tableEntry.setEnpi(teEnPI);
                                });
                            } else {
                                tableEntry.setAvg(teAvg.toString());
                                tableEntry.setEnpi(teEnPI);
                            }
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
                finalSum.append(nf.format(sum)).append(" ").append(getUnit());
            } else {
                if (qu.isSumCalculable(unit) && singleRow.getManipulationMode().equals(ManipulationMode.NONE)) {
                    try {
                        JEVisUnit sumUnit = qu.getSumUnit(unit);
                        ChartUnits cu = new ChartUnits();
                        double newScaleFactor = cu.scaleValue(unit.toString(), sumUnit.toString());
                        JEVisUnit inputUnit = singleRow.getAttribute().getInputUnit();
                        JEVisUnit sumUnitOfInputUnit = qu.getSumUnit(inputUnit);

                        if (qu.isDiffPrefix(sumUnitOfInputUnit, sumUnit)) {
                            sum = sum * newScaleFactor / singleRow.getTimeFactor();
                        } else {
                            sum = sum / singleRow.getScaleFactor() / singleRow.getTimeFactor();
                        }

                        Double finalSum1 = sum;
                        finalSum.append(nf.format(finalSum1)).append(" ").append(sumUnit);
                    } catch (Exception e) {
                        logger.error("Couldn't calculate periods");
                        finalSum.append("- ").append(getUnit());
                    }
                } else {
                    finalSum.append("- ").append(getUnit());
                }
            }
        }

        if (later) {
            Platform.runLater(() -> {
                tableEntry.setPeriod(finalPeriod.toString());
                tableEntry.setMin(min);
                tableEntry.setMax(max);
                tableEntry.setAvg(finalAvg.toString());
                tableEntry.setSum(finalSum.toString());
            });
        } else {
            tableEntry.setPeriod(finalPeriod.toString());
            tableEntry.setMin(min);
            tableEntry.setMax(max);
            tableEntry.setAvg(finalAvg.toString());
            tableEntry.setSum(finalSum.toString());
        }

        singleRow.setMin(min);
        singleRow.setMax(max);
        singleRow.setAvg(avg);
        singleRow.setSum(sum);
    }

    public String generateNote(JEVisSample sample) throws JEVisException {
        Note note = new Note(sample, singleRow.getNoteSamples().get(sample.getTimestamp()), singleRow.getAlarms(false).get(sample.getTimestamp()));

        return note.getNoteAsString();
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

    public ValueWithDateTime getMinValue() {
        return minValue;
    }

    public void setMinValue(ValueWithDateTime minValue) {
        this.minValue = minValue;
    }

    public ValueWithDateTime getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(ValueWithDateTime maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getyAxis() {
        return yAxis;
    }

    public double getSortCriteria() {
        return sortCriteria;
    }

    public Axis getXAxis() {
        return xAxis;
    }

    public void setXAxis(Axis xAxis) {
        this.xAxis = xAxis;
    }

    public boolean isShownInRenderer() {
        return shownInRenderer.get();
    }

    public void setShownInRenderer(boolean shownInRenderer) {
        this.shownInRenderer.set(shownInRenderer);
    }

    public SimpleBooleanProperty shownInRendererProperty() {
        return shownInRenderer;
    }

    public void addValueDataSetRenderer(Renderer renderer) {
        shownInRendererProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                renderer.getDatasets().add(this.getValueDataSet());
            } else {
                renderer.getDatasets().remove(this.getValueDataSet());
            }
        });
    }

    public void addNoteDataSetRenderer(Renderer renderer) {
        shownInRendererProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                renderer.getDatasets().add(this.getNoteDataSet());
            } else {
                renderer.getDatasets().remove(this.getNoteDataSet());
            }
        });
    }
}
