package org.jevis.jeconfig.application.Chart.ChartElements;


import com.ibm.icu.text.NumberFormat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.Chart.data.ChartDataRow;
import org.jevis.jeconfig.application.Chart.data.ChartModel;
import org.joda.time.DateTime;

import java.util.List;

public class BarChartSerie {
    private static final Logger logger = LogManager.getLogger(BarChartSerie.class);
    private final ChartModel chartModel;
    private final String FINISHED_SERIE;
    private final ObservableList<TableEntry> tableData = FXCollections.observableArrayList();

    private final TableEntry tableEntry;
    private DateTime timeStampFromFirstSample = DateTime.now();
    private DateTime timeStampFromLastSample = new DateTime(1990, 1, 1, 0, 0, 0);
    private final XYChart.Data<Number, String> data;

    public BarChartSerie(ChartModel chartModel, ChartDataRow singleRow, boolean lastValue) throws JEVisException {
        this.chartModel = chartModel;
        this.FINISHED_SERIE = I18n.getInstance().getString("graph.progress.finishedserie") + " " + singleRow.getName();
        String unit = UnitManager.getInstance().format(singleRow.getUnit());
        if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        String tableEntryName = singleRow.getObject().getName();
        tableEntry = new TableEntry(tableEntryName);
        tableEntry.setColor(singleRow.getColor());
        tableData.add(tableEntry);

//        JEVisAttribute att = singleRow.getAttribute();
        List<JEVisSample> samples = singleRow.getSamples();

        double result = 0;
        long count = 0;

        if (!lastValue) {
            for (JEVisSample sample : samples) {
                Double value = sample.getValueAsDouble();
                result += value;
                count++;
            }
        } else {
            result = samples.get(samples.size() - 1).getValueAsDouble();
            count = 1;
        }

        QuantityUnits qu = new QuantityUnits();
        boolean quantityUnit = qu.isQuantityUnit(singleRow.getUnit());
        if (!quantityUnit) {
            result = result / count;
        }

        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(this.chartModel.getMinFractionDigits());
        nf_out.setMinimumFractionDigits(this.chartModel.getMaxFractionDigits());
        String text = nf_out.format(result) + " " + unit;
        tableEntry.setValue(text);

//        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
//        String s = dateTime.toString(dtf);
        String dataName = singleRow.getName();

        if (dataName == null) {
            dataName = singleRow.getObject().getName();
        }

        data = new XYChart.Data<>(result, dataName);

        JEConfig.getStatusBar().progressProgressJob(org.jevis.jeconfig.application.Chart.Charts.XYChart.JOB_NAME, 1, FINISHED_SERIE);

    }

    public XYChart.Data<Number, String> getData() {
        return data;
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