package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.api.JEVisUnit;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.dataprocessing.ManipulationMode;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisBarChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.plugin.charts.GraphPluginView;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class ColumnChartSerie {
    private static final Logger logger = LogManager.getLogger(ColumnChartSerie.class);
    private final Boolean hideShowIcons;
    private boolean forecast;
    private ChartDataModel singleRow;
    private Integer yAxis;
    private ObservableList<MultiAxisBarChart.Data<String, Number>> seriesData = FXCollections.observableArrayList();
    private MultiAxisBarChart.Series<String, Number> serie = new MultiAxisBarChart.Series<>(seriesData);
    private TableEntry tableEntry;
    private DateTime timeStampFromFirstSample = DateTime.now();
    private DateTime timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);
    private TreeMap<DateTime, JEVisSample> sampleMap;

    public ColumnChartSerie(ChartDataModel singleRow, Boolean hideShowIcons, boolean forecast) throws JEVisException {
        this.singleRow = singleRow;
        this.yAxis = singleRow.getAxis();
        this.hideShowIcons = hideShowIcons;
        this.forecast = forecast;

        generateSeriesFromSamples();
    }

    private void generateSeriesFromSamples() {
        timeStampFromFirstSample = DateTime.now();
        timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);
        List<JEVisSample> samples = new ArrayList<>();
        if (!forecast) {
            this.tableEntry = new TableEntry(getTableEntryName());
            this.serie.setName(getTableEntryName());
            this.tableEntry.setColor(ColorHelper.toColor(singleRow.getColor()));

            samples = singleRow.getSamples();
        } else {
            this.tableEntry = new TableEntry(getTableEntryName() + " - " + I18n.getInstance().getString("plugin.graph.chart.forecast.title"));
            this.serie.setName(getTableEntryName() + " - " + I18n.getInstance().getString("plugin.graph.chart.forecast.title"));
            this.tableEntry.setColor(ColorHelper.toColor(ColorHelper.colorToBrighter(singleRow.getColor())));

            samples = singleRow.getForecastSamples();
        }

        JEVisUnit unit = singleRow.getUnit();
        serie.getData().clear();

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

        for (JEVisSample sample : samples) {
            try {

                DateTime dateTime = sample.getTimestamp();
                Double currentValue = sample.getValueAsDouble();

                min = Math.min(min, currentValue);
                max = Math.max(max, currentValue);
                sum += currentValue;

                MultiAxisBarChart.Data<String, Number> data = new MultiAxisBarChart.Data<>();
                data.setXValue(dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm")));
                data.setYValue(currentValue);
                data.setExtraValue(yAxis);

                serie.getData().add(data);

                sampleMap.put(dateTime, sample);

            } catch (JEVisException e) {

            }
        }

        QuantityUnits qu = new QuantityUnits();
        boolean isQuantity = qu.isQuantityUnit(unit);

        if (samples.size() > 0) {
            avg = sum / samples.size();
        }

        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(2);
        nf_out.setMinimumFractionDigits(2);

        if (min == Double.MAX_VALUE || samples.size() == 0) {
            tableEntry.setMin("- " + getUnit());
        } else {
            tableEntry.setMin(nf_out.format(min) + " " + unit);
        }

        if (max == Double.MIN_VALUE || samples.size() == 0) {
            tableEntry.setMax("- " + getUnit());
        } else {
            tableEntry.setMax(nf_out.format(max) + " " + getUnit());
        }

        if (samples.size() == 0) {
            tableEntry.setAvg("- " + getUnit());
            tableEntry.setSum("- " + getUnit());
        } else {
            tableEntry.setAvg(nf_out.format(avg) + " " + getUnit());
            if (isQuantity) {
                tableEntry.setSum(nf_out.format(sum) + " " + getUnit());
            } else {
                if (qu.isSumCalculable(unit) && singleRow.getManipulationMode().equals(ManipulationMode.NONE)) {
                    try {
                        Period p = new Period(samples.get(0).getTimestamp(), samples.get(1).getTimestamp());
                        double factor = Period.hours(1).toStandardDuration().getMillis() / p.toStandardDuration().getMillis();
                        tableEntry.setSum(nf_out.format(sum / factor) + " " + qu.getSumUnit(unit));
                    } catch (Exception e) {
                        logger.error("Couldn't calculate periods");
                        tableEntry.setSum("- " + getUnit());
                    }
                } else {
                    tableEntry.setSum("- " + getUnit());
                }
            }
        }

        JEConfig.getStatusBar().progressProgressJob(GraphPluginView.JOB_NAME, 1, "Finished Serie");
    }

    public void setDataNodeColor(MultiAxisBarChart.Data<String, Number> data) {
        if (data.getNode() != null) {

            data.getNode().setStyle("-fx-background-color: " + singleRow.getColor() + ";");
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
        if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

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

    public Integer getyAxis() {
        return yAxis;
    }
}
