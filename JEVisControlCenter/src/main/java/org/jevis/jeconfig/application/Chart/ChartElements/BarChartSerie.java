package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.application.Chart.ChartDataModel;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisBarChart;
import org.jevis.jeconfig.application.Chart.Charts.MultiAxis.MultiAxisChart;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.List;
import java.util.TreeMap;

public class BarChartSerie {
    private static final Logger logger = LogManager.getLogger(BarChartSerie.class);
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private ObservableList<MultiAxisBarChart.Data<String, Number>> seriesData = FXCollections.observableArrayList();
    private TreeMap<Double, JEVisSample> sampleMap = new TreeMap<Double, JEVisSample>();
    private MultiAxisBarChart.Series<String, Number> serie;
    private TableEntry tableEntry;
    private DateTime timeStampFromFirstSample = DateTime.now();
    private DateTime timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);

    public BarChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        String unit = UnitManager.getInstance().format(singleRow.getUnit());
        if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        String tableEntryName = singleRow.getObject().getName();
        tableEntry = new TableEntry(tableEntryName);
        tableEntry.setColor(singleRow.getColor());
        tableData.add(tableEntry);

        List<JEVisSample> samples = singleRow.getSamples();

        if (samples.size() > 0) {
            try {

                if (samples.get(0).getTimestamp().isBefore(getTimeStampFromFirstSample()))
                    setTimeStampFromFirstSample(samples.get(0).getTimestamp());

                if (samples.get(samples.size() - 1).getTimestamp().isAfter(getTimeStampFromLastSample()))
                    setTimeStampFromLastSample(samples.get(samples.size() - 1).getTimestamp());

            } catch (Exception e) {
                logger.error("Couldn't get timestamps from samples. " + e);
            }
        }

        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        double avg = 0.0;
        Double sum = 0.0;

        for (JEVisSample sample : samples) {
            try {
                DateTime dateTime = sample.getTimestamp();
                Double value = sample.getValueAsDouble();

                Double currentValue = sample.getValueAsDouble();
                min = Math.min(min, currentValue);
                max = Math.max(max, currentValue);
                sum += sample.getValueAsDouble();

                DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
                String s = dateTime.toString(dtf);
                MultiAxisBarChart.Data<String, Number> data = new MultiAxisBarChart.Data<>(s, value);

                sampleMap.put((double) dateTime.getMillis(), sample);
                seriesData.add(data);

            } catch (JEVisException e) {

            }
        }

        QuantityUnits qu = new QuantityUnits();
        boolean isQuantity = qu.isQuantityUnit(singleRow.getUnit());

        if (samples.size() > 0)
            avg = sum / samples.size();

        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(2);
        nf_out.setMinimumFractionDigits(2);

        if (min == Double.MAX_VALUE || samples.size() == 0)
            tableEntry.setMin("- " + unit);
        else {
            tableEntry.setMin(nf_out.format(min) + " " + unit);
        }

        if (max == Double.MIN_VALUE || samples.size() == 0)
            tableEntry.setMax("- " + unit);
        else {
            tableEntry.setMax(nf_out.format(max) + " " + unit);
        }

        if (samples.size() == 0) {
            tableEntry.setAvg("- " + unit);
            tableEntry.setSum("- " + unit);
        } else {
            tableEntry.setAvg(nf_out.format(avg) + " " + unit);
            if (isQuantity) {
                tableEntry.setSum(nf_out.format(sum) + " " + unit);
            } else tableEntry.setSum("- " + unit);
        }

        serie = new MultiAxisChart.Series<>(tableEntryName, seriesData);
    }

    public MultiAxisBarChart.Series getSerie() {
        return serie;
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }

    public DateTime getTimeStampFromFirstSample() {
        return timeStampFromFirstSample;
    }

    public void setTimeStampFromFirstSample(DateTime timeStampFromFirstSample) {
        this.timeStampFromFirstSample = timeStampFromFirstSample;
    }

    public DateTime getTimeStampFromLastSample() {
        return timeStampFromLastSample;
    }

    public void setTimeStampFromLastSample(DateTime timeStampFromLastSample) {
        this.timeStampFromLastSample = timeStampFromLastSample;
    }
}
