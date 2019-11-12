package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.unit.ChartUnits.QuantityUnits;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.JEConfig;
import org.jevis.jeconfig.application.tools.ColorHelper;
import org.jevis.jeconfig.plugin.graph.view.GraphPluginView;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.List;

public class BarChartSerie {
    private static final Logger logger = LogManager.getLogger(BarChartSerie.class);
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private XYChart.Series<Number, String> serie = new XYChart.Series<>();
    private TableEntry tableEntry;
    private DateTime timeStampFromFirstSample = DateTime.now();
    private DateTime timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);

    public BarChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        String unit = UnitManager.getInstance().format(singleRow.getUnit());
        if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        String tableEntryName = singleRow.getObject().getName();
        serie.setName(tableEntryName);
        serie.getData().clear();
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

        XYChart.Data<Number, String> data = new XYChart.Data<>(result, dataName);

        serie.getData().setAll(data);

        JEConfig.getStatusBar().progressProgressJob(GraphPluginView.JOB_NAME, 1, "Finished Serie");

    }

    public XYChart.Series getSerie() {
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
