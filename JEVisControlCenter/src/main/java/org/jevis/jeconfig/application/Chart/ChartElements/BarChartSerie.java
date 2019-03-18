package org.jevis.jeconfig.application.Chart.ChartElements;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisAttribute;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.commons.chart.ChartDataModel;
import org.jevis.commons.unit.UnitManager;
import org.jevis.jeconfig.tool.I18n;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;

public class BarChartSerie {
    private static final Logger logger = LogManager.getLogger(BarChartSerie.class);
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, String>> seriesData = FXCollections.observableArrayList();
    private XYChart.Series<Number, String> serie = new XYChart.Series<>(seriesData);
    private TableEntry tableEntry;
    private DateTime timeStampFromFirstSample = DateTime.now();
    private DateTime timeStampFromLastSample = new DateTime(2001, 1, 1, 0, 0, 0);

    public BarChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        String unit = UnitManager.getInstance().format(singleRow.getUnit());
        if (unit.equals("")) unit = I18n.getInstance().getString("plugin.graph.chart.valueaxis.nounit");

        String tableEntryName = singleRow.getObject().getName();
        serie.setName(tableEntryName);
        tableEntry = new TableEntry(tableEntryName);
        tableEntry.setColor(singleRow.getColor());
        tableData.add(tableEntry);

        JEVisAttribute att = singleRow.getAttribute();
        JEVisSample latestSample = null;
        if (att != null && att.hasSample()) {
            latestSample = att.getLatestSample();

        }

        if (latestSample != null) {
            DateTime dateTime = latestSample.getTimestamp();
            Double value = latestSample.getValueAsDouble();

            NumberFormat nf_out = NumberFormat.getNumberInstance();
            nf_out.setMaximumFractionDigits(2);
            nf_out.setMinimumFractionDigits(2);
            String text = nf_out.format(value) + " " + unit;
            tableEntry.setValue(text);

            DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
            String s = dateTime.toString(dtf);
            XYChart.Data<Number, String> data = new XYChart.Data<>(value, s);

            StackPane node = new StackPane();
            Label label = new Label(text);
            Group group = new Group(label);
            StackPane.setAlignment(group, Pos.CENTER_RIGHT);
            StackPane.setMargin(group, new Insets(0, 0, 5, 0));
            node.getChildren().add(group);
            data.setNode(node);

            seriesData.clear();
            seriesData.add(data);

        }

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
