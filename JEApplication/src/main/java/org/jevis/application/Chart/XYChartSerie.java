package org.jevis.application.Chart;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.shape.Rectangle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jevis.api.JEVisException;
import org.jevis.api.JEVisObject;
import org.jevis.api.JEVisSample;
import org.jevis.application.application.AppLocale;
import org.jevis.application.application.SaveResourceBundle;
import org.jevis.commons.unit.UnitManager;
import org.joda.time.DateTime;

import java.text.NumberFormat;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

public class XYChartSerie {
    private static SaveResourceBundle rb = new SaveResourceBundle(AppLocale.BUNDLE_ID, AppLocale.getInstance().getLocale());
    private final Logger logger = LogManager.getLogger(XYChartSerie.class);
    private ObservableList<TableEntry> tableData = FXCollections.observableArrayList();
    private ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();
    private TreeMap<Double, JEVisSample> sampleMap = new TreeMap<Double, JEVisSample>();
    private XYChart.Series<Number, Number> serie;
    private TableEntry tableEntry;

    public XYChartSerie(ChartDataModel singleRow, Boolean hideShowIcons) throws JEVisException {
        String unit = UnitManager.getInstance().formate(singleRow.getUnit());
        if (unit.equals("")) unit = rb.getString("plugin.graph.chart.valueaxis.nounit");

        String tableEntryName = singleRow.getObject().getName();
        tableEntry = new TableEntry(tableEntryName);
        tableEntry.setColor(singleRow.getColor());

        singleRow.setTableEntry(tableEntry);
        tableData.add(tableEntry);

        boolean isQuantitiy = false;

        JEVisObject dp = singleRow.getDataProcessor();
        if (Objects.nonNull(dp)) {
            try {
                if (Objects.nonNull(dp.getAttribute("Value is a Quantity"))) {
                    if (Objects.nonNull(dp.getAttribute("Value is a Quantity").getLatestSample())) {
                        if (dp.getAttribute("Value is a Quantity").getLatestSample().getValueAsBoolean()) {

                            isQuantitiy = true;
                        }
                    }
                }
            } catch (JEVisException e) {
                logger.error("Error: could not data processor attribute", e);
            }
        }

        QuantityUnits qu = new QuantityUnits();
        if (qu.get().contains(singleRow.getUnit())) isQuantitiy = true;

        List<JEVisSample> samples = singleRow.getSamples();
        samples.forEach(sample -> {
            try {
                DateTime dateTime = sample.getTimestamp();
                Double value = sample.getValueAsDouble();
                Long timestamp = dateTime.getMillis();

                XYChart.Data<Number, Number> data = new XYChart.Data<Number, Number>(timestamp, value);

                Note note = new Note(sample.getNote(), singleRow.getColor());
                if (note.getNote() != null && hideShowIcons) {
                    note.setVisible(true);
                    data.setNode(note.getNote());
                } else {
                    Rectangle rect = new Rectangle(0, 0);
                    rect.setFill(singleRow.getColor());
                    rect.setVisible(false);
                    data.setNode(rect);
                }


                sampleMap.put((double) sample.getTimestamp().getMillis(), sample);
                seriesData.add(data);


            } catch (JEVisException e) {

            }
        });


        if (isQuantitiy) {
            calcTableValues(samples, unit);
        }

        serie = new XYChart.Series<>(tableEntryName, seriesData);
        singleRow.setSampleMap(sampleMap);
    }

    private void calcTableValues(List<JEVisSample> samples, String unit) throws JEVisException {
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        Double avg;
        Double sum = 0.0;

        for (JEVisSample smp : samples) {
            min = Math.min(min, smp.getValueAsDouble());
            max = Math.max(max, smp.getValueAsDouble());
            sum += smp.getValueAsDouble();
        }

        avg = sum / samples.size();
        NumberFormat nf_out = NumberFormat.getNumberInstance();
        nf_out.setMaximumFractionDigits(2);
        nf_out.setMinimumFractionDigits(2);

        tableEntry.setMin(nf_out.format(min) + " " + unit);
        tableEntry.setMax(nf_out.format(max) + " " + unit);
        tableEntry.setAvg(nf_out.format(avg) + " " + unit);
        tableEntry.setSum(nf_out.format(sum) + " " + unit);
    }

    public XYChart.Series getSerie() {
        return serie;
    }

    public TableEntry getTableEntry() {
        return tableEntry;
    }
}
