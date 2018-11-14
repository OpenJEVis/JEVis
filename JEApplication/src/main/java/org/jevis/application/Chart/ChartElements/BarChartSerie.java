package org.jevis.application.Chart.ChartElements;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisSample;
import org.jevis.application.Chart.ChartDataModel;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;
import java.util.TreeMap;

public class BarChartSerie implements Serie {
    private static SaveResourceBundle rb = new SaveResourceBundle(AppLocale.BUNDLE_ID, AppLocale.getInstance().getLocale());
    private static final Logger logger = LogManager.getLogger(BarChartSerie.class);
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private ObservableList<BarChart.Data<String, Number>> seriesData = FXCollections.observableArrayList();
    private TreeMap<Double, JEVisSample> sampleMap = new TreeMap<Double, JEVisSample>();
    private BarChart.Series<String, Number> serie;
    private TableEntry tableEntry;

    public BarChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        String unit = UnitManager.getInstance().formate(singleRow.getUnit());
        if (unit.equals("")) unit = rb.getString("plugin.graph.chart.valueaxis.nounit");

        String tableEntryName = singleRow.getObject().getName();
        tableEntry = new TableEntry(tableEntryName);
        tableEntry.setColor(singleRow.getColor());

        singleRow.setTableEntry(tableEntry);
        tableData.add(tableEntry);

//        boolean isQuantitiy = false;

//        JEVisObject dp = singleRow.getDataProcessor();
//        if (Objects.nonNull(dp)) {
//            try {
//                if (Objects.nonNull(dp.getAttribute("Value is a Quantity"))) {
//                    if (Objects.nonNull(dp.getAttribute("Value is a Quantity").getLatestSample())) {
//                        if (dp.getAttribute("Value is a Quantity").getLatestSample().getValueAsBoolean()) {
//
//                            isQuantitiy = true;
//                        }
//                    }
//                }
//            } catch (JEVisException e) {
//                logger.error("Error: could not data processor attribute", e);
//            }
//        }

//        QuantityUnits qu = new QuantityUnits();
//        if (qu.get().contains(singleRow.getUnit())) isQuantitiy = true;

        List<JEVisSample> samples = singleRow.getSamples();
        samples.forEach(sample -> {
            try {
                DateTime dateTime = sample.getTimestamp();
                Double value = sample.getValueAsDouble();

                DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
                String s = dateTime.toString(dtf);
                BarChart.Data<String, Number> data = new BarChart.Data<>(s, value);

                sampleMap.put((double) sample.getTimestamp().getMillis(), sample);
                seriesData.add(data);

            } catch (JEVisException e) {

            }
        });


//        if (isQuantitiy) {
        calcTableValues(tableEntry, samples, unit);
//        }

        serie = new BarChart.Series<>(tableEntryName, seriesData);
        singleRow.setSampleMap(sampleMap);
    }

    public XYChart.Series getSerie() {
        return serie;
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }
}
