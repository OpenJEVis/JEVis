package org.jevis.jeconfig.application.Chart.ChartElements;


import de.gsi.dataset.DataSet;
import de.gsi.dataset.spi.DoubleDataSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.i18n.I18n;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.plugin.charts.GraphPluginView;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.List;

public class BarChartSerie {
    private static final Logger logger = LogManager.getLogger(BarChartSerie.class);
    public final String FINISHED_SERIE;
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private DoubleDataSet dataSet = new DoubleDataSet("");
    private TableEntry tableEntry;
    private DateTime timeStampFromFirstSample = DateTime.now();
    private DateTime timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);

    public BarChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        this.FINISHED_SERIE = I18n.getInstance().getString("graph.progress.finishedserie") + singleRow.getTitle();
        String unit = UnitManager.getInstance().format(singleRow.getUnit());
        if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        String tableEntryName = singleRow.getObject().getName();
        dataSet.setName(tableEntryName);
        dataSet.clearData();
        tableEntry = new TableEntry(tableEntryName);
        tableEntry.setColor(ColorHelper.toColor(singleRow.getColor()));
        tableData.add(tableEntry);

//        JEVisAttribute att = singleRow.getAttribute();
        List<JEVisSample> samples = singleRow.getSamples();

        double result = 0;
        long count = 0;
        for (JEVisSample sample : samples) {
            Double value = sample.getValueAsDouble();
            result += value;
            count++;
        }

        QuantityUnits qu = new QuantityUnits();
        boolean quantityUnit = qu.isQuantityUnit(singleRow.getUnit());
        if (!quantityUnit) {
            result = result / count;
        }

        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(2);
        nf_out.setMinimumFractionDigits(2);
        String text = nf_out.format(result) + " " + unit;
        tableEntry.setValue(text);

//        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
//        String s = dateTime.toString(dtf);
        String dataName = singleRow.getTitle();

        if (dataName == null) {
            dataName = singleRow.getObject().getName();
        }

//        XYChart.Data<Number, String> data = new XYChart.Data<>(result, dataName);

        dataSet.add(0, result);

//        dataSet.getData().setAll(data);

        JEConfig.getStatusBar().progressProgressJob(GraphPluginView.JOB_NAME, 1, FINISHED_SERIE);

    }

    public DataSet getDataSet() {
        return dataSet;
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
