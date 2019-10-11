package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.calculation.CalcJob;
import org.jevis.commons.calculation.CalcJobFactory;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.database.SampleHandler;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class XYChartSerie {
    private static final Logger logger = LogManager.getLogger(XYChartSerie.class);
    Integer yAxis;
    MultiAxisChart.Series<Number, Number> serie;
    TableEntry tableEntry;
    ChartDataModel singleRow;
    Boolean hideShowIcons;
    TreeMap<DateTime, JEVisSample> sampleMap;
    DateTime timeStampFromFirstSample = DateTime.now();
    DateTime timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);
    Double minValue = Double.MAX_VALUE;
    Double maxValue = -Double.MAX_VALUE;

    public XYChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        this.singleRow = singleRow;
        this.yAxis = singleRow.getAxis();
        this.hideShowIcons = hideShowIcons;
        this.serie = new MultiAxisChart.Series<>();

        generateSeriesFromSamples();
    }

    public void generateSeriesFromSamples() throws JEVisException {
        timeStampFromFirstSample = DateTime.now();
        timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);
        tableEntry = new TableEntry(getTableEntryName());
        this.serie.setName(getTableEntryName());

        tableEntry.setColor(singleRow.getColor());

        List<JEVisSample> samples = singleRow.getSamples();

        JEVisUnit unit = singleRow.getUnit();

        serie.getData().clear();

        int samplesSize = samples.size();
//        int seriesDataSize = serie.getData().size();

//        if (samplesSize < seriesDataSize) {
//            serie.getData().subList(samplesSize, seriesDataSize).clear();
//        } else if (samplesSize > seriesDataSize) {
//            for (int i = seriesDataSize; i < samplesSize; i++) {
//                serie.getData().add(new MultiAxisChart.Data<>());
//            }
//        }

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

        List<MultiAxisChart.Data<Number, Number>> dataList = new ArrayList<>();
        for (JEVisSample sample : samples) {
            try {
//                int index = samples.indexOf(sample);

                DateTime dateTime = sample.getTimestamp();
                Double currentValue = sample.getValueAsDouble();

                if (!sample.getNote().contains("Zeros")) {
                    min = Math.min(min, currentValue);
                    max = Math.max(max, currentValue);
                    sum += currentValue;
                } else {
                    zeroCount++;
                }

                Long timestamp = dateTime.getMillis();

                MultiAxisChart.Data<Number, Number> data = new MultiAxisChart.Data<>();
                data.setXValue(timestamp);
                data.setYValue(currentValue);
                data.setExtraValue(yAxis);

                data.setNode(null);
                data.setNode(generateNode(sample));

                setDataNodeColor(data);
                dataList.add(data);

                sampleMap.put(dateTime, sample);

            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        if (singleRow.getManipulationMode().equals(ManipulationMode.CUMULATE)) {
            avg = max / samples.size();
            sum = max;
        }


        serie.getData().setAll(dataList);
        JEConfig.getStatusBar().progressProgressJob(GraphPluginView.JOB_NAME, 1, "Finished Serie");


        updateTableEntry(samples, unit, min, max, avg, sum, zeroCount);
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
            Platform.runLater(() -> tableEntry.setPeriod(new Period(finalFirstTS, finalSecondTS).toString(PeriodFormat.wordBased().withLocale(I18n.getInstance().getLocale()))));
        }

        QuantityUnits qu = new QuantityUnits();
        boolean isQuantity = qu.isQuantityUnit(unit);

        if (!singleRow.getManipulationMode().equals(ManipulationMode.CUMULATE) && samples.size() > 0) {
            avg = sum / (samples.size() - zeroCount);
        }

        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(2);
        nf_out.setMinimumFractionDigits(2);

        if (min == Double.MAX_VALUE || samples.size() == 0) {
            Platform.runLater(() -> tableEntry.setMin("- " + getUnit()));
        } else {
            Platform.runLater(() -> tableEntry.setMin(nf_out.format(min) + " " + getUnit()));
        }

        if (max == Double.MIN_VALUE || samples.size() == 0) {
            Platform.runLater(() -> tableEntry.setMax("- " + getUnit()));
        } else {
            Platform.runLater(() -> tableEntry.setMax(nf_out.format(max) + " " + getUnit()));
        }

        if (samples.size() == 0) {
            Platform.runLater(() -> tableEntry.setAvg("- " + getUnit()));
            Platform.runLater(() -> tableEntry.setSum("- " + getUnit()));
        } else {
            if (!singleRow.getEnPI()) {
                double finalAvg = avg;
                Platform.runLater(() -> tableEntry.setAvg(nf_out.format(finalAvg) + " " + getUnit()));
            } else {
                CalcJobFactory calcJobCreator = new CalcJobFactory();

                CalcJob calcJob = calcJobCreator.getCalcJobForTimeFrame(new SampleHandler(), singleRow.getObject().getDataSource(), singleRow.getCalculationObject(),
                        firstTS, lastTS, true);
                List<JEVisSample> results = calcJob.getResults();

                if (results.size() == 1) {
                    Platform.runLater(() -> {
                        try {
                            tableEntry.setAvg(nf_out.format(results.get(0).getValueAsDouble()) + " " + getUnit());
                        } catch (JEVisException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    Platform.runLater(() -> tableEntry.setAvg("- " + getUnit()));
                }
                double finalAvg1 = avg;
                Platform.runLater(() -> tableEntry.setEnpi(nf_out.format(finalAvg1) + " " + getUnit()));
            }
            if (isQuantity) {
//                tableEntry.setSum(nf_out.format(sum / singleRow.getScaleFactor() / singleRow.getTimeFactor()) + " " + getUnit());
                Double finalSum = sum;
                Platform.runLater(() -> tableEntry.setSum(nf_out.format(finalSum) + " " + getUnit()));
            } else {
                if (qu.isSumCalculable(unit) && singleRow.getManipulationMode().equals(ManipulationMode.NONE)) {
                    try {
                        Period period = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
                        if (period.getMonths() < 1 && period.getYears() < 1) {
                            long periodMillis = period.toStandardDuration().getMillis();
                            long hourMillis = Period.hours(1).toStandardDuration().getMillis();
                            Double factor = (double) hourMillis / (double) periodMillis;
//                            tableEntry.setSum(nf_out.format(sum / factor) + " " + qu.getSumUnit(unit));
                            sum = sum / singleRow.getScaleFactor() / singleRow.getTimeFactor();
                            Double finalSum1 = sum;
                            Platform.runLater(() -> tableEntry.setSum(nf_out.format(finalSum1) + " " + qu.getSumUnit(unit)));
                        } else {
                            double periodMillis = 0.0;

                            if (period.getMonths() == 1) {
                                periodMillis = (double) Period.days(1).toStandardDuration().getMillis() * 30.4375;
                            } else if (period.getMonths() == 3) {
                                periodMillis = (double) Period.days(1).toStandardDuration().getMillis() * 30.4375 * 3;
                            } else if (period.getYears() == 1) {
                                periodMillis = (double) Period.days(1).toStandardDuration().getMillis() * 365.25;
                            }

                            long hourMillis = Period.hours(1).toStandardDuration().getMillis();
                            Double factor = (double) hourMillis / periodMillis;
//                            tableEntry.setSum(nf_out.format(sum / factor) + " " + qu.getSumUnit(unit));
                            sum = sum / singleRow.getScaleFactor() / singleRow.getTimeFactor();
                            Double finalSum2 = sum;
                            Platform.runLater(() -> tableEntry.setSum(nf_out.format(finalSum2) + " " + qu.getSumUnit(unit)));
                        }
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


    public void setDataNodeColor(MultiAxisChart.Data<Number, Number> data) {
        if (data.getNode() != null) {
            Color currentColor = singleRow.getColor();
            String hexColor = toRGBCode(currentColor);
            data.getNode().setStyle("-fx-background-color: " + hexColor + ";");
        }
    }

    public Node generateNode(JEVisSample sample) throws JEVisException {
        Note note = new Note(sample);

        if (note.getNote() != null && hideShowIcons) {
            if (sample.getNote().contains("Zeros")) {
                return null;
            }
            note.getNote().setVisible(true);
            return note.getNote();
        } else {
            if (sample.getNote() != null && sample.getNote().contains("Zeros")) {
                return null;
            }
            Rectangle rect = new Rectangle(0, 0);
            rect.setFill(singleRow.getColor());
            rect.setVisible(false);
            return rect;
        }
    }


    public MultiAxisChart.Series getSerie() {
        return serie;
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

    public ChartDataModel getSingleRow() {
        return singleRow;
    }

    public void setSingleRow(ChartDataModel singleRow) {
        this.singleRow = singleRow;
        this.yAxis = singleRow.getAxis();
    }

    public String getTableEntryName() {
        return singleRow.getObject().getName();
    }

    public String getUnit() {

        String unit = UnitManager.getInstance().format(singleRow.getUnit());

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
}
