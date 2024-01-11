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
import org.jevis.commons.dataprocessing.AggregationPeriod;
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
    final NumberFormat nf = NumberFormat.getNumberInstance();
    private final SimpleBooleanProperty shownInRenderer = new SimpleBooleanProperty();
    public String FINISHED_SERIE = I18n.getInstance().getString("graph.progress.finishedserie") + " ";
    boolean forecast = false;
    Integer yAxis;
    DoubleDataSet valueDataSet;
    DoubleDataSet noteDataSet;
    TableEntry tableEntry;
    ChartDataRow singleRow;
    Boolean showIcons;
    TreeMap<DateTime, JEVisSample> sampleMap;
    DateTime timeStampOfFirstSample = DateTime.now();
    DateTime timeStampOfLastSample = new DateTime(1990, 1, 1, 0, 0, 0);
    ValueWithDateTime minValue;
    ChartModel chartModel;
    Axis xAxis;
    ValueWithDateTime maxValue;
    private double sortCriteria;
    private double avg;
    private boolean aggregated = false;

    public XYChartSerie() {
    }

    public XYChartSerie(ChartModel chartModel, ChartDataRow singleRow, Boolean showIcons, boolean forecast) throws JEVisException {
        this.chartModel = chartModel;
        this.singleRow = singleRow;
        this.FINISHED_SERIE += singleRow.getName();
        this.yAxis = singleRow.getAxis();
        this.showIcons = showIcons;
        this.valueDataSet = new DoubleDataSet(singleRow.getName());
        this.noteDataSet = new DoubleDataSet(singleRow.getName());
        this.forecast = forecast;
        this.nf.setMinimumFractionDigits(chartModel.getMinFractionDigits());
        this.nf.setMaximumFractionDigits(chartModel.getMaxFractionDigits());
        if (singleRow.getDecimalDigits() > -1) {
            this.nf.setMinimumFractionDigits(singleRow.getDecimalDigits());
            this.nf.setMaximumFractionDigits(singleRow.getDecimalDigits());
        }
        minValue = new ValueWithDateTime(Double.MAX_VALUE, nf);
        maxValue = new ValueWithDateTime(-Double.MAX_VALUE, nf);

        this.aggregated = singleRow.getAggregationPeriod() != AggregationPeriod.NONE;

        generateSeriesFromSamples();
    }

    public void generateSeriesFromSamples() throws JEVisException {
        minValue = new ValueWithDateTime(Double.MAX_VALUE, nf);
        maxValue = new ValueWithDateTime(-Double.MAX_VALUE, nf);
        timeStampOfFirstSample = DateTime.now();
        timeStampOfLastSample = new DateTime(1990, 1, 1, 0, 0, 0);
        Color color = singleRow.getColor().deriveColor(0, 1, 1, 0.9);
        Color brighter = ColorHelper.colorToBrighter(singleRow.getColor());

        List<JEVisSample> samples = new ArrayList<>();
        if (aggregated) {
            JEVisUnit unit = singleRow.getUnit();

            QuantityUnits quantityUnits = new QuantityUnits();
            JEVisUnit sumUnit = quantityUnits.getSumUnit(unit);
            if (sumUnit != null && !sumUnit.equals(unit)) {
                singleRow.setUnit(sumUnit);
                singleRow.setSomethingChanged(true);
            }
        }

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

                if (samples.get(0).getTimestamp().isBefore(getTimeStampOfFirstSample()))
                    setTimeStampOfFirstSample(samples.get(0).getTimestamp());

                if (samples.get(samples.size() - 1).getTimestamp().isAfter(getTimeStampOfLastSample()))
                    setTimeStampOfLastSample(samples.get(samples.size() - 1).getTimestamp());

            } catch (Exception e) {
                logger.error("Couldn't get timestamps from samples. ", e);
            }
        }

        sampleMap = new TreeMap<>();

        avg = 0.0;
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

        minValue = new ValueWithDateTime(Double.MAX_VALUE, nf);
        maxValue = new ValueWithDateTime(-Double.MAX_VALUE, nf);
        avg = 0.0;
        sum = 0.0;
        for (JEVisSample sample : samples) {
            try {

                DateTime dateTime = sample.getTimestamp();
                Double currentValue = sample.getValueAsDouble();

                minValue.minCheck(dateTime, currentValue);
                maxValue.maxCheck(dateTime, currentValue);
                sum += currentValue;

            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        QuantityUnits qu = new QuantityUnits();
        boolean isQuantity = qu.isQuantityUnit(unit);

        if (!singleRow.getManipulationMode().equals(ManipulationMode.CUMULATE) && samples.size() > 0) {
            avg = sum / (samples.size() - zeroCount);
            this.avg = avg;
            sortCriteria = avg;
        }

        if (samples.isEmpty()) {
            finalAvg.append("- ").append(getUnit());
            finalSum.append("- ").append(getUnit());
        } else {
            if (!singleRow.isCalculation()) {
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

                        Period currentPeriod = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
                        Period rawPeriod = CleanDataObject.getPeriodForDate(singleRow.getAttribute().getObject(), samples.get(0).getTimestamp());

                        double newScaleFactor = cu.scaleValue(rawPeriod, unit.toString(), currentPeriod, sumUnit.toString());

                        sum = sum * newScaleFactor;

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

    public void setValueDataSet(DoubleDataSet dataSet) {
        valueDataSet = dataSet;
    }

    public DoubleDataSet getNoteDataSet() {
        return noteDataSet;
    }

    public void setNoteDataSet(DoubleDataSet dataSet) {
        this.noteDataSet = dataSet;
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }

    public DateTime getTimeStampOfFirstSample() {
        return this.timeStampOfFirstSample;
    }

    public void setTimeStampOfFirstSample(DateTime timeStampOfFirstSample) {
        this.timeStampOfFirstSample = timeStampOfFirstSample;
    }

    public DateTime getTimeStampOfLastSample() {
        return this.timeStampOfLastSample;
    }

    public void setTimeStampOfLastSample(DateTime timeStampOfLastSample) {
        this.timeStampOfLastSample = timeStampOfLastSample;
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

        String unit = String.valueOf(singleRow.getUnit());

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

    private Renderer valueDataSetRenderer;
    private Renderer noteDataSetRenderer;

    public void addValueDataSetRenderer(Renderer renderer) {
        this.valueDataSetRenderer = renderer;
        shownInRendererProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                renderer.getDatasets().add(this.getValueDataSet());
            } else {
                renderer.getDatasets().remove(this.getValueDataSet());
            }
        });
    }

    public void addNoteDataSetRenderer(Renderer renderer) {
        this.noteDataSetRenderer = renderer;
        shownInRendererProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1) {
                renderer.getDatasets().add(this.getNoteDataSet());
            } else {
                renderer.getDatasets().remove(this.getNoteDataSet());
            }
        });
    }

    public double getAvg() {
        return avg;
    }

    public void setTableEntry(TableEntry tableEntry) {
        this.tableEntry = tableEntry;
    }

    public void setSampleMap(TreeMap<DateTime, JEVisSample> sampleMap) {
        this.sampleMap = sampleMap;
    }

    public Renderer getValueDataSetRenderer() {
        return valueDataSetRenderer;
    }

    public Renderer getNoteDataSetRenderer() {
        return noteDataSetRenderer;
    }

    public NumberFormat getNf() {
        return nf;
    }
}
